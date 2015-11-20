package protocol;

import java.math.BigInteger;
import java.security.SecureRandom;

public class BGWParameters {
	
	public static class BGWPrivateParameters {
		public final BigInteger p;
		public final BigInteger q;
		
		private BGWPrivateParameters(BigInteger p, BigInteger q) {
			this.p = p;
			this.q = q;
		}
		
		public static BGWPrivateParameters genFor(int i,ProtocolParameters protParams, SecureRandom sr) {
			BigInteger a;
			BigInteger b;
			BigInteger min = BigInteger.ONE.shiftLeft(protParams.k-1);
			BigInteger max = BigInteger.ONE.shiftLeft(protParams.k).subtract(BigInteger.ONE);
			BigInteger four = BigInteger.valueOf(4);
			BigInteger modFourTarget = i == 0 ? BigInteger.valueOf(3) : BigInteger.ZERO;
			
			do {
				a = min.add(new BigInteger(protParams.k-1, sr));
				b = min.add(new BigInteger(protParams.k-1, sr));
			} while(a.compareTo(max) > 0 ||
					b.compareTo(max) > 0 ||
					! a.mod(four).equals(modFourTarget) ||
					! b.mod(four).equals(modFourTarget));
			return new BGWPrivateParameters(a,b);
		}
	}
	
	public static class BGWPublicParameters {
		public final BigInteger pj;
		public final BigInteger ppj;
		public final BigInteger qj;
		public final BigInteger qpj;
		public final BigInteger hj;
		public final BigInteger hpj;
		
		private BGWPublicParameters(BigInteger pj,BigInteger ppj, BigInteger qj, BigInteger qpj, BigInteger hj, BigInteger hpj) {
			this.pj = pj;
			this.ppj = ppj;
			this.qj = qj;
			this.qpj = qpj;
			this.hj = hj;
			this.hpj = hpj;
		}
		
		public static BGWPublicParameters genFor(int j, SecureRandom sr) {
			return null;
		}
	}
}
