package actors;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import messages.Messages.BGWNPoint;
import messages.Messages.BiprimalityTestResult;
import messages.Messages.CandidateN;
import messages.Messages.Participants;
import messages.Messages.QiTestForRound;
import messages.Messages.ThetaPoint;
import messages.Messages.VerificationKey;
import paillierp.key.PaillierPrivateThresholdKey;
import protocol.BGWParameters.BGWPublicParameters;
import protocol.KeysDerivationParameters.KeysDerivationPublicParameters;
import protocol.ProtocolParameters;
import actordata.ProtocolData;
import actors.ProtocolActor.States;
import akka.actor.AbstractLoggingFSM;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;

/**
 * This is the encoding of the main phases of the protocol described by Takashi Nishide 
 * and Kouichi Sakurai in <i> Distributed Paillier Cryptosystem without Trusted Dealer </i>.
 * <p> This Akka actor describes the transition between the main phases of the protocol and 
 * the forwarding of the messages to its children. There is one children actor per phases,
 * each of them implementing a sub-protocol: 
 * <ul> <li> The BGWActor implements the BGW protocol in which the parties generate N=pq
 * 		<li> The BiprimalityTestActor implements a distributed biprimality test for N. It accept
 * 			 N in the case p and q are prime.
 * 		<li> The KeysDerivationActor implements a distributed key derivation based on an accepted
 * 			 N.
 * </ul>
 * <p>
 * @author Christian Mouchet
 */
public class ProtocolActor extends AbstractLoggingFSM<States, ProtocolData> {
	
	public static enum States {INITIALIZATION,BGW,BIPRIMAL_TEST, KEYS_DERIVATION};
	
	private  ActorRef bgwActor;
	private  ActorRef biprimalTestActor;
	private  ActorRef keysDerivationActor;
	
	/** Constructor of the actor. This makes use of the Akka AbstractFSM DSL to build the FSM.
	 * @param protocolParams the public parameters on which the parties agreed 
	 */
	@SuppressWarnings("deprecation")
	public ProtocolActor(ProtocolParameters protocolParams) {
		bgwActor = context().actorOf(Props.create(BGWProtocolActor.class, protocolParams,self()), "BGWActor");
		biprimalTestActor = context().actorOf(Props.create(BiprimalityTestActor.class, self()), "BiprimalityTestActor");
		keysDerivationActor = context().actorOf(Props.create(KeysDerivationActor.class, protocolParams,  self()), "KeysDerivationActor");
		
		startWith(States.INITIALIZATION, ProtocolData.init());
		
		
		when(States.INITIALIZATION, matchEvent(Participants.class,
				(participants,data) -> {
					biprimalTestActor.tell(participants, self());
					keysDerivationActor.tell(participants, self());
					return goTo(States.BGW).using(data.withParticipants(participants.getParticipants()));
				}));
		
		when(States.BGW, matchEvent(CandidateN.class, 
				(candidateN, data) -> {
					if(data.getParticipants().get(self())==1)
						System.out.println("TRY: N="+candidateN.N);
					return goTo(States.BIPRIMAL_TEST).using(data.withNewN(candidateN.N, candidateN.bgwPrivateParameters));
				}));
		
		onTransition((from,to) -> {		
			
			// Starting or looping back to BGW after rejecting N
			if(to == States.BGW) {
				bgwActor.tell(new Participants(nextStateData().getParticipants()), self());
			}
			// Starting Biprimality test after generating a new N
			if(to == States.BIPRIMAL_TEST) {
				biprimalTestActor.tell(new CandidateN(nextStateData().N, nextStateData().bgwPrivateParameters), sender());
			}
			
		});
		
		when(States.BIPRIMAL_TEST, matchEvent(BiprimalityTestResult.class, 
				(result, data) -> {
					
					if(result.passes) {
						if(data.getParticipants().get(self())==1)
							System.out.println("FOUND N="+result.N);
						return goTo(States.KEYS_DERIVATION).using(data.withNewN(result.N, result.bgwPrivateParameters));
					} else {
						if(data.getParticipants().get(self())==1)
							System.out.println("DID NOT PASS");
						return goTo(States.BGW).using(data);
					}
		}));
		
		// N passes all biprimality tests
		onTransition(matchState(States.BIPRIMAL_TEST, States.KEYS_DERIVATION, () -> {
			ProtocolData data = nextStateData();
			keysDerivationActor.tell(new BiprimalityTestResult(data.N, data.bgwPrivateParameters, true),  self());
		}));
		
		when(States.KEYS_DERIVATION, matchEvent(PaillierPrivateThresholdKey.class, (key, data) -> {
			new File("keys").mkdirs();
			DataOutputStream out = new DataOutputStream(new FileOutputStream("keys/"+self().path().name()+".privkey"));
			out.write(key.toByteArray());
			out.close();
			
			System.out.println(self().path()+" DONE");
			bgwActor.tell(PoisonPill.getInstance(), self());
			context().system().shutdown();
			return stop();
		}));
		
		
		// Routing logic
		whenUnhandled(matchAnyEvent((evt,data) -> {
			if(evt instanceof BGWPublicParameters || evt instanceof BGWNPoint) {
				//Thread.sleep(1); // SIMULATE DELAY
				bgwActor.tell(evt, sender());
			}
			else if(evt instanceof QiTestForRound) {
				//Thread.sleep(1); // SIMULATE DELAY
				biprimalTestActor.tell(evt, sender());
			}
			else if(evt instanceof KeysDerivationPublicParameters || evt instanceof ThetaPoint || evt instanceof VerificationKey) {
				//Thread.sleep(1); // SIMULATE DELAY
				keysDerivationActor.tell(evt, sender());
			}
			
			return stay();
		}));
		
	}
}
