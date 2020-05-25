package es.toni.crytpo.symmetric;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

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
	    	return new String(java.util.Base64.getEncoder().encode((bloqueCifrado)));
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
	    	byte[] bloqueCifrado = cifrado.doFinal(java.util.Base64.getDecoder().decode((texto)));
	    	return new String(bloqueCifrado);
    	}catch(Exception e){
    		return e.toString();
    	}
    }

}
