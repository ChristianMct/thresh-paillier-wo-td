package math;

import java.math.BigInteger;
import java.util.Random;

public class PolynomialMod extends Polynomial{
	
	protected final BigInteger mod;
	
	public PolynomialMod(int degree, BigInteger mod, BigInteger a0,  int numbit, Random random) {
		super(degree, a0, numbit, random);
		this.mod = mod;
		a[0] = a[0].mod(mod);
		for(int t=0; t<=degree; t++) {
			a[t] = a[t].mod(mod);
		}
	}
	
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
