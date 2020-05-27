package es.toni.crytpo.symmetric;

import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

public class AlgoritmoBlowfish {
	
    /**
     * 
     * @param texto
     * @param clave
     * @return string - cadena descifrada blowfish con modo de cifrado ECB
     */
    public static String cifrarBFSinPadding(String texto,String clave){
    	try {
    		Security.addProvider(new BouncyCastleProvider());
    		Cipher cipher = Cipher.getInstance("Blowfish/ECB/PKCS5Padding", "BC");
			SecretKeySpec skeySpec = new SecretKeySpec(clave.getBytes(),"Blowfish");
			
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			byte[] bloqueCifrado = cipher.doFinal(texto.getBytes());
	    	return Hex.toHexString(bloqueCifrado)+" (hexadecimal)";
    	}catch(Exception e){
    		return e.toString();
    	}
    }
    
    /**
     * 
     * @param texto
     * @param clave
     * @return string - cadena descifrada blowfish con modo de cifrado ECB
     */
    public static String descifrarBFSinPadding(String texto,String clave){
    	try {
    		Security.addProvider(new BouncyCastleProvider());
    		Cipher cipher = Cipher.getInstance("Blowfish/ECB/PKCS5Padding", "BC");
			SecretKeySpec skeySpec = new SecretKeySpec(clave.getBytes(),"Blowfish");
			
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			byte[] bloqueCifrado = cipher.doFinal(Hex.decode(texto.getBytes()));
			return new String(bloqueCifrado);
    	}catch(Exception e){
    		return e.toString();
    	}
    }

}
