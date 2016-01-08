package math;

import java.math.BigInteger;
import java.util.Random;

public class PolynomialSharing {

	public final Polynomial f;
	public final Polynomial h;
	
	private PolynomialSharing(Polynomial f, Polynomial h) {
		this.f = f;
		this.h = h;
	}
	
	public Share getShare(int i) {
		return new Share(f.eval(i), h.eval(i));
	}
	
	public static class Share {
		public final BigInteger f;
		public final BigInteger h;
		
		public Share(BigInteger f, BigInteger h) {
			this.f = f;
			this.h = h;
		}
	}
	
	public static class PolynomialSharingFactory {
		
		private final int degree;
		private final int numbit;
		private final Random random;
		
		public PolynomialSharingFactory(int degree, int numbit, Random random) {
			this.degree = degree;
			this.numbit = numbit;
			this.random = random;
		}
		
		public PolynomialSharing share(BigInteger sharee, BigInteger mod) {
			PolynomialMod f = new PolynomialMod(degree, mod, sharee, numbit,random);
			PolynomialMod h = new PolynomialMod(degree, mod, BigInteger.ZERO, numbit, random);
			return new PolynomialSharing(f, h);
		}
		
		public PolynomialSharing share(BigInteger sharee) {
			Polynomial f = new Polynomial(degree, sharee, numbit, random);
			Polynomial h = new Polynomial(degree, sharee, numbit, random);
			return new PolynomialSharing(f,h);
		}
	}
}
