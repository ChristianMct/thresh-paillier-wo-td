package actordata;

import java.util.HashMap;
import java.util.Map;

import akka.actor.ActorRef;

public class Data {
	protected final Map<ActorRef,Integer> participants;
	
	protected Data(Map<ActorRef,Integer> participants) {
		if(participants != null)
			this.participants = new HashMap<ActorRef, Integer>(participants);
		else 
			this.participants = null;
	}
	
	public Data withParticipants(Map<ActorRef,Integer> participants) {
		return new Data(new HashMap<ActorRef, Integer>(participants));
	}
	
	public Map<ActorRef,Integer> getParticipants() {
		return new HashMap<ActorRef, Integer>(this.participants);
	}
}
