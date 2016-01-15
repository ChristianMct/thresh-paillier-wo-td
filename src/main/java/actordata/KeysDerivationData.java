package actordata;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import protocol.KeysDerivationParameters.KeysDerivationPrivateParameters;
import protocol.KeysDerivationParameters.KeysDerivationPublicParameters;
import scala.math.BigInt;
import akka.actor.ActorRef;

public class KeysDerivationData extends Data{

	public final BigInteger N;
	public final BigInteger Rpoint;
	public final BigInteger v;
	public final BigInteger fi;
	public final KeysDerivationPrivateParameters keysDerivationPrivateParameters;
	private final Map<Integer, KeysDerivationPublicParameters> publicParameters;
	public final Map<Integer, BigInteger> thetas;
	public final Map<Integer, BigInteger> verificationKeys;

	private KeysDerivationData(Map<ActorRef, Integer> participants,
								BigInteger N,
								BigInteger Rpoint,
								BigInteger v,
								BigInteger fi,
								KeysDerivationPrivateParameters keysDerivationPrivateParameters,
								Map<Integer, KeysDerivationPublicParameters> publicParameters,
								Map<Integer, BigInteger> thetas,
								Map<Integer, BigInteger> verificationKeys) {
		super(participants);
		
		this.N = N;
		this.Rpoint = Rpoint;
		this.v = v;
		this.fi = fi;
		this.keysDerivationPrivateParameters = keysDerivationPrivateParameters;
		this.publicParameters = publicParameters != null ? new HashMap<Integer, KeysDerivationPublicParameters>(publicParameters) : null;
		this.thetas = thetas != null ? new HashMap<Integer, BigInteger>(thetas) : null;
		this.verificationKeys = verificationKeys != null ? new HashMap<Integer, BigInteger>(verificationKeys) : null;
	}
	
	public boolean hasBetaiRiOf(Collection<Integer> is) {
		return is.stream().allMatch(i->this.publicParameters.containsKey(i));
	}
	
	public boolean hasThetaiOf(Collection<Integer> is){
		return is.stream().allMatch(i -> this.thetas.containsKey(i));
	}
	
	public boolean hasVerifKeyOf(Collection<Integer> is){
		return is.stream().allMatch(i -> this.verificationKeys.containsKey(i));
	}
	
	public Stream<Entry<Integer, KeysDerivationPublicParameters>> publicParameters() {
		return this.publicParameters.entrySet().stream();
	}
	
	public KeysDerivationData withParticipants(Map<ActorRef,Integer> participants) {
		return new KeysDerivationData(participants, N, Rpoint, v, fi, keysDerivationPrivateParameters, publicParameters, thetas, verificationKeys);
	}

	public KeysDerivationData withN(BigInteger N) {
		return new KeysDerivationData(participants, N, Rpoint, v, fi, keysDerivationPrivateParameters, publicParameters, thetas, verificationKeys);
	}
	
	public KeysDerivationData withPrivateParameters(KeysDerivationPrivateParameters keysDerivationPrivateParameters) {
		return new KeysDerivationData(participants, N, Rpoint, v, fi, keysDerivationPrivateParameters, publicParameters, thetas, verificationKeys);
	}
	
	public KeysDerivationData withFi(BigInteger fi) {
		return new KeysDerivationData(participants, N, Rpoint, v, fi, keysDerivationPrivateParameters, publicParameters, thetas, verificationKeys);
	}
	
	public KeysDerivationData withNewPublicParametersFor(int j, KeysDerivationPublicParameters keysDerivationPublicParameters) {
		if (this.publicParameters != null && this.publicParameters.containsKey(j)) {
			return this;
		}
	
		HashMap<Integer, KeysDerivationPublicParameters> newBetasMap = this.publicParameters != null ? 
				new HashMap<Integer, KeysDerivationPublicParameters>(this.publicParameters) : new HashMap<Integer, KeysDerivationPublicParameters>();

		
		newBetasMap.put(j, keysDerivationPublicParameters);
		return new KeysDerivationData(participants, N, Rpoint, v,fi, keysDerivationPrivateParameters, newBetasMap, thetas, verificationKeys);
	}
	
	public KeysDerivationData withRPoint(BigInteger RPoint) {
		return new KeysDerivationData(participants, N, RPoint, v,fi, keysDerivationPrivateParameters, publicParameters, thetas, verificationKeys);
	}
	
	public KeysDerivationData withNewV(BigInteger v) {
		return new KeysDerivationData(participants, N, Rpoint, v,fi, keysDerivationPrivateParameters, publicParameters, thetas, verificationKeys);
	}
	
	public KeysDerivationData withNewThetaFor(int j, BigInteger theta) {
		if (this.thetas != null && this.thetas.containsKey(j)){
			return this;
		}
		
		HashMap<Integer, BigInteger> newThetaMap = this.thetas != null ? new HashMap<Integer, BigInteger>(thetas) :
																		new HashMap<Integer, BigInteger>();
		newThetaMap.put(j, theta);
		return new KeysDerivationData(participants, N, Rpoint, v,fi, keysDerivationPrivateParameters, publicParameters, newThetaMap, verificationKeys);
	}
	
	public KeysDerivationData withNewVerificationKeyFor(int j, BigInteger newVerifKey) {
		if (this.verificationKeys != null && this.verificationKeys.containsKey(j)){
			return this;
		}
		
		HashMap<Integer, BigInteger> newVKMap = this.verificationKeys != null ? new HashMap<Integer, BigInteger>(verificationKeys) :
																		new HashMap<Integer, BigInteger>();
		newVKMap.put(j, newVerifKey);
		return new KeysDerivationData(participants, N, Rpoint, v,fi, keysDerivationPrivateParameters, publicParameters, thetas, newVKMap);
	}

	
	public static KeysDerivationData init() {
		return new KeysDerivationData(null, null, null, null, null, null, null, null,null);
	}

}
