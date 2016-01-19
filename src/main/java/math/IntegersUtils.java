package math;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;

/**
 *  Provides some of the required operations on the integers like random picking in range, factorial,...
 * @author Christian Mouchet
 */
public class IntegersUtils {

	/** Picks an integer at random in the given range, boundaries included.
	 * @param min the lower bound of the range
	 * @param max the upper bound of the range
	 * @param rand a randomness generator
	 * @return a random integer in the range [min; max]
	 */
	public static BigInteger pickInRange(BigInteger min, BigInteger max, Random rand) {
		BigInteger candidate;
		do {
			candidate = min.add(new BigInteger(max.bitLength(), rand));
		} while(candidate.compareTo(max) > 0);
		return candidate;
	}
	
	public static BigInteger pickPrimeInRange(BigInteger min, BigInteger max, Random rand) {
		BigInteger candidate;
		do {
			candidate = min.add(BigInteger.probablePrime(max.bitLength()-1, rand));
		} while(candidate.compareTo(max) > 0);
		return candidate;
	}
	
	/** Picks a random integer that is a generator of Z<sub>N<sup>2</sup></sub> with high probability.
	 * @param N is N
	 * @param bitLength the size of the integer to be picked in bit
	 * @param rand a randomness generator
	 * @return a probable generator of of Z<sub>N<sup>2</sup></sub>
	 */
	public static BigInteger pickProbableGeneratorOfZNSquare(BigInteger N, int bitLength, Random rand) {
		BigInteger r;
		do {
			r = new BigInteger(bitLength,rand);
		} while (!BigInteger.ONE.equals(r.gcd(N)));
		return r.multiply(r).mod(N.multiply(N));
	}
	
	/** Computes the factorial of an integer.
	 * @param n an integer
	 * @return <i>n</i>!
	 */
	public static BigInteger factorial(BigInteger n) {
		BigInteger result = BigInteger.ONE;
		while (!n.equals(BigInteger.ZERO)) {			
			result = result.multiply(n);
			n = n.subtract(BigInteger.ONE);
		}
		return result;
	}

	
	/** Computes the intercept of a polynomial mod N over the integers given a list of points using
	 * Lagrangian interpolation.
	 * @param points the list of points in the order: <code>(f(1), f(2), f(3),...)</code>
	 * @param mod the modulo of the polynomial
	 * @return <code>f(0)</code>
	 */
	public static BigInteger getIntercept(List<BigInteger> points, BigInteger mod) {
		int k = points.size();
		BigInteger sum = BigInteger.ZERO;
		for(int j=1; j <= k; j++){
			int numerator = 1;
			int denominator = 1;
			for(int m=1; m<=k; m++){
				if (m != j) {
					numerator *= -m;
					denominator *= (j-m);
				}
			}
			BigInteger lambdaj = BigInteger.valueOf(numerator/denominator);
			sum = sum.add(points.get(j-1).multiply(lambdaj)).mod(mod);
		}
		return sum;
	}
	
	
}
