import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import messages.Messages.Participants;
import protocol.ProtocolParameters;
import actors.ProtocolActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

/**
 * This script creates a local Actor System and runs the protocol for N_PARTIES parties with a 
 * threshold of T_THRESHOLD. The bit size of the keys can be controlled using the KEY_SIZE constant.
 * This constant is the minimum size of p and q in bit.
 * @author Christian Mouchet
 */
public class ProtocolTest {

	public static final int N_PARTIES = 10; // Current implementation: works for 3 to 30 
	public static final int T_THRESHOLD = 4; // Should be less than n/2
	public static final int KEY_SIZE = 128; // Tested up to 512
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws InterruptedException {
	    ActorSystem system = ActorSystem.create();
	    
	    ProtocolParameters protoParam = ProtocolParameters.gen(KEY_SIZE, N_PARTIES, T_THRESHOLD, new SecureRandom());
	    //System.out.println("Pp="+protoParam.P);
	    
	    Map<ActorRef,Integer> indexMap = new HashMap<ActorRef,Integer>(N_PARTIES);
	    for(int i=1; i<=N_PARTIES; i++) {
	    	indexMap.put(system.actorOf(Props.create(ProtocolActor.class, protoParam),"Actor"+i),i);
	    }
	    
	    Participants participants = new Participants(indexMap);
	    
	    long start = System.currentTimeMillis();
	    
	    indexMap.keySet().stream().forEach(actor -> actor.tell(participants, ActorRef.noSender()));
	    
	    system.awaitTermination();
	    
	    long different = System.currentTimeMillis() - start;
	    printEllapsedTime(different);
	}

	private static void printEllapsedTime(long different) {
	    long secondsInMilli = 1000;
		long minutesInMilli = secondsInMilli * 60;
		long hoursInMilli = minutesInMilli * 60;
		
		long elapsedHours = different / hoursInMilli;
		different = different % hoursInMilli;
		
		long elapsedMinutes = different / minutesInMilli;
		different = different % minutesInMilli;
		
		long elapsedSeconds = different / secondsInMilli;
	    System.out.println(String.format("Finished after %dh %dm %ds",elapsedHours, elapsedMinutes, elapsedSeconds));
	}

}
