package actors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import messages.Messages.BGWResult;
import messages.Messages.Participants;
import protocol.ProtocolParameters;
import actordata.ProtocolData;
import actors.ProtocolActor.States;
import akka.actor.AbstractLoggingFSM;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Props;

public class ProtocolActor extends AbstractLoggingFSM<States, ProtocolData> {
	
	public static enum States {INITIALIZATION,BGW,BIPRIMAL_TEST};
	
	private  Map<ActorRef,Integer> participants;
	private  ActorRef bgwActor;
	private  ActorRef biprimalTestActor;
	
	public ProtocolActor(ProtocolParameters protocolParams) {
		bgwActor = context().actorOf(Props.create(BGWProtocolActor.class, protocolParams,self()), "BGWActor");
		biprimalTestActor = null;
		
		startWith(States.INITIALIZATION, ProtocolData.init());
		
		when(States.INITIALIZATION, matchEvent(Participants.class,
				(participants,data) -> {
					this.participants = participants.getParticipants();
					bgwActor.tell(participants, self());
					return goTo(States.BGW);
				}));
		
		when(States.BGW, matchEvent(BGWResult.class, 
				(bgwResult, data) -> {
					bgwActor.tell(PoisonPill.getInstance(), self());
					System.out.println(self().path()+" N = "+bgwResult.N);
					return goTo(States.BIPRIMAL_TEST).using(data.withNewN(bgwResult.N));
				}));
		when(States.BGW, matchAnyEvent(
				(evt, data) -> {
					bgwActor.tell(evt, sender());
					return stay();
				}));
		
		when(States.BIPRIMAL_TEST, matchEvent(Boolean.class, (evt,data) -> {return stay();}));
	}
}
