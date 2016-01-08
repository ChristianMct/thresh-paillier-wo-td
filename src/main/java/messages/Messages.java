package messages;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import protocol.BGWParameters.BGWPrivateParameters;
import akka.actor.ActorRef;

@SuppressWarnings("serial")
public class Messages {


	public static class Complaint implements Serializable{
		public final int id;
		public Complaint(int id) {
			this.id = id;
		}
	}
	
	public static class BGWNPoint implements Serializable {
		public final BigInteger point;
		public BGWNPoint(BigInteger point) {
			this.point = point;
		}
	}
	
//	public static class BGWResult implements Serializable{
//		private static final long serialVersionUID = -7313055519959943195L;
//		public final BigInteger N;
//		public BGWResult(BigInteger N) {
//			this.N = N;
//		}
//	}
	
	public static class Participants implements Serializable {
		private final HashMap<ActorRef,Integer> participants;
		public Participants(Map<ActorRef,Integer> participants) {
			this.participants = new HashMap<ActorRef, Integer>(participants);
		}
		public Map<ActorRef, Integer> getParticipants() {
			return new HashMap<ActorRef, Integer>(this.participants);
		}
	}
	
	public static class CandidateN implements Serializable {
		public final BigInteger N;
		public final BGWPrivateParameters bgwPrivateParameters;		
		public CandidateN(BigInteger candidateN, BGWPrivateParameters bgwPrivateParameters) {
			this.N = candidateN;
			this.bgwPrivateParameters = bgwPrivateParameters;
		}
	}
	
	public static class QiTestForRound implements Serializable {
		public final BigInteger Qi;
		public final int round;
		public QiTestForRound(BigInteger Qi, int round) {
			this.Qi = Qi;
			this.round = round;
		}
	}
	
	public static class BiprimalityTestResult implements Serializable {
		public BigInteger N;
		public BGWPrivateParameters bgwPrivateParameters;
		public boolean passes;
		public BiprimalityTestResult(BigInteger N,
				BGWPrivateParameters bgwPrivateParameters, boolean passes) {
			this.N = N;
			this.bgwPrivateParameters = bgwPrivateParameters;
			this.passes = passes;
		}
	}
	
	public static class AcceptedN implements Serializable {	
		public final BigInteger N;
		public final BigInteger phi;
		public AcceptedN(BigInteger N, BigInteger phi) {
			this.N = N;
			this.phi = phi;
		}
	}
	
	public static class KeyDerivationResult {

	}
}
