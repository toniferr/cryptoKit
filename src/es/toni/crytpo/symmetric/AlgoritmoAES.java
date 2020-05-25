package es.toni.crytpo.symmetric;

import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

public class AlgoritmoAES {
    
    /**
     * 
     * @param texto
     * @return string - cadena descifrada AES 
     * @throws Exception
     */
    public static String cifrarAESPKCS7(String texto,String clave){
    	try{
    		Security.addProvider(new BouncyCastleProvider());
    		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
			SecretKeySpec skeySpec = new SecretKeySpec(clave.getBytes(),"AES");
			
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			byte[] bloqueCifrado = cipher.doFinal(texto.getBytes());
	    	return Hex.toHexString(bloqueCifrado)+" (hexadecimal)";
    	}catch (Exception e){
    		return e.toString();
    	}
    }
    
    /**
     * 
     * @param texto
     * @return string - cadena descifrada AES 
     * @throws Exception
     */
    public static String cifrarAESPKCS5(String texto,String clave,String vector){
    	try{
    		Security.addProvider(new BouncyCastleProvider());
    		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
			SecretKeySpec skeySpec = new SecretKeySpec(clave.getBytes(),"AES");
			IvParameterSpec ivParameterSpec = new IvParameterSpec(vector.getBytes());
			
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParameterSpec);
			byte[] bloqueCifrado = cipher.doFinal(texto.getBytes());
	    	return Hex.toHexString(bloqueCifrado)+" (hexadecimal)";
    	}catch (Exception e){
    		return e.toString();
    	}
    }
        
    /**
     * 
     * @param texto
     * @return string - cadena cifrada AES 
     * @throws Exception
     */
    public static String descifrarAESPKCS7(String texto,String clave){
    	try {
    		Security.addProvider(new BouncyCastleProvider());
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
			SecretKeySpec skeySpec = new SecretKeySpec(clave.getBytes(),"AES");
			
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			byte[] bloqueCifrado = cipher.doFinal(Hex.decode(texto.getBytes()));
			return new String(bloqueCifrado);
    	}catch (Exception e){
    		return e.toString();
    	}
    }    
    
    /**
     * 
     * @param texto
     * @return string - cadena cifrada AES 
     * @throws Exception
     */
    public static String descifrarAESPKCS5(String texto,String clave,String vector){
    	try {
    		Security.addProvider(new BouncyCastleProvider());
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
			SecretKeySpec skeySpec = new SecretKeySpec(clave.getBytes(),"AES");
			IvParameterSpec ivParameterSpec = new IvParameterSpec(vector.getBytes());
			
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivParameterSpec);
			byte[] bloqueCifrado = cipher.doFinal(Hex.decode(texto.getBytes()));
			return new String(bloqueCifrado);
    	}catch (Exception e){
    		return e.toString();
    	}
    }
}
