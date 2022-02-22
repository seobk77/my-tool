package free.my.tool.util;

import static free.my.tool.ui.panel.ZookeeperPanel.E_KEY;

import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncUtil {
	private static final String AES = "AES";
	private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
	private static final String ENCODING = "UTF-8";
	private String iv = null;
	private Key keySpec = null;
	
	public EncUtil() throws Exception {
		this.createAESKey();
	}
	
	private void createAESKey() throws Exception {
		this.iv = E_KEY.substring(0, 16);
		
		byte[] keyBytes = new byte[16];
		byte[] b = E_KEY.getBytes(ENCODING);

		int len = b.length;
		if (len > keyBytes.length) {
			len = keyBytes.length;
		}

		System.arraycopy(b, 0, keyBytes, 0, len);
		this.keySpec = new SecretKeySpec(keyBytes, AES);
	}

	public String encAES(String str) throws Exception {
		Cipher c = Cipher.getInstance(TRANSFORMATION);
		c.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes()));
		byte[] encrypted = c.doFinal(str.getBytes(ENCODING));
		String  enStr = Base64.getEncoder().encodeToString(encrypted);

		return enStr;
	}

	public String decAES(String enStr) throws Exception {
		Cipher c = Cipher.getInstance(TRANSFORMATION);
		c.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(iv.getBytes(ENCODING)));
		byte[] byteStr = Base64.getDecoder().decode(enStr.getBytes(ENCODING));
		String decStr = new String(c.doFinal(byteStr), ENCODING);

		return decStr;
	}
/*	
	public static void main(String[] args) throws Exception {
//		String a = "vnf20dj17qh09tp14dy_zz"; //2017-09-14
//		String a = "vnf20dj18qh12tp31dy_zz"; //2018-12-31
//		String a = "vnf20dj18qh03tp31dy_zz"; //2018-03-31
//		String a = "vnf20dj18qh07tp01dy_zz"; //2018-07-01
//		String a = "vnf20dj18qh12tp31dy_zz"; //2018-12-31
		
		EncUtil e = new EncUtil();
		
		String encAES = e.encAES(a);
		String decAES = e.decAES(encAES);
		
		System.out.println("["+encAES+"]");
		System.out.println(decAES);
	}
*/	
}
