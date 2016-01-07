import java.math.BigInteger;
import java.util.Random;


public class genpq {

	public static void main(String[] args) {
		
		Random r = new Random(6);
		
		BigInteger p1 = new BigInteger("3");
		BigInteger p = BigInteger.probablePrime(32, r);
		
		System.out.println("p="+p);
		
		while (!p.subtract(p1).mod(BigInteger.valueOf(36)).equals(BigInteger.ZERO)) {
			//System.out.println(p1);
			if (p1.compareTo(p)  == 1) {				
				p = BigInteger.probablePrime(32, r);
				p1 = new BigInteger("3");
				System.out.println("p="+p);
			}
			p1=p1.add(BigInteger.valueOf(4));
		}
		
		System.out.println("FOUND: p1=" + p1);
		System.out.println("ps = "+p.subtract(p1));
		
	}
	
	//checks whether an int is prime or not.
	static boolean isPrime(long n) {
	    //check if n is a multiple of 2
	    if (n%2==0) return false;
	    //if not, then just check the odds
	    for(int i=3;i*i<=n;i+=2) {
	        if(n%i==0)
	            return false;
	    }
	    return true;
	}


}
