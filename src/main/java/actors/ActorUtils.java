package actors;

import akka.actor.ActorRef;

public class ActorUtils {
	
	public static String nameOf(ActorRef actor) {
		return actor.path().toStringWithoutAddress();
	}
}
