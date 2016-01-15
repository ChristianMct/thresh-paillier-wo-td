package math;

import java.math.BigInteger;
import java.util.List;

public class LagrangianInterpolation {
	
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
