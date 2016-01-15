import java.io.FileOutputStream;
import java.io.IOException;

import paillierp.key.KeyGen;
import paillierp.key.PaillierPrivateThresholdKey;

public class PaillierKeyGen {
	
	
	
	public static void main(String[] arg) throws IOException {
		PaillierPrivateThresholdKey[] keys = KeyGen.PaillierThresholdKey(64, 10, 4, 1);
		
		int i = 1;
		for(PaillierPrivateThresholdKey key : keys) {
			FileOutputStream fos = new FileOutputStream("keys/Actor"+(i)+".privkey");
			fos.write(key.toByteArray());
			fos.close();
			System.out.println("DONE "+i++);
		}
	}
	
}
