package math;

import java.math.BigInteger;
import java.util.Random;

public class IntegersUtils {

	public static BigInteger pickInRange(BigInteger min, BigInteger max, Random rand) {
		BigInteger candidate;
		do {
			candidate = min.add(new BigInteger(max.bitLength(), rand));
		} while(candidate.compareTo(max) > 0);
		return candidate;
	}
	
	public static BigInteger pickProbableGeneratorOfZNSquare(BigInteger N, int bitLength, Random rand) {
		BigInteger r;
		do {
			r = new BigInteger(bitLength,rand);
		} while (!BigInteger.ONE.equals(r.gcd(N)));
		return r.multiply(r).mod(N.multiply(N));
	}
	
	public static BigInteger factorial(BigInteger n) {
		BigInteger result = BigInteger.ONE;
		while (!n.equals(BigInteger.ZERO)) {			
			result = result.multiply(n);
			n = n.subtract(BigInteger.ONE);
		}
		return result;
	}
	
	
}
