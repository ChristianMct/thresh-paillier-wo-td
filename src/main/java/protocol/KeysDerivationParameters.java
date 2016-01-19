package protocol;

import java.math.BigInteger;
import java.util.Random;

import math.IntegersUtils;
import math.Polynomial;
import math.PolynomialMod;

/**
 * Provide structures for, and generation of, parameters of the Key Derivation protocol.
 * <p>
 * This is the abstract super-type of two concrete parameters structures:
 * <ul><li> {@link KeysDerivationPrivateParameters} represents the private parameters one party generates in the
 * 			key derivation protocol
 * <li> {@link KeysDerivationPublicParameters} is the structure used by parties when exchanging their shares
 * <p>
 * @author Christian Mouchet
 */
public abstract class KeysDerivationParameters {
	
	public final int i;
	
	private KeysDerivationParameters(int i) {
		this.i = i;
	}
	
	/**
	 * The structure of private contributions of a party in the key derivation protocol.
	 * @author Christian Mouchet
	 */
	public static class KeysDerivationPrivateParameters extends KeysDerivationParameters {
		public final Polynomial DRiSharing;
		public final PolynomialMod betaiSharing;
		public final PolynomialMod PhiSharing;
		public final PolynomialMod zeroSharing;
		
		private KeysDerivationPrivateParameters(int i, PolynomialMod betaiSharing, PolynomialMod PhiSharing, PolynomialMod zeroSharing, Polynomial DRiSharing) {
			super(i);
			this.betaiSharing = betaiSharing;
			this.DRiSharing = DRiSharing;
			this.PhiSharing = PhiSharing;
			this.zeroSharing = zeroSharing;
		}
		
		/** Generate the key derivation private parameters for a given party i.
		 * @param protocolParameters the security parameters of the protocol
		 * @param i the party's id
		 * @param N the accepted RSA modulus
		 * @param Phii the contribution of this party to $Phi;(N)
		 * @param rand a randomness generator
		 * @return the generated private parameters
		 */
		public static KeysDerivationPrivateParameters gen(ProtocolParameters protocolParameters, int i,BigInteger N, BigInteger Phii, Random rand) {
			
			if (i < 1 || i > protocolParameters.n)
				throw new IllegalArgumentException("i must be between 1 and the number of parties");
			
			BigInteger KN = N.multiply(BigInteger.valueOf(protocolParameters.K));
			BigInteger K2N = KN.multiply(BigInteger.valueOf(protocolParameters.K));
			
			BigInteger betai = IntegersUtils.pickInRange(BigInteger.ZERO, KN, rand);
			BigInteger delta = IntegersUtils.factorial(BigInteger.valueOf(protocolParameters.n));
			BigInteger Ri = IntegersUtils.pickInRange(BigInteger.ZERO, K2N, rand);
						
			PolynomialMod betaiSharing = new PolynomialMod(protocolParameters.t, protocolParameters.P, betai, protocolParameters.k, rand);
			PolynomialMod PhiiSharing = new PolynomialMod(protocolParameters.t, protocolParameters.P, Phii, protocolParameters.k, rand);
			PolynomialMod zeroSharing = new PolynomialMod(protocolParameters.t, protocolParameters.P, BigInteger.ZERO, protocolParameters.k, rand);
			Polynomial DRiSharing = new Polynomial(protocolParameters.t, delta.multiply(Ri), protocolParameters.k, rand);
			
			return new KeysDerivationPrivateParameters(i, betaiSharing, PhiiSharing, zeroSharing, DRiSharing);
		}
		
	}
	
	/**
	 * The structure used by the parties to exchange their shares in the key derivation
	 * @author Christian Mouchet
	 */
	public static class KeysDerivationPublicParameters extends KeysDerivationParameters {
		public final int j;
		public final BigInteger betaij;
		public final BigInteger DRij;
		public final BigInteger Phiij;
		public final BigInteger hij;
		
		private KeysDerivationPublicParameters(int i, int j, BigInteger betaij, BigInteger Rij, BigInteger Phiij, BigInteger hij){
			super(i);
			this.j = j;
			this.betaij = betaij;
			this.DRij = Rij;
			this.Phiij = Phiij;
			this.hij = hij;
		}
		
		/** Generates the structure containing the shares for party j
		 * @param j the id of the party for which we want to generate the shares
		 * @param keysDerivationPrivateParameters the private parameters to use
		 * @return the structure containing the shares for party j
		 */
		public static KeysDerivationPublicParameters genFor(int j, KeysDerivationPrivateParameters keysDerivationPrivateParameters) {
			BigInteger Betaij = keysDerivationPrivateParameters.betaiSharing.eval(j);
			BigInteger DRij = keysDerivationPrivateParameters.DRiSharing.eval(j);
			BigInteger Phiij = keysDerivationPrivateParameters.PhiSharing.eval(j);
			BigInteger hij = keysDerivationPrivateParameters.zeroSharing.eval(j);
			return new KeysDerivationPublicParameters(keysDerivationPrivateParameters.i, j, Betaij, DRij, Phiij, hij);
		}
	}
}
