package protocol;

import java.math.BigInteger;
import java.util.Random;

import math.IntegersUtils;

/**
 * Provides structure for, and generation of, the parameters of distributed Threshold Paillier key generation protocol .
 * @author Christian Mouchet
 */
public class ProtocolParameters {
	
	/** Large prime P' used for secret sharing with polynomials over the integer mod P' */
	public final BigInteger P;
	
	/**The number of parties in the key generation protocol*/
	public final int n;
	
	/** The maximum number of parties an adversary can corrupt without breaking the security of the protocol*/
	public final int t;
	
	/** The bitlength of p and q, so that N has a security of 2k */
	public final int k;
	
	/** The security of the statistical hiding of &Phi;(N) with &Beta; and R */
	public final int K;

	private ProtocolParameters(BigInteger Pp, int t, int k, int K, int n) {
		this.P = Pp;
		this.t = t;
		this.k = k;
		this.K = K;
		this.n = n;
	}
	
	/** Generates the parameters for n parties, t of which can be corrupted without breaking the security.
	 * @param k the bitlength of p and q, such that N has a security of 2k
	 * @param n The number of parties
	 * @param t The maximum number of parties an adversary can corrupt without breaking the security of the protocol. Must be < <code>n/2</code>
	 * @param random a randomness generator
	 * @return the protocol parameters structure
	 */
	public static ProtocolParameters gen(int k, int n, int t, Random random) {
		
		if (k < 16)
			throw new IllegalArgumentException("k should be at least 16");
		if (n < 3)
			throw new IllegalArgumentException("n should be at least 3");
		if (t > n/2)
			throw new IllegalArgumentException("t should be smaller than n/2");
		if (random == null)
			throw new IllegalArgumentException("random cannot be null");
		
		int fact = k < 512 ? 4 : 1;
		BigInteger np = BigInteger.valueOf(n);
		BigInteger three = BigInteger.valueOf(3);
		BigInteger maxN = BigInteger.valueOf(2).pow(fact*k-1); //TODO: the formula for min Pp given in the paper does not work. Add a factor on the bitlen is a quick, dirty fix
		BigInteger minPp = np.multiply(three.multiply(maxN)).pow(2);
		BigInteger maxPp = BigInteger.valueOf(2).pow(minPp.bitLength()+1);
		
		System.out.println("Generating P' ...");
		BigInteger Pp = IntegersUtils.pickPrimeInRange(minPp, maxPp, random);
		return new ProtocolParameters(Pp, t, k, 1000, n);
	}
	
}
