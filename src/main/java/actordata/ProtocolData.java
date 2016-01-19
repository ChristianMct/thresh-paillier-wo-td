package actordata;

import java.math.BigInteger;
import java.util.Map;

import protocol.BGWParameters.BGWPrivateParameters;
import akka.actor.ActorRef;

/**
 * Represents the state data of the top level actor of the protocol's FSM.
 * <p>
 * This is an immutable object type in order to comply to the Akka good practices regarding FSMs.
 * @author Christian Mouchet
 */
public class ProtocolData extends Data {
	
	/** The current candidate to RSA modulus in the protocol. Can be accepted or not depending on the phase.*/
	public final BigInteger N;
	/** The BGW private parameters associated with the current N.*/
	public final BGWPrivateParameters bgwPrivateParameters;
	
	private ProtocolData(Map<ActorRef,Integer> participants, BigInteger N, BGWPrivateParameters bgwPrivateParameters) {
		super(participants);
		this.N = N;
		this.bgwPrivateParameters = bgwPrivateParameters;
	}
	
	public ProtocolData withNewN(BigInteger N, BGWPrivateParameters bgwPrivateParameters) {
		return new ProtocolData(participants,N, bgwPrivateParameters);
	}

	/** Used to initialize the data object.
	 * @return  a new object with all the field initialized to null
	 */
	public static ProtocolData init() {
		return new ProtocolData(null,null, null);
	}
	
	public ProtocolData withParticipants(Map<ActorRef,Integer> participants) { 
		return new ProtocolData(participants, N, bgwPrivateParameters);
	}
}
