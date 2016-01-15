import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bouncycastle.util.BigIntegers;

import paillierp.PaillierThreshold;
import paillierp.PartialDecryption;
import paillierp.key.KeyGen;
import paillierp.key.PaillierKey;
import paillierp.key.PaillierPrivateThresholdKey;


public class PaillierKeysTest {

	private static final int NUM_SERV =6;

	public static void main(String[] args) throws IOException {
		
		
		
		Random rand = new SecureRandom();
		List<PaillierPrivateThresholdKey> keys = new ArrayList<PaillierPrivateThresholdKey>();
		List<PaillierThreshold> decryptionServers =  new ArrayList<PaillierThreshold>();
		
		
		Files.walk(Paths.get("keys")).filter(f -> f.toString().endsWith(".privkey")).forEach(filePath -> {
		    try {
		    	if(Files.isRegularFile(filePath))
		    		System.out.println(filePath.toString());
		    		keys.add(new PaillierPrivateThresholdKey(Files.readAllBytes(filePath), rand.nextLong()));
			} catch (Exception e) {
				e.printStackTrace();
				//System.exit(1);
			}
		});
		
		for(PaillierPrivateThresholdKey key : keys) {
			assert(key.canEncrypt());
			PaillierThreshold decryptionServer = new PaillierThreshold(key);
			if(key.getID() <= NUM_SERV)
				decryptionServers.add(decryptionServer);
		}
		
		BigInteger M = new BigInteger(new String("Hello!").getBytes());
		
		PaillierKey publickey = decryptionServers.get(1).getPublicKey();
		BigInteger N = publickey.getN();
		BigInteger Ns = N.multiply(N);
		BigInteger G = publickey.getNPlusOne();
		BigInteger thetap = decryptionServers.get(1).getPrivateKey().getThetaPrime();
		BigInteger x = new BigInteger(publickey.getK(), new Random(1)).mod(N).modInverse(N);
		
		BigInteger cipher = G.modPow(M, Ns).multiply(x.modPow(N, Ns)).mod(Ns);
		System.out.println("me =>"+cipher);
		System.out.println("ud =>"+decryptionServers.get(1).encrypt(M, x));
		System.out.println("---------------------------------------------");
		
		BigInteger two = BigInteger.ONE.add(BigInteger.ONE);
		BigInteger delta = KeyGen.factorial(10);
		
		BigInteger[] partDecInt = new BigInteger[decryptionServers.size()];
		PartialDecryption[] partDec = new PartialDecryption[decryptionServers.size()];
		
		for(int i=0; i<decryptionServers.size(); i++){
			PaillierPrivateThresholdKey privateKey = decryptionServers.get(i).getPrivateKey();
			BigInteger fi = privateKey.getSi();
			
			partDecInt[privateKey.getID()-1] = cipher.modPow(two.multiply(delta).multiply(fi),Ns);
			partDec[privateKey.getID()-1] = decryptionServers.get(i).decrypt(cipher);
			System.out.println("me =>"+partDecInt[privateKey.getID()-1]);
			System.out.println("ud =>"+partDec[privateKey.getID()-1].getDecryptedValue());
			System.out.println("------------------------------------------------------------------");
		}
		int S = decryptionServers.size();
		
		BigInteger prod = BigInteger.ONE;
		for(PartialDecryption pd: partDec)
			prod = prod.multiply(pd.getDecryptedValue().modPow(two.multiply(delta).multiply(lambda(pd.getID(), partDec)), Ns));
		
		BigInteger L = prod.subtract(BigInteger.ONE).divide(N);
		BigInteger theta = thetap.mod(N);
		BigInteger cst = BigInteger.valueOf(4).multiply(delta.multiply(delta)).multiply(thetap).modInverse(N);
		
		BigInteger DM = (L.multiply(cst)).mod(N);
		BigInteger udDM = decryptionServers.get(1).combineShares(partDec);
		System.out.println("me =>"+DM+"     ("+new String(DM.toByteArray())+")");
		System.out.println("ud =>"+udDM+"     ("+new String(udDM.toByteArray())+")");
		
		System.out.println();
		

		
		//decryptionServers.stream().map(s -> s.decrypt(cipher)).forEach(pd -> partDecArr[pd.getID()-1] = pd);
		
		
		
		
		//BigInteger result = decryptionServers.get(1).combineShares(partDecArr);
		
		System.out.println(new String(cipher.toString()));
	
	}
	
	public static BigInteger lambda(int i, PartialDecryption[] decryptionServers) {
		int numerator = 1;
		int denominator = 1;
		for(PartialDecryption pd: decryptionServers){
			if (pd.getID() != i) {
				numerator *= -pd.getID();
				denominator *= (i-pd.getID());
			}
		}
		return BigInteger.valueOf(numerator/denominator);
	}

}
