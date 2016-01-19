package actordata;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import akka.actor.ActorRef;
import protocol.BGWParameters.BGWPrivateParameters;
import protocol.BGWParameters.BGWPublicParameters;

/** Represents the state data of the BGW protocol Actor's FSM.
 * <p>
 * This is an immutable object type in order to comply to the Akka good practices regarding FSMs.
 * @author Christian Mouchet
 */
public class BGWData extends Data{
	
	/** The BGW private parameters selected by the actor in the BGW protocol (p<sub>i</sub>, q<sub>i</sub>, ...)*/
	public final BGWPrivateParameters bgwPrivateParameters;
	
	/** Collection of the recieved shares of N*/
	public final Map<Integer,BigInteger> Ns;

	private final Map<Integer,BGWPublicParameters> bgwPublicParameters;
	
	private BGWData(Map<ActorRef,Integer> participants,
					BGWPrivateParameters bgwPrivateParameters,
					Map<Integer,BGWPublicParameters> bgwPublicParameters,
					Map<Integer,BigInteger> Ns) {
		super(participants);
		this.bgwPrivateParameters = bgwPrivateParameters;
		this.bgwPublicParameters = new HashMap<Integer,BGWPublicParameters>(bgwPublicParameters);
		this.Ns = new HashMap<Integer,BigInteger>(Ns);
	}
	
	public static BGWData init() {
		return new BGWData(null,
							null,
							new HashMap<Integer,BGWPublicParameters>(),
							new HashMap<Integer,BigInteger>());
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
	
	
	public BGWData withParticipants(Map<ActorRef,Integer> participants) {
		return new BGWData(new HashMap<ActorRef, Integer>(participants),
							bgwPrivateParameters,
							bgwPublicParameters,
							Ns);
	}
	
	public BGWData withPrivateParameters(BGWPrivateParameters params) {
		return new BGWData(participants, params, bgwPublicParameters,Ns);
	}
	
	public BGWData withNewShare(BGWPublicParameters share, int fromId) {
		if (bgwPublicParameters.containsKey(fromId))
			return this;
		
		Map<Integer, BGWPublicParameters> newMap = new HashMap<Integer,BGWPublicParameters>(bgwPublicParameters);
		newMap.put(fromId, share);
		return new BGWData(participants, bgwPrivateParameters, newMap,Ns);
	}
	
	public BGWData withNewNi(BigInteger Ni, int fromId) {
		if(Ns.containsKey(fromId))
			return this;
		
		Map<Integer,BigInteger> newNs = new HashMap<Integer, BigInteger>(Ns);
		newNs.put(fromId, Ni);
		return new BGWData(participants, bgwPrivateParameters, bgwPublicParameters , newNs);
	}
	
	public BGWData withCandidateN(BigInteger candidateN) {
		return new BGWData(participants, bgwPrivateParameters, bgwPublicParameters, Ns);
	}
	
}