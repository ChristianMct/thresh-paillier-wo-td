package actors;


import java.util.Random;

import messages.Messages.BGWNPoint;
import messages.Messages.BiprimalityTestResult;
import messages.Messages.CandidateN;
import messages.Messages.Participants;
import messages.Messages.QiTestForRound;
import protocol.BGWParameters.BGWPublicParameters;
import protocol.ProtocolParameters;
import actordata.ProtocolData;
import actors.ProtocolActor.States;
import akka.actor.AbstractLoggingFSM;
import akka.actor.ActorRef;
import akka.actor.Props;

public class ProtocolActor extends AbstractLoggingFSM<States, ProtocolData> {
	
	public static enum States {INITIALIZATION,BGW,BIPRIMAL_TEST};
	
	private  ActorRef bgwActor;
	private  ActorRef biprimalTestActor;
	
	public ProtocolActor(ProtocolParameters protocolParams) {
		bgwActor = context().actorOf(Props.create(BGWProtocolActor.class, protocolParams,self()), "BGWActor");
		biprimalTestActor = context().actorOf(Props.create(BiprimalityTestActor.class, self()), "BiprimalityTestActor");
		
		startWith(States.INITIALIZATION, ProtocolData.init());
		
		when(States.INITIALIZATION, matchEvent(Participants.class,
				(participants,data) -> {
					//bgwActor.tell(participants, self());
					biprimalTestActor.tell(participants, self());
					return goTo(States.BGW).using(data.withParticipants(participants.getParticipants()));
				}));
		
		when(States.BGW, matchEvent(CandidateN.class, 
				(candidateN, data) -> {
					if(data.getParticipants().get(self())==1)
						System.out.println("TRY: N="+candidateN.N);
					return goTo(States.BIPRIMAL_TEST).using(data.withNewN(candidateN.N, candidateN.bgwPrivateParameters));
				}));

		
		when(States.BIPRIMAL_TEST, matchEvent(BiprimalityTestResult.class, 
				(result, data) -> {
					
					if(result.passes) {
						System.out.println("FOUND N="+result.N);
						return stop();
					} else {
						if(data.getParticipants().get(self())==1)
							System.out.println("DID NOT PASS");
						//bgwActor.tell(new Participants(data.getParticipants()), self());
						return goTo(States.BGW).using(data);
					}
		}));
		
		
		whenUnhandled(matchAnyEvent((evt,data) -> {
			Random rand = new Random();
			if(evt instanceof BGWPublicParameters || evt instanceof BGWNPoint) {
				Thread.sleep(rand.nextInt(3));
				bgwActor.tell(evt, sender());
			}
			else if(evt instanceof QiTestForRound) {
				Thread.sleep(rand.nextInt(3));
				biprimalTestActor.tell(evt, sender());
			}
			else
				System.out.println("CACA");
			
			return stay();
		}));
		
		onTransition((from,to) -> {
			//System.out.println(String.format("%s transition %s -> %s", ActorUtils.nameOf(self()), from, to));
			
			if(to == States.BGW) {
				bgwActor.tell(new Participants(nextStateData().getParticipants()), self());
			}
			
			if(to == States.BIPRIMAL_TEST) {
				biprimalTestActor.tell(new CandidateN(nextStateData().N, nextStateData().bgwPrivateParameters), sender());
			}
			
		});
	}
}
