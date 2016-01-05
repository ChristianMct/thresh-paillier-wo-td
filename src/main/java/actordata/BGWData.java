package actordata;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import akka.actor.ActorRef;
import protocol.BGWParameters.BGWPrivateParameters;
import protocol.BGWParameters.BGWPublicParameters;

public class BGWData extends Data{
	public final BGWPrivateParameters bgwPrivateParameters;
	private final Map<Integer,BGWPublicParameters> bgwPublicParameters;
	public final Map<Integer,BigInteger> Ns;
	public final BigInteger candidateN;
	private final Map<Integer, BigInteger> Qs;
	
	private BGWData(Map<ActorRef,Integer> participants,
					BGWPrivateParameters bgwPrivateParameters,
					Map<Integer,BGWPublicParameters> bgwPublicParameters,
					Map<Integer,BigInteger> Ns,
					BigInteger candidateN,
					Map<Integer, BigInteger> Qs) {
		super(participants);
		this.bgwPrivateParameters = bgwPrivateParameters;
		this.bgwPublicParameters = new HashMap<Integer,BGWPublicParameters>(bgwPublicParameters);
		this.Ns = new HashMap<Integer,BigInteger>(Ns);
		this.candidateN = candidateN;
		this.Qs = new HashMap<Integer, BigInteger>(Qs);
	}
	
	public static BGWData init() {
		return new BGWData(null,
							null,
							new HashMap<Integer,BGWPublicParameters>(),
							new HashMap<Integer,BigInteger>(),
							null,
							new HashMap<Integer, BigInteger>());
	}
	
	public boolean hasShareOf(Collection<Integer> is) {
		return is.stream().allMatch(i->bgwPublicParameters.containsKey(i));
	}
	
	public Stream<Map.Entry<Integer,BGWPublicParameters>> shares() {
		return bgwPublicParameters.entrySet().stream();
	}
	
	public boolean hasNiOf(Collection<Integer> is) {
		return is.stream().allMatch(i->Ns.containsKey(i));
	}
	
	public Stream<Map.Entry<Integer, BigInteger>> nis() {
		return Ns.entrySet().stream();
	}
	
	public boolean hasQiOf(Collection<Integer> is) {
		return is.stream().allMatch(i->Qs.containsKey(i));
	}
	
	public Stream<Map.Entry<Integer, BigInteger>> qis() {
		return this.Qs.entrySet().stream();
	}
	
	public Map<Integer,BigInteger> qiss() {
		return this.Qs;
	}
	
	public BGWData withParticipants(Map<ActorRef,Integer> participants) {
		return new BGWData(new HashMap<ActorRef, Integer>(participants),
							bgwPrivateParameters,
							bgwPublicParameters,
							Ns,
							candidateN,
							Qs);
	}
	
	public BGWData withPrivateParameters(BGWPrivateParameters params) {
		return new BGWData(participants, params, bgwPublicParameters,Ns, candidateN,Qs);
	}
	
	public BGWData withNewShare(BGWPublicParameters share, int fromId) {
		if (bgwPublicParameters.containsKey(fromId))
			return this;
		
		Map<Integer, BGWPublicParameters> newMap = new HashMap<Integer,BGWPublicParameters>(bgwPublicParameters);
		newMap.put(fromId, share);
		return new BGWData(participants, bgwPrivateParameters, newMap,Ns,candidateN,Qs);
	}
	
	public BGWData withNewNi(BigInteger Ni, int fromId) {
		if(Ns.containsKey(fromId))
			return this;
		
		Map<Integer,BigInteger> newNs = new HashMap<Integer, BigInteger>(Ns);
		newNs.put(fromId, Ni);
		return new BGWData(participants, bgwPrivateParameters, bgwPublicParameters , newNs, candidateN,Qs);
	}
	
	public BGWData withCandidateN(BigInteger candidateN) {
		return new BGWData(participants, bgwPrivateParameters, bgwPublicParameters, Ns, candidateN,Qs);
	}
	
	public BGWData withNewQi(BigInteger Qi, int fromId) {
		if(Qs.containsKey(fromId))
			return this;
		
		Map<Integer, BigInteger> newMap = new HashMap<Integer, BigInteger>(Qs);
		newMap.put(fromId, Qi);
		return new BGWData(participants, bgwPrivateParameters, bgwPublicParameters, Ns, candidateN, newMap);
	}
	
}