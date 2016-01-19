import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import paillierp.PaillierThreshold;
import paillierp.PartialDecryption;
import paillierp.key.PaillierPrivateThresholdKey;


/**
 * This script looks for keys in a folder named "Keys" in the project root directory.
 * It then creates at most NUM_SERV PaillierThreshold object, each initialized with a
 * different private key. Finally, it attempts to encrypt, share decrypt and combine 
 * the message MESSAGE.
 * 
 * @author Christian Mouchet
 */
public class PaillierKeysTest {

	private static final int NUM_SERV =5;
	private static final String MESSAGE = "Hello world !"; // Plain must be smaller than n^2

	public static void main(String[] args) throws IOException {
		
		
		
		Random rand = new SecureRandom();
		List<PaillierPrivateThresholdKey> keys = new ArrayList<PaillierPrivateThresholdKey>();
		List<PaillierThreshold> decryptionServers =  new ArrayList<PaillierThreshold>();
		
		Files.walk(Paths.get("keys")).filter(f -> f.toString().endsWith(".privkey")).forEach(filePath -> {
		    try {
		    	if(Files.isRegularFile(filePath)) {
		    		//System.out.println(filePath.toString());
		    		keys.add(new PaillierPrivateThresholdKey(Files.readAllBytes(filePath), rand.nextLong()));
		    	}
			} catch (Exception e) {
				e.printStackTrace();
				//System.exit(1);
			}
		});
		
		System.out.println("Found "+keys.size()+" keys.");
		System.out.println("---------------------------------------------");
		
		for(PaillierPrivateThresholdKey key : keys) {
			assert(key.canEncrypt());
			PaillierThreshold decryptionServer = new PaillierThreshold(key);
			if(key.getID() <= NUM_SERV)
				decryptionServers.add(decryptionServer);
		}
		
		System.out.println("Created "+decryptionServers.size()+" decryption servers");
		System.out.println("---------------------------------------------");
		
		BigInteger M = new BigInteger(new String(MESSAGE).getBytes());
		System.out.println("Plain => "+M+"         ("+new String(M.toByteArray())+")");
		System.out.println("---------------------------------------------");
		BigInteger cipher = decryptionServers.get(1).encrypt(M);
		System.out.println("Cipher =>"+cipher);
		System.out.println("---------------------------------------------");
				
		PartialDecryption[] partDec = new PartialDecryption[decryptionServers.size()];
		
		System.out.println("Decryption attempt with "+decryptionServers.size()+" decryption servers...");
		System.out.println();
		for(int i=0; i<decryptionServers.size(); i++){
			PaillierPrivateThresholdKey privateKey = decryptionServers.get(i).getPrivateKey();
			partDec[privateKey.getID()-1] = decryptionServers.get(i).decrypt(cipher);
		}
		
		BigInteger udDM = decryptionServers.get(1).combineShares(partDec);
		System.out.println("Decryption =>"+udDM+"     ("+new String(udDM.toByteArray())+")");
			
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
