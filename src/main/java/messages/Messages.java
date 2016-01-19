package messages;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import protocol.BGWParameters.BGWPrivateParameters;
import akka.actor.ActorRef;

/**
 * Container class for all messages types
 * @author Christian Mouchet
 */
@SuppressWarnings("serial")
public class Messages {

	/*
	 * EXTERNAL MESSAGES 
	 */

	/**
	 * A complaint message sent when a share was found to be invalid.
	 */
	public static class Complaint implements Serializable{
		/** The id of the party that produced the invalid share*/
		public final int id;
		public Complaint(int id) {
			this.id = id;
		}
	}
	
	
	/**
	 * Wraps a BigInteger as a share of N
	 */
	public static class BGWNPoint implements Serializable {
		/**A share of N*/
		public final BigInteger point;
		public BGWNPoint(BigInteger point) {
			this.point = point;
		}
	}
	
	
	/**
	 * Wraps a BigInteger as a share of Theta
	 */
	public static class ThetaPoint implements Serializable {
		/**a share of Theta*/
		public final BigInteger thetai;
		public ThetaPoint(BigInteger thetai) {
			this.thetai = thetai;
		}
	}
	
	/**
	 * Wraps a BigInteger as a verification key
	 */
	public static class VerificationKey implements Serializable {
		/** A verification key*/
		public final BigInteger verificationKey;
		public VerificationKey(BigInteger verificationKey) {
			this.verificationKey = verificationKey;
		}
	}
	
	/** 
	 * Wraps a BigInteger as a Qi used for Biprimality test. It also contains the round number to avoid concurrency issues 
	 */
	public static class QiTestForRound implements Serializable {
		/** A Qi in the Biprimality test*/
		public final BigInteger Qi;
		/** The current round number in the Biprimality test*/
		public final int round;
		public QiTestForRound(BigInteger Qi, int round) {
			this.Qi = Qi;
			this.round = round;
		}
	}
	
	
	
	/**
	 * Wraps an HashMap containing the mapping between ActorRef's and id in the protocol
	 */
	public static class Participants implements Serializable {
		private final HashMap<ActorRef,Integer> participants;
		public Participants(Map<ActorRef,Integer> participants) {
			this.participants = new HashMap<ActorRef, Integer>(participants);
		}
		/** @return the map containing the mapping between ActorRef's and id in the protocol*/
		public Map<ActorRef, Integer> getParticipants() {
			return new HashMap<ActorRef, Integer>(this.participants);
		}
	}
	
	/*
	 * INTERNAL MESSAGES 
	 */
	
	/**
	 * Wraps a BigInteger as a candidate to RSA modulus and its associated BGW private parameters
	 */
	public static class CandidateN implements Serializable {
		/** The candidate to RSA modulus*/
		public final BigInteger N;
		/** The BGW private parameters associated with the candidate to RSA modulus*/
		public final BGWPrivateParameters bgwPrivateParameters;		
		public CandidateN(BigInteger candidateN, BGWPrivateParameters bgwPrivateParameters) {
			this.N = candidateN;
			this.bgwPrivateParameters = bgwPrivateParameters;
		}
	}

	/**
	 *	Wraps a BigInteger as a tested candidate to RSA modulus along with its associated BGW private parameters and
	 *	a boolean indicating success or failure of the Biprimality test.
	 */
	public static class BiprimalityTestResult implements Serializable {
		/** The candidate to RSA modulus*/
		public BigInteger N;
		/** The BGW private parameters associated with N*/
		public BGWPrivateParameters bgwPrivateParameters;
		/** The result of the Biprimality test. True if succeed, false if not.*/
		public boolean passes;
		public BiprimalityTestResult(BigInteger N,
				BGWPrivateParameters bgwPrivateParameters, boolean passes) {
			this.N = N;
			this.bgwPrivateParameters = bgwPrivateParameters;
			this.passes = passes;
		}
	}
	

	

}
