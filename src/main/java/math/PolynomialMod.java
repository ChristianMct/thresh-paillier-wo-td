package math;

import java.math.BigInteger;
import java.util.Random;

/**
 * Represents a polynomial over the integers modulo N. This extends the {@link Polynomial} class with a constructor
 * taking N as a parameter.
 * <p> the <code>eval</code> method is overridden to reduce mod N at each step.
 * @author Christian Mouchet
 */
public class PolynomialMod extends Polynomial{
	
	protected final BigInteger mod;
	
	/** Construct a random polynomial over the integers mod N given its degree and intercept
	 * @param degree the degree of this polynomial
	 * @param mod N
	 * @param a0 the intercept <code>f(0)</code>
	 * @param numbit the bitlength of the coefficients
	 * @param random a randomness generator
	 */
	public PolynomialMod(int degree, BigInteger mod, BigInteger a0,  int numbit, Random random) {
		super(degree, a0, numbit, random);
		this.mod = mod;
		a[0] = a[0].mod(mod);
		for(int t=0; t<=degree; t++) {
			a[t] = a[t].mod(mod);
		}
	}
	
	@Override
	public BigInteger eval(int x) {
        BigInteger result = a[0];
        BigInteger powx;
        BigInteger term;
        
        for(int i = 1;i < a.length;i++) {
        	powx = BigInteger.valueOf((long) Math.pow(x, i));
            term = a[i].multiply(powx).mod(mod);
            result = result.add(term).mod(mod);
        }
        return result;
	}
	
}
