package actordata;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import math.PolynomialSharing;
import math.PolynomialSharing.Share;
import akka.actor.ActorRef;

public class KeysDerivationData extends Data{

	public final BigInteger phi;
	public final BigInteger N;
	public final PolynomialSharing betaiSharing;
	public final PolynomialSharing RiSharing;
	private final Map<Integer, Share> betas;
	private final Map<Integer, Share> Rs;

	private KeysDerivationData(Map<ActorRef, Integer> participants,
								BigInteger N,
								BigInteger phi,
								PolynomialSharing betaiSharing,
								PolynomialSharing RiSharing,
								Map<Integer, Share> betas,
								Map<Integer, Share> Rs) {
		super(participants);
		
		this.N = N;
		this.phi = phi;
		this.betaiSharing = betaiSharing;
		this.RiSharing = RiSharing;
		this.betas = betas != null ? new HashMap<Integer, Share>(betas) : null;
		this.Rs = Rs != null ?  new HashMap<Integer, Share>(Rs) : null;
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
	
	public KeysDerivationData withSharings(PolynomialSharing betaiSharing, PolynomialSharing RiSharing) {
		return new KeysDerivationData(participants, N, phi, betaiSharing, RiSharing, betas, Rs);
	}
	
	public KeysDerivationData withNewBetaiRiSharesFor(int i, Share betai, Share Ri) {
		if (	(this.betas != null && this.betas.containsKey(i)) ||
				(this.Rs != null && this.Rs.containsKey(i))) {
			return this;
		}
	
		HashMap<Integer, Share> newBetasMap = this.betas != null ? 
				new HashMap<Integer, Share>(this.betas) : new HashMap<Integer, Share>();
		HashMap<Integer, Share> newRsMap = this.Rs != null ? 
				new HashMap<Integer, Share>(this.Rs) : new HashMap<Integer, Share>();
		
		newBetasMap.put(i, betai);
		newRsMap.put(i, Ri);
		return new KeysDerivationData(participants, N, phi, betaiSharing, RiSharing, newBetasMap, newRsMap);
	}
	
	public static KeysDerivationData init() {
		return new KeysDerivationData(null, null, null, null, null, null, null);
	}

}
