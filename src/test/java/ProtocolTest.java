import java.util.ArrayList;
import java.util.List;

import protocol.ProtocolParameters;
import actors.ProtocolActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class ProtocolTest {

	public static void main(String[] args) throws InterruptedException {
	    ActorSystem system = ActorSystem.create();
	    List<ActorRef> all = new ArrayList<ActorRef>(5);
	    for(int i=0; i<5; i++) {
	    	all.add(system.actorOf(Props.create(ProtocolActor.class)));
	    }
	
	    ProtocolParameters protoParam = ProtocolParameters.gen();
	    all.stream().forEach(actor -> actor.tell(protoParam, ActorRef.noSender()));
	}

}
