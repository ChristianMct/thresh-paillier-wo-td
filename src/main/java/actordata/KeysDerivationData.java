package actordata;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import protocol.KeysDerivationParameters.KeysDerivationPrivateParameters;
import protocol.KeysDerivationParameters.KeysDerivationPublicParameters;
import math.Polynomial;
import math.PolynomialMod;
import akka.actor.ActorRef;

public class KeysDerivationData extends Data{

	public final BigInteger phi;
	public final BigInteger N;
	public final KeysDerivationPrivateParameters keysDerivationPrivateParameters;
	private final Map<Integer, KeysDerivationPublicParameters> publicParameters;

	private KeysDerivationData(Map<ActorRef, Integer> participants,
								BigInteger N,
								BigInteger phi,
								KeysDerivationPrivateParameters keysDerivationPrivateParameters,
								Map<Integer, KeysDerivationPublicParameters> betas) {
		super(participants);
		
		this.N = N;
		this.phi = phi;
		this.keysDerivationPrivateParameters = keysDerivationPrivateParameters;
		this.publicParameters = betas != null ? new HashMap<Integer, KeysDerivationPublicParameters>(betas) : null;
	}
	
	public boolean hasBetaiRiOf(Collection<Integer> is) {
		return is.stream().allMatch(i->this.publicParameters.containsKey(i));
	}
	
	public KeysDerivationData withParticipants(Map<ActorRef,Integer> participants) {
		return new KeysDerivationData(participants, N, phi, keysDerivationPrivateParameters, publicParameters);
	}

	public KeysDerivationData withN(BigInteger N, BigInteger phi) {
		return new KeysDerivationData(participants, N, phi, keysDerivationPrivateParameters, publicParameters);
	}
	
	public KeysDerivationData withPrivateParameters(KeysDerivationPrivateParameters keysDerivationPrivateParameters) {
		return new KeysDerivationData(participants, N, phi, keysDerivationPrivateParameters, publicParameters);
	}
	
	public KeysDerivationData withNewPublicParametersFor(int j, KeysDerivationPublicParameters keysDerivationPublicParameters) {
		if (this.publicParameters != null && this.publicParameters.containsKey(j)) {
			return this;
		}
	
		HashMap<Integer, KeysDerivationPublicParameters> newBetasMap = this.publicParameters != null ? 
				new HashMap<Integer, KeysDerivationPublicParameters>(this.publicParameters) : new HashMap<Integer, KeysDerivationPublicParameters>();

		
		newBetasMap.put(j, keysDerivationPublicParameters);
		return new KeysDerivationData(participants, N, phi, keysDerivationPrivateParameters, newBetasMap);
	}
	
	public static KeysDerivationData init() {
		return new KeysDerivationData(null, null, null, null, null);
	}

}
