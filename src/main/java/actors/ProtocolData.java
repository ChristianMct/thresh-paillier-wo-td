package actors;

import java.math.BigInteger;
import java.util.Map;

import akka.actor.ActorRef;

public class ProtocolData extends Data {
	
	public final BigInteger N;
	
	private ProtocolData(Map<ActorRef,Integer> participants, BigInteger N) {
		super(participants);
		this.N = N;
	}
	
	public ProtocolData withNewN(BigInteger N) {
		return new ProtocolData(participants,N);
	}

	public static ProtocolData init() {
		return new ProtocolData(null,null);
	}
}
