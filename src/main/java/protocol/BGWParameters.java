package protocol;

import java.math.BigInteger;
import java.security.SecureRandom;

import math.PolynomialMod;

public class BGWParameters {
	
	public static class BGWPrivateParameters {
		public final BigInteger p;
		public final BigInteger q;
		public final PolynomialMod f;
		public final PolynomialMod g;
		public final PolynomialMod h;
		//public final PolynomialMod fp;
		//public final PolynomialMod gp;
		//public final PolynomialMod hp;
		private final int i;
		
		private BGWPrivateParameters(int i, BigInteger p, BigInteger q,PolynomialMod f, PolynomialMod g, PolynomialMod h/*, PolynomialMod fp,PolynomialMod gp,PolynomialMod hp*/) {
			this.p = p;
			this.q = q;
			this.f = f;
			this.g = g;
			this.h = h;
			this.i = i;
			//this.fp = fp;
			//this.gp = gp;
			//this.hp = hp;
		}
		
		public static BGWPrivateParameters genFor(int i,ProtocolParameters protParam, SecureRandom sr) {
			
			
			// p and q generation
			BigInteger p;
			BigInteger q;
			BigInteger min = BigInteger.ONE.shiftLeft(protParam.k-1);
			BigInteger max = BigInteger.ONE.shiftLeft(protParam.k).subtract(BigInteger.ONE);
			BigInteger four = BigInteger.valueOf(4);
			BigInteger modFourTarget = i == 1 ? BigInteger.valueOf(3) : BigInteger.ZERO;
			
			do {
				p = min.add(new BigInteger(protParam.k-1, sr));
				q = min.add(new BigInteger(protParam.k-1, sr));
			} while(p.compareTo(max) > 0 ||
					q.compareTo(max) > 0 ||
					! p.mod(four).equals(modFourTarget) ||
					! q.mod(four).equals(modFourTarget));
			
			// Injection of stuff
			p = i == 1 ? new BigInteger("2905983851") :  new BigInteger("46406792");
			q = i == 1 ? new BigInteger("23") :  new BigInteger("431972452");
			
			// polynomials generation
			
			PolynomialMod f = new PolynomialMod(protParam.t, protParam.Pp, p, protParam.k, sr);
			PolynomialMod g = new PolynomialMod(protParam.t, protParam.Pp, q, protParam.k, sr);
			PolynomialMod h = new PolynomialMod(2*protParam.t, protParam.Pp, BigInteger.ZERO, protParam.k, sr); 
			
//			BigInteger pp = new BigInteger(protParam.k, sr).mod(protParam.Pp);
//			BigInteger qp = new BigInteger(protParam.k, sr).mod(protParam.Pp);
//			BigInteger c0p = new BigInteger(protParam.k, sr).mod(protParam.Pp); //Correct ?			
//			PolynomialMod fp = new PolynomialMod(protParam.t, protParam.Pp, pp , protParam.k, sr);
//			PolynomialMod gp = new PolynomialMod(protParam.t, protParam.Pp, qp, protParam.k, sr);			
//			PolynomialMod hp = new PolynomialMod(2*protParam.t, protParam.Pp, c0p, protParam.k, sr);
			
			
			return new BGWPrivateParameters(i, p, q, f, g, h/*, fp, gp, hp*/);
		}
		
		public String toString() {
			return String.format("BGWPrivateParameters[%d]", i);
		}
		
	}
	
	public static class BGWPublicParameters {
		private final int i;
		private final int j;
		public final BigInteger pj;
		public final BigInteger qj;
		public final BigInteger hj;
//		public final BigInteger qpj;
//		public final BigInteger ppj;
//		public final BigInteger hpj;
		
		private BGWPublicParameters(int i, int j, BigInteger pj, BigInteger qj, BigInteger hj/*, BigInteger ppj, BigInteger qpj, BigInteger hpj,*/) {
			this.pj = pj;
			this.qj = qj;
			this.hj = hj;
			this.j = j;
			this.i = i;
//			this.ppj = ppj;
//			this.qpj = qpj;
//			this.hpj = hpj;
		}
		
		public boolean isCorrect(ProtocolParameters protocolParameters,int i,int j) {
			//BigInteger gPowPi = protocolParameters.g.modPow(pj, protocolParameters.Pp);
			//BigInteger hPowPpi = protocolParameters.h.modPow(ppj, protocolParameters.Pp);
			
			return true;
		}
		
		public static BGWPublicParameters genFor(int j, BGWPrivateParameters bgwPrivParam, SecureRandom sr) {
			int i = bgwPrivParam.i;
			BigInteger pj = bgwPrivParam.f.eval(j);
			BigInteger qj = bgwPrivParam.g.eval(j);
			BigInteger hj = bgwPrivParam.h.eval(j);
//			BigInteger ppj = bgwPrivParam.fp.eval(j);
//			BigInteger qpj = bgwPrivParam.gp.eval(j);
//			BigInteger hpj = bgwPrivParam.hp.eval(j);
			return new BGWPublicParameters(i,j, pj, qj, hj/*, ppj, qpj,  hpj*/);
		}
		
		public String toString() {
			return String.format("BGWPublicParameters[%d][%d]", this.i, this.j);
		}
	}
}
