package actordata;

import java.util.HashMap;
import java.util.Map;

import akka.actor.ActorRef;


/**
 * This is the abstract superclass of data-structures held by the different FSMs modeling the protocol.
 * <p>
 * This is an immutable object type in order to comply to the Akka good practices regarding FSMs.
 * @author Christian Mouchet
 */
public abstract class Data {
	protected final Map<ActorRef,Integer> participants;
	
	protected Data(Map<ActorRef,Integer> participants) {
		if(participants != null)
			this.participants = new HashMap<ActorRef, Integer>(participants);
		else 
			this.participants = null;
	}
	
	/** Returns a new copy of this object with the ActorRef->Party index mapping updated.
	 * @param participants the new ActorRef->Party index mapping
	 * @return updated structure with a new ActorRef->Party index mapping.
	 */
	public abstract Data withParticipants(Map<ActorRef,Integer> participants);
	
	public Map<ActorRef,Integer> getParticipants() {
		return new HashMap<ActorRef, Integer>(this.participants);
	}
}
