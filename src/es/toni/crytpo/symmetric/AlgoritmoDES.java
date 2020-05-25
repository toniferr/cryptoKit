package es.toni.crytpo.symmetric;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Hex;

public class AlgoritmoDES {

    /**
     * 
     * @param texto
     * @param clave
     * @return string - cadena descifrada DES con modo de cifrado ECB
     */
    public static String cifrarDESecb(String texto,String clave){
    	try {
	    	SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
	    	DESKeySpec kspec = new DESKeySpec(clave.getBytes());
	    	SecretKey ks = skf.generateSecret(kspec);
	    	
	    	Cipher cifrado = Cipher.getInstance("DES/ECB/PKCS5Padding");
	    	cifrado.init(Cipher.ENCRYPT_MODE, ks);
	    	byte[] bloqueCifrado = cifrado.doFinal(texto.getBytes());
	    	return Hex.toHexString(bloqueCifrado);
    	}catch(Exception e){
    		return e.toString();
    	}
    }
    
    /**
     * 
     * @param texto
     * @param clave
     * @return string - cadena descifrada DES con modo de cifrado ECB
     */
    public static String cifrarDEScbc(String texto,String clave, String vector){
    	try {    		
		    SecretKeySpec pKey = new SecretKeySpec(clave.getBytes(), "DES");
		    IvParameterSpec ivectorSpecv = new IvParameterSpec(vector.getBytes());
		    Cipher cifrado = Cipher.getInstance("DES/CBC/PKCS5Padding");
		    
		    cifrado.init(Cipher.ENCRYPT_MODE, pKey, ivectorSpecv);
	    	byte[] bloqueCifrado = cifrado.doFinal(texto.getBytes());
	    	return Hex.toHexString(bloqueCifrado);
    	}catch(Exception e){
    		return e.toString();
    	}
    }
    
    /**
     * 
     * @param texto
     * @param clave
     * @return string - cadena descifrada DES con modo de cifrado ECB
     */
    public static String descifrarDESecb(String texto,String clave){
    	try {
	    	SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
	    	DESKeySpec kspec = new DESKeySpec(clave.getBytes());
	    	SecretKey ks = skf.generateSecret(kspec);
	    	
	    	Cipher cifrado = Cipher.getInstance("DES/ECB/PKCS5Padding");
	    	cifrado.init(Cipher.DECRYPT_MODE, ks);
	    	byte[] bloqueCifrado = cifrado.doFinal(Hex.decode(texto.getBytes()));
	    	return new String(bloqueCifrado);
    	}catch(Exception e){
    		return e.toString();
    	}
    }
    
    /**
     * 
     * @param texto
     * @param clave
     * @param vector
     * @return string - cadena descifrada DES con modo de cifrado ECB
     */
    public static String descifrarDEScbc(String texto,String clave, String vector){
    	try {
		    SecretKeySpec pKey = new SecretKeySpec(clave.getBytes(), "DES");
		    IvParameterSpec ivectorSpecv = new IvParameterSpec(vector.getBytes());
		    Cipher cifrado = Cipher.getInstance("DES/CBC/PKCS5Padding");
		    
		    cifrado.init(Cipher.DECRYPT_MODE, pKey, ivectorSpecv);
	    	byte[] bloqueCifrado = cifrado.doFinal(Hex.decode(texto.getBytes()));
	    	return new String(bloqueCifrado);
    	}catch(Exception e){
    		return e.toString();
    	}
    }

}
