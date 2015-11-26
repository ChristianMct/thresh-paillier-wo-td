package protocol;

import java.math.BigInteger;
import java.security.SecureRandom;

public class ProtocolParameters {
	
	public final BigInteger P;
	public final BigInteger Pp;
	public final BigInteger g;
	public final BigInteger h;
	public final int t;
	public final int k;

	private ProtocolParameters(BigInteger P, BigInteger Pp, BigInteger g, BigInteger h, int t, int k) {
		this.P = P;
		this.Pp = Pp;
		this.g = g;
		this.h = h;
		this.t = t;
		this.k = k;
	}
	
	public static ProtocolParameters gen(int k, SecureRandom sr) {
		BigInteger z = BigInteger.ZERO;
		BigInteger Pp = BigInteger.probablePrime(k, sr);
		return new ProtocolParameters(z, Pp, z, z, 1, k);
	}
}
