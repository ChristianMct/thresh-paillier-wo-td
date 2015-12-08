import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import messages.Messages.Participants;
import protocol.ProtocolParameters;
import actors.BGWProtocolActor;
import actors.ProtocolActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class ProtocolTest {

	public static void main(String[] args) throws InterruptedException {
	    ActorSystem system = ActorSystem.create();
	    
	    ProtocolParameters protoParam = ProtocolParameters.gen(128,new SecureRandom());
	    
	    Map<ActorRef,Integer> indexMap = new HashMap<ActorRef,Integer>(5);
	    for(int i=0; i<5; i++) {
	    	indexMap.put(system.actorOf(Props.create(ProtocolActor.class, protoParam),"Actor"+i),i);
	    }
	    
	    Participants participants = new Participants(indexMap);
	    
	    indexMap.keySet().stream().forEach(actor -> actor.tell(participants, ActorRef.noSender()));
	}

}
