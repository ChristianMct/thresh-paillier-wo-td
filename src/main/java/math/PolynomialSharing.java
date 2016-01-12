package math;

import java.math.BigInteger;
import java.util.Random;

public class PolynomialSharing {
	
	public final Polynomial fi;
	public final Polynomial hi;
	
	private PolynomialSharing(Polynomial f, Polynomial h) {
		this.fi = f;
		this.hi = h;
	}
	
	public Share getShare(int i) {
		return new Share(fi.eval(i), hi.eval(i));
	}
	
	public static class Share {
		public final BigInteger fij;
		public final BigInteger hij;
		public Share(BigInteger fij, BigInteger hij){
			this.fij = fij;
			this.hij = hij;
		}
		
		public BigInteger getSum(){
			return fij.add(hij);
		}
	}
	
	public static class PolynomialSharingFactory {
		private final int bitLength;
		private final Random random;
		public PolynomialSharingFactory(int bitlength, Random random) {
			this.bitLength = bitlength;
			this.random = random;
		}
		
		public PolynomialSharing share(BigInteger sharee, int degree) {
			Polynomial f = new Polynomial(degree, sharee, bitLength, random);
			Polynomial h = new Polynomial(degree, BigInteger.ZERO, bitLength, random);
			return new PolynomialSharing(f, h);
		}
		
		public PolynomialSharing shareMod(BigInteger sharee, int degree, BigInteger mod) {
			PolynomialMod f = new PolynomialMod(degree, mod, sharee, bitLength, random);
			PolynomialMod h = new PolynomialMod(degree, mod, BigInteger.ZERO, bitLength, random);
			return new PolynomialSharing(f, h);
		}
	}
}
