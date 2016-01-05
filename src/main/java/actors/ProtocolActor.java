package actors;

import java.util.Map;

import messages.Messages.CandidateN;
import messages.Messages.Participants;
import protocol.ProtocolParameters;
import actordata.ProtocolData;
import actors.ProtocolActor.States;
import akka.actor.AbstractLoggingFSM;
import akka.actor.ActorRef;
import akka.actor.Props;

public class ProtocolActor extends AbstractLoggingFSM<States, ProtocolData> {
	
	public static enum States {INITIALIZATION,BGW,BIPRIMAL_TEST};
	
	private  Map<ActorRef,Integer> participants;
	private  ActorRef bgwActor;
	private  ActorRef biprimalTestActor;
	
	public ProtocolActor(ProtocolParameters protocolParams) {
		bgwActor = context().actorOf(Props.create(BGWProtocolActor.class, protocolParams,self()), "BGWActor");
		biprimalTestActor = context().actorOf(Props.create(BiprimalityTestActor.class, self()), "BiprimalityTestActor");
		biprimalTestActor = null;
		
		startWith(States.INITIALIZATION, ProtocolData.init());
		
		when(States.INITIALIZATION, matchEvent(Participants.class,
				(participants,data) -> {
					this.participants = participants.getParticipants();
					bgwActor.tell(participants, self());
					return goTo(States.BGW);
				}));
		
		when(States.BGW, matchEvent(CandidateN.class, 
				(candidateN, data) -> {
					System.out.println(self().path()+" N = "+candidateN.N);
					return goTo(States.BIPRIMAL_TEST).using(data.withNewN(candidateN.N));
				}));
		when(States.BGW, matchAnyEvent(
				(evt, data) -> {
					bgwActor.tell(evt, sender());
					return stay();
				}));
		
		when(States.BIPRIMAL_TEST, matchAnyEvent((evt,data) -> {
			biprimalTestActor.tell(evt, sender());
			return stay();
		}));
	}
}
