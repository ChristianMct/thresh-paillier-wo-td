package actordata;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import math.Polynomial;
import math.PolynomialMod;
import akka.actor.ActorRef;

public class KeysDerivationData extends Data{

	public final BigInteger phi;
	public final BigInteger N;
	public final PolynomialMod betaiSharing;
	public final Polynomial RiSharing;
	private final Map<Integer, BigInteger> betas;
	private final Map<Integer, BigInteger> Rs;

	private KeysDerivationData(Map<ActorRef, Integer> participants,
								BigInteger N,
								BigInteger phi,
								PolynomialMod betaiSharing,
								Polynomial RiSharing,
								Map<Integer, BigInteger> betas,
								Map<Integer, BigInteger> Rs) {
		super(participants);
		
		this.N = N;
		this.phi = phi;
		this.betaiSharing = betaiSharing;
		this.RiSharing = RiSharing;
		this.betas = betas != null ? new HashMap<Integer, BigInteger>(betas) : null;
		this.Rs = Rs != null ?  new HashMap<Integer, BigInteger>(Rs) : null;
	}
	
	public boolean hasBetaiRiOf(Collection<Integer> is) {
		return is.stream().allMatch(i->this.betas.containsKey(i) && this.Rs.containsKey(i));
	}
	
	public KeysDerivationData withParticipants(Map<ActorRef,Integer> participants) {
		return new KeysDerivationData(participants, N, phi, betaiSharing, RiSharing, betas, Rs);
	}

	public KeysDerivationData withN(BigInteger N, BigInteger phi) {
		return new KeysDerivationData(participants, N, phi, betaiSharing, RiSharing, betas, Rs);
	}
	
	public KeysDerivationData withSharings(PolynomialMod betaiSharing, Polynomial RiSharing) {
		return new KeysDerivationData(participants, N, phi, betaiSharing, RiSharing, betas, Rs);
	}
	
	public KeysDerivationData withNewBetaiRiSharesFor(int i, BigInteger betai, BigInteger Ri) {
		if (	(this.betas != null && this.betas.containsKey(i)) ||
				(this.Rs != null && this.Rs.containsKey(i))) {
			return this;
		}
	
		HashMap<Integer, BigInteger> newBetasMap = this.betas != null ? 
				new HashMap<Integer, BigInteger>(this.betas) : new HashMap<Integer, BigInteger>();
		HashMap<Integer, BigInteger> newRsMap = this.Rs != null ? 
				new HashMap<Integer, BigInteger>(this.Rs) : new HashMap<Integer, BigInteger>();
		
		newBetasMap.put(i, betai);
		newRsMap.put(i, Ri);
		return new KeysDerivationData(participants, N, phi, betaiSharing, RiSharing, newBetasMap, newRsMap);
	}
	
	public static KeysDerivationData init() {
		return new KeysDerivationData(null, null, null, null, null, null, null);
	}

}
