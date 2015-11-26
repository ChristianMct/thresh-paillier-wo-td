import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import protocol.ProtocolParameters;
import actors.ProtocolActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class ProtocolTest {

	public static void main(String[] args) throws InterruptedException {
	    ActorSystem system = ActorSystem.create();
	    Map<ActorRef,Integer> indexMap = new HashMap<ActorRef,Integer>(5);
	    for(int i=0; i<5; i++) {
	    	indexMap.put(system.actorOf(Props.create(ProtocolActor.class),"Actor"+i),i);
	    }

	    ProtocolActor.indexMap = indexMap;
	    
	    ProtocolParameters protoParam = ProtocolParameters.gen(128,new SecureRandom());
	    indexMap.keySet().stream().forEach(actor -> actor.tell(protoParam, ActorRef.noSender()));
	}

}
