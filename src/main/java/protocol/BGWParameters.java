package protocol;

import java.math.BigInteger;
import java.util.Random;

import math.PolynomialMod;

/**
 * Provide structures for, and generation of, parameters of the BGW protocol.
 * <p>
 * This is the abstract super-type of two concrete parameters structures:
 * <ul><li> {@link BGWPrivateParameters} represents the private parameters one party generates in the BGW protocol
 * <li> {@link BGWPublicParameters} is the structure used by parties when exchanging their shares
 * <p>
 * @author Christian Mouchet
 */
public abstract class BGWParameters {
	/** The id of the party. i &in; [1;N]*/
	public final int i;
	/** the number of parties*/
	public final int n;
	
	private BGWParameters(int i, int n) {
		this.i = i;
		this.n = n;
	}
	
	/**
	 * The structure of private contributions of a parti in the BGW protocol.
	 * @author Christian Mouchet
	 */
	public static class BGWPrivateParameters extends BGWParameters {
		/** The contribution of party Pi to p = &sum;<sup>N</sup><sub>i=1</sub>   pi*/
		public final BigInteger pi;
		
		/** The contribution of party Pi to q = &sum;<sup>N</sup><sub>i=1</sub>   qi*/
		public final BigInteger qi;
		
		/** The polynomial used to share pi*/
		public final PolynomialMod fi;
		
		/** The polynomial used to share qi*/
		public final PolynomialMod gi;
		
		/** The polynomial used to share Ni*/
		public final PolynomialMod hi;
		
		private BGWPrivateParameters(int i, int n, BigInteger p, BigInteger q,PolynomialMod f, PolynomialMod g, PolynomialMod h) {
			super(i,n);
			this.pi = p;
			this.qi = q;
			this.fi = f;
			this.gi = g;
			this.hi = h;
		}
		
		/** Generates the private parameters for a given party in the BGW protocol
		 * @param i the id of the party. i &in; [1,n], n the number of parties
		 * @param protParam the security parameters
		 * @param rand a randomness generator
		 * @return the generated parameters
		 */
		public static BGWPrivateParameters genFor(int i, ProtocolParameters protParam, Random rand) {
			
			if (i < 1 || i > protParam.n)
				throw new IllegalArgumentException("i must be between 1 and the number of parties");
			
			// p and q generation
			BigInteger p;
			BigInteger q;
			BigInteger min = BigInteger.ONE.shiftLeft(protParam.k-1);
			BigInteger max = BigInteger.ONE.shiftLeft(protParam.k).subtract(BigInteger.ONE);
			BigInteger four = BigInteger.valueOf(4);
			BigInteger modFourTarget = i == 1 ? BigInteger.valueOf(3) : BigInteger.ZERO;
			
			do {
				p = min.add(new BigInteger(protParam.k-1, rand));
				q = min.add(new BigInteger(protParam.k-1, rand));
			} while(p.compareTo(max) > 0 ||
					q.compareTo(max) > 0 ||
					! p.mod(four).equals(modFourTarget) ||
					! q.mod(four).equals(modFourTarget));
			
			// polynomials generation
			
			PolynomialMod f = new PolynomialMod(protParam.t, protParam.P, p, protParam.k, rand);
			PolynomialMod g = new PolynomialMod(protParam.t, protParam.P, q, protParam.k, rand);
			PolynomialMod h = new PolynomialMod(2*protParam.t, protParam.P, BigInteger.ZERO, protParam.k, rand); 
			
			return new BGWPrivateParameters(i, protParam.n, p, q, f, g, h);
		}
		
		@Override
		public String toString() {
			return String.format("BGWPrivateParameters[%d]", i);
		}
		
	}
	
	/**
	 * The structure used by the parties to exchange their shares.
	 * @author Christian Mouchet
	 */
	public static class BGWPublicParameters extends BGWParameters {

		/** The id of the party for which these shares were generated*/
		public final int j;
		
		/** The share pij = f(j) of party i's pi generated for party j*/
		public final BigInteger pij;
		
		/** The share qij = g(j) of party i's qi generated for party j*/
		public final BigInteger qij;
		
		/** The share hij = h(j) of party i's zero generated for party j*/
		public final BigInteger hij;

		private BGWPublicParameters(int i, int j, int n, BigInteger pj, BigInteger qj, BigInteger hj) {
			super(i, n);
			this.pij = pj;
			this.qij = qj;
			this.hij = hj;
			this.j = j;
		}
		
		/** Checks the shares for correctness. Not implemented yet.
		 * @param protocolParameters the security parameters of the protocol
		 * @return true if the share could be verified, false otherwise
		 */
		public boolean isCorrect(ProtocolParameters protocolParameters) {
			// Not implemented yet
			return true;
		}
		
		/** Generates the shares for a given party j.
		 * @param j the id of the party for which we want to generate the share
		 * @param bgwPrivParam the private parameters to use
		 * @return a structure containing the shares generated to party j
		 */
		public static BGWPublicParameters genFor(int j, BGWPrivateParameters bgwPrivParam) {
			
			if (j < 1 || j > bgwPrivParam.n)
				throw new IllegalArgumentException("j must be between 1 and the number of parties");
			
			int i = bgwPrivParam.i;
			BigInteger pj = bgwPrivParam.fi.eval(j);
			BigInteger qj = bgwPrivParam.gi.eval(j);
			BigInteger hj = bgwPrivParam.hi.eval(j);
			return new BGWPublicParameters(i,j, bgwPrivParam.n, pj, qj, hj);
		}
		
		@Override
		public String toString() {
			return String.format("BGWPublicParameters[%d][%d]", this.i, this.j);
		}
	}
}
