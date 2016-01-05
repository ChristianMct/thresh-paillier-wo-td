package messages;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.omg.CORBA.PUBLIC_MEMBER;

import akka.actor.ActorRef;


public class Messages {
	
	public static class Complaint implements Serializable{
		private static final long serialVersionUID = -1098299909396056849L;
		public final int id;
		public Complaint(int id) {
			this.id = id;
		}
	}
	
	public static class BGWNPoint implements Serializable {
		private static final long serialVersionUID = 347073615387402156L;
		public final BigInteger point;
		public BGWNPoint(BigInteger point) {
			this.point = point;
		}
	}
	
	public static class BGWResult implements Serializable{
		private static final long serialVersionUID = -7313055519959943195L;
		public final BigInteger N;
		public BGWResult(BigInteger N) {
			this.N = N;
		}
	}
	
	public static class Participants implements Serializable {
		private static final long serialVersionUID = 7032510981247682105L;
		private final HashMap<ActorRef,Integer> participants;
		public Participants(Map<ActorRef,Integer> participants) {
			this.participants = new HashMap<ActorRef, Integer>(participants);
		}
		public Map<ActorRef, Integer> getParticipants() {
			return new HashMap<ActorRef, Integer>(this.participants);
		}
	}
}
