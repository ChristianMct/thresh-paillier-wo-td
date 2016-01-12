package protocol;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

public class ProtocolParameters {
	
	public final BigInteger P;
	public final BigInteger Pp;
	public final BigInteger g;
	public final BigInteger gp;
	public final BigInteger h;
	public final int t;
	public final int k;
	public final int K;
	public final int n;

	private ProtocolParameters(BigInteger P, BigInteger Pp, BigInteger g, BigInteger gp, BigInteger h, int t, int k, int K, int n) {
		this.P = P;
		this.Pp = Pp;
		this.g = g;
		this.gp = gp;
		this.h = h;
		this.t = t;
		this.k = k;
		this.K = K;
		this.n = n;
	}
	
	public static ProtocolParameters gen(int k, int t, int n,SecureRandom sr) {
		BigInteger Pp = new BigInteger("43661658899963406608383637340196977962122829941011847601528077076668470132290990200968125083014971338133392588979");
		BigInteger P = null;//new BigInteger("8208391873193120442376123819957031856879092028910227349087278490413672384870706157782007515606814611569077806728053");
		BigInteger g = null;//new BigInteger("372223741455328186459978054469800111530408682317511647391519328038886770606432351748879444689126472561350420501");
		BigInteger h = null;//new BigInteger("41882225770559995570059604591701087241268749365485546402758167539038856425273885483351238739958821766359906783652");
		return new ProtocolParameters(P, Pp, g, null,h, t, k, 1000, n);
	}
	
	public static ProtocolParameters generate(int k, int t, int n, Random random) {
		BigInteger Pp = BigInteger.probablePrime(k, random);
		return new ProtocolParameters(null, Pp, null, null, null, t, k, 1000, n);
	}
	
}
