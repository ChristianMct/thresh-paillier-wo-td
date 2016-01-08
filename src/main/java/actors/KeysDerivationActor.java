package actors;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Set;

import math.IntegersUtils;
import math.Polynomial;
import math.PolynomialMod;
import messages.Messages;
import messages.Messages.AcceptedN;
import messages.Messages.BetaiRiShares;
import messages.Messages.Participants;
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
			BigInteger KN = N.multiply(BigInteger.valueOf(protocolParameters.K));
			BigInteger K2N = KN.multiply(BigInteger.valueOf(protocolParameters.K));
			
			BigInteger betai = IntegersUtils.pickInRange(BigInteger.ZERO, KN, rand);
			BigInteger Ri = IntegersUtils.pickInRange(BigInteger.ZERO, K2N, rand);
			
			PolynomialMod betaiSharing = new PolynomialMod(protocolParameters.t, protocolParameters.Pp, betai, protocolParameters.k, rand);
			Polynomial RiSharing = new Polynomial(protocolParameters.t, Ri, protocolParameters.k, rand);
			
			int self = data.getParticipants().get(this.master);
			BigInteger selfBetaShare = betaiSharing.eval(self);
			BigInteger selfRiShare = RiSharing.eval(self);
			
			KeysDerivationData nextData = data.withN(N, phi)
												.withSharings(betaiSharing, RiSharing)
												.withNewBetaiRiSharesFor(self, selfBetaShare, selfRiShare);
			
			return goTo(States.AWAITING_BETAi_Ri_SHARES).using(nextData);
		}));
		
		onTransition(matchState(States.AWAITING_N, States.AWAITING_BETAi_Ri_SHARES, () -> {
			Map<ActorRef, Integer> actors = nextStateData().getParticipants();
			PolynomialMod betaiSharing = nextStateData().betaiSharing;
			Polynomial RiSharing = nextStateData().RiSharing;
			actors.entrySet().stream().forEach(e -> {
				if(!e.getKey().equals(this.master)){
					BigInteger betaiShare = betaiSharing.eval(e.getValue());
					BigInteger RiShare = RiSharing.eval(e.getValue());
					e.getKey().tell(new Messages.BetaiRiShares(betaiShare, RiShare), this.master);
				}
			});
		}));
		
		when(States.AWAITING_BETAi_Ri_SHARES, matchEvent(BetaiRiShares.class, (newShare, data) -> {
			
			Map<ActorRef, Integer> actors = data.getParticipants();
			Integer sender = actors.get(sender());
			
			KeysDerivationData nextData = data.withNewBetaiRiSharesFor(sender, newShare.betaiShare, newShare.RiShare);
			
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