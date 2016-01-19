package math;

import java.math.BigInteger;
import java.util.Random;

/**
 * Represents a polynomial over the integers. This simply wraps an array of the
 * coefficient and provides convenience methods to build and evaluate a polynomial.
 * @author Christian Mouchet
 */
public class Polynomial {
	
	protected final BigInteger[] a;
	
	/** Constructs a new random polynomial with the given degree and intercept
	 * @param degree the degree of the polynomial
	 * @param a0 the intercept of the polynomial <code>f(0)</code>
	 * @param numbit the bitlength of the coefficients
	 * @param random a randomness generator
	 */
	public Polynomial(int degree, BigInteger a0, int numbit, Random random) {
		a = new BigInteger[degree+1];
		a[0] = a0;
		for (int t=1; t <= degree; t++) {
			a[t] = new BigInteger(numbit, random);
		}
	}
	
	/** Evaluate this polynomial in <code>x</code>
	 * @param x <code>x</code>
	 * @return <code>f(x)</code>
	 */
	public BigInteger eval(int x) {
        BigInteger result = a[0];
        BigInteger powx;
        BigInteger term;
        
        for(int i = 1;i < a.length;i++) {
        	powx = BigInteger.valueOf((long) Math.pow(x, i));
            term = a[i].multiply(powx);
            result = result.add(term);
        }
        return result;
	}
	
	
}
