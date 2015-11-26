package protocol;

import java.math.BigInteger;
import java.security.SecureRandom;

import math.Polynomial;

public class BGWParameters {
	
	public static class BGWPrivateParameters {
		public final BigInteger p;
		public final BigInteger q;
		public final Polynomial f;
		public final Polynomial fp;
		public final Polynomial g;
		public final Polynomial gp;
		public final Polynomial h;
		public final Polynomial hp;
		
		private BGWPrivateParameters(BigInteger p, BigInteger q,Polynomial f,Polynomial fp,Polynomial g,Polynomial gp,Polynomial h,Polynomial hp ) {
			this.p = p;
			this.q = q;
			this.f = f;
			this.fp = fp;
			this.g = g;
			this.gp = gp;
			this.h = h;
			this.hp = hp;
		}
		
		public static BGWPrivateParameters genFor(int i,ProtocolParameters protParam, SecureRandom sr) {
			
			
			// p and q generation
			BigInteger p;
			BigInteger q;
			BigInteger min = BigInteger.ONE.shiftLeft(protParam.k-1);
			BigInteger max = BigInteger.ONE.shiftLeft(protParam.k).subtract(BigInteger.ONE);
			BigInteger four = BigInteger.valueOf(4);
			BigInteger modFourTarget = i == 0 ? BigInteger.valueOf(3) : BigInteger.ZERO;
			
			do {
				p = min.add(new BigInteger(protParam.k-1, sr));
				q = min.add(new BigInteger(protParam.k-1, sr));
			} while(p.compareTo(max) > 0 ||
					q.compareTo(max) > 0 ||
					! p.mod(four).equals(modFourTarget) ||
					! q.mod(four).equals(modFourTarget));
			
			// polynomials generation
			BigInteger pp = new BigInteger(protParam.k, sr).mod(protParam.Pp);
			BigInteger qp = new BigInteger(protParam.k, sr).mod(protParam.Pp);
			BigInteger c0p = new BigInteger(protParam.k, sr).mod(protParam.Pp); //Correct ?
			
			Polynomial f = new Polynomial(protParam.t, protParam.Pp, p, sr, protParam.k);
			Polynomial fp = new Polynomial(protParam.t, protParam.Pp, pp , sr, protParam.k);
			Polynomial g = new Polynomial(protParam.t, protParam.Pp, q, sr, protParam.k);
			Polynomial gp = new Polynomial(protParam.t, protParam.Pp, qp, sr, protParam.k);
			
			Polynomial h = new Polynomial(2*protParam.t, protParam.Pp, BigInteger.ZERO, sr, protParam.k); 
			Polynomial hp = new Polynomial(2*protParam.t, protParam.Pp, c0p, sr, protParam.k);
			
			return new BGWPrivateParameters(p,q,f,fp,g,gp,h,hp);
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
		
		public boolean isCorrect(ProtocolParameters protocolParameters,int i,int j) {
			return true;
		}
		
		public static BGWPublicParameters genFor(int j, BGWPrivateParameters bgwPrivParam, SecureRandom sr) {
			BigInteger pj = bgwPrivParam.f.eval(j);
			BigInteger ppj = bgwPrivParam.fp.eval(j);
			BigInteger qj = bgwPrivParam.g.eval(j);
			BigInteger qpj = bgwPrivParam.gp.eval(j);
			BigInteger hj = bgwPrivParam.h.eval(j);
			BigInteger hpj = bgwPrivParam.hp.eval(j);
			return new BGWPublicParameters(pj, ppj, qj, qpj, hj, hpj);
		}
	}
}
