package actordata;

import java.math.BigInteger;
import java.util.Map;

import protocol.BGWParameters.BGWPrivateParameters;
import akka.actor.ActorRef;

public class ProtocolData extends Data {
	
	public final BigInteger N;
	public final BGWPrivateParameters bgwPrivateParameters;
	
	private ProtocolData(Map<ActorRef,Integer> participants, BigInteger N, BGWPrivateParameters bgwPrivateParameters) {
		super(participants);
		this.N = N;
		this.bgwPrivateParameters = bgwPrivateParameters;
	}
	
	public ProtocolData withNewN(BigInteger N, BGWPrivateParameters bgwPrivateParameters) {
		return new ProtocolData(participants,N, bgwPrivateParameters);
	}

	public static ProtocolData init() {
		return new ProtocolData(null,null, null);
	}
	
	public ProtocolData withParticipants(Map<ActorRef,Integer> participants) { 
		return new ProtocolData(participants, N, bgwPrivateParameters);
	}
}
