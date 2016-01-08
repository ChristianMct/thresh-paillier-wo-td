package protocol;

import java.math.BigInteger;
import java.util.Random;

import math.IntegersUtils;
import math.Polynomial;
import math.PolynomialMod;

public abstract class KeysDerivationParameters {
	
	public static class KeysDerivationPrivateParameters {
		public final int i;
		public final PolynomialMod betaiSharing;
		public final Polynomial RiSharing;
		
		private KeysDerivationPrivateParameters(int i, PolynomialMod betaiSharing, Polynomial RiSharing) {
			this.i = i;
			this.betaiSharing = betaiSharing;
			this.RiSharing = RiSharing;
		}
		
		public static KeysDerivationPrivateParameters gen(ProtocolParameters protocolParameters, int i,BigInteger N, Random rand) {
			BigInteger KN = N.multiply(BigInteger.valueOf(protocolParameters.K));
			BigInteger K2N = KN.multiply(BigInteger.valueOf(protocolParameters.K));
			
			BigInteger betai = IntegersUtils.pickInRange(BigInteger.ZERO, KN, rand);
			BigInteger Ri = IntegersUtils.pickInRange(BigInteger.ZERO, K2N, rand);
			
			PolynomialMod betaiSharing = new PolynomialMod(protocolParameters.t, protocolParameters.Pp, betai, protocolParameters.k, rand);
			Polynomial RiSharing = new Polynomial(protocolParameters.t, Ri, protocolParameters.k, rand);
			
			return new KeysDerivationPrivateParameters(i, betaiSharing, RiSharing);
		}
		
	}
	
	public static class KeysDerivationPublicParameters {
		public final int i;
		public final int j;
		public final BigInteger betaij;
		public final BigInteger Rij;
		
		private KeysDerivationPublicParameters(int i, int j, BigInteger betaij, BigInteger Rij){
			this.i = i;
			this.j = j;
			this.betaij = betaij;
			this.Rij = Rij;
		}
		
		public static KeysDerivationPublicParameters genFor(int j, KeysDerivationPrivateParameters keysDerivationPrivateParameters) {
			BigInteger Betaij = keysDerivationPrivateParameters.betaiSharing.eval(j);
			BigInteger Rij = keysDerivationPrivateParameters.RiSharing.eval(j);
			return new KeysDerivationPublicParameters(keysDerivationPrivateParameters.i, j, Betaij, Rij);
		}
	}
}
