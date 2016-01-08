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
	
	
}
