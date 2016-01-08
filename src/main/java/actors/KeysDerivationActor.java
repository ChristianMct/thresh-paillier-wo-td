package actors;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Set;

import messages.Messages.AcceptedN;
import messages.Messages.Participants;
import protocol.KeysDerivationParameters.KeysDerivationPrivateParameters;
import protocol.KeysDerivationParameters.KeysDerivationPublicParameters;
import protocol.ProtocolParameters;
import actordata.KeysDerivationData;
import actors.KeysDerivationActor.States;
import akka.actor.AbstractLoggingFSM;
import akka.actor.ActorRef;

public class KeysDerivationActor extends AbstractLoggingFSM<States, KeysDerivationData> {
	
	public static enum States {INITIALIZATION, AWAITING_N, AWAITING_BETAi_Ri_SHARES}

	private SecureRandom rand = new SecureRandom();
	private ActorRef master;
	
	public KeysDerivationActor(ActorRef master, ProtocolParameters protocolParameters) {
		
		this.master = master;
		
		startWith(States.INITIALIZATION, KeysDerivationData.init());
		
		when(States.INITIALIZATION, matchEvent(Participants.class, (participants, data) -> {
			return goTo(States.AWAITING_N).using(data.withParticipants(participants.getParticipants()));
		}));
		
		when(States.AWAITING_N, matchEvent(AcceptedN.class, (acceptedN, data) -> {
			BigInteger N = acceptedN.N;
			BigInteger phi = acceptedN.phi;
			int self = data.getParticipants().get(this.master);

			KeysDerivationPrivateParameters keysDerivationPrivateParameters = KeysDerivationPrivateParameters.gen(protocolParameters, self, N, rand);
			
			KeysDerivationPublicParameters keysDerivationPublicParameters = KeysDerivationPublicParameters.genFor(self, keysDerivationPrivateParameters);
			
			KeysDerivationData nextData = data.withN(N, phi)
												.withPrivateParameters(keysDerivationPrivateParameters)
												.withNewPublicParametersFor(self, keysDerivationPublicParameters);
			
			return goTo(States.AWAITING_BETAi_Ri_SHARES).using(nextData);
		}));
		
		onTransition(matchState(States.AWAITING_N, States.AWAITING_BETAi_Ri_SHARES, () -> {
			Map<ActorRef, Integer> actors = nextStateData().getParticipants();
			KeysDerivationPrivateParameters privateParameters = nextStateData().keysDerivationPrivateParameters;
			actors.entrySet().stream().forEach(e -> {
				if(!e.getKey().equals(this.master)){
					e.getKey().tell(KeysDerivationPublicParameters.genFor(e.getValue(), privateParameters), this.master);
				}
			});
		}));
		
		when(States.AWAITING_BETAi_Ri_SHARES, matchEvent(KeysDerivationPublicParameters.class, (newShare, data) -> {
			
			Map<ActorRef, Integer> actors = data.getParticipants();
			Integer sender = actors.get(sender());
			
			KeysDerivationData nextData = data.withNewPublicParametersFor(sender, newShare);
			
			if(!nextData.hasBetaiRiOf(actors.values())) {
				return stay().using(nextData);
			} else {
				System.out.println("HAS ALL");
				return stop();
			}
			
		}));
		
		whenUnhandled(matchAnyEvent((evt, data) -> {
			self().tell(evt, sender());
			return stay();
		}));
		
	}
	
	private void broadCast(Object o, Set<ActorRef> targets) {
		targets.stream().forEach(actor -> {if (!actor.equals(this.master)) actor.tell(o, this.master);});
	}
}