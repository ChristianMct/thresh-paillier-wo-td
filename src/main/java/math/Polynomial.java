package math;

import java.math.BigInteger;
import java.util.Random;

public class Polynomial {
	
	protected final BigInteger[] a;
	
	public Polynomial(int degree, BigInteger a0, int numbit, Random random) {
		a = new BigInteger[degree+1];
		a[0] = a0;
		for (int t=1; t <= degree; t++) {
			a[t] = new BigInteger(numbit, random);
		}
	}
	
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
