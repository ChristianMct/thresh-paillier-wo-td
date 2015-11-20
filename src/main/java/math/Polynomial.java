package math;

import java.math.BigInteger;
import java.security.SecureRandom;

public class Polynomial {

	private final BigInteger[] a;
	private final BigInteger mod;
	
	public Polynomial(int degree, BigInteger mod, BigInteger a0, SecureRandom sr, int numbit) {
		this.mod = mod;
		a = new BigInteger[degree+1];
		a[0] = a0.mod(mod);
		for(int t=1; t<=degree; t++) {
			a[t] = new BigInteger(numbit,sr).mod(mod);
		}
	}
	
	public BigInteger eval(int x) {
        BigInteger result = a[0];
        BigInteger powx;
        BigInteger term;
        
        for(int i = 1;i < a.length;i++) {
        	powx = new BigInteger(Integer.toString((int)Math.pow((double)x,(double)i)));
            term = a[i].multiply(powx);
            term = term.mod(mod);
            result = result.add(term);
            result = result.mod(mod);
        }
        return result;
	}
	
}
