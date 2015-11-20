package protocol;

import java.math.BigInteger;

public class BGWParameters {
	
	public static class BGWPrivateParameters {
		public final BigInteger p;
		public final BigInteger q;
		
		private BGWPrivateParameters(BigInteger p, BigInteger q) {
			this.p = p;
			this.q = q;
		}
		
		public static BGWPrivateParameters gen(ProtocolParameters protParams) {
			return null;
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
		
		public static BGWPublicParameters genFor(int j) {
			return null;
		}
	}
}
