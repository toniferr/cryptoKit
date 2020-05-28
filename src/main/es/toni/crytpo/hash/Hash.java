package es.toni.crytpo.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.util.encoders.Hex;

import es.toni.crytpo.utils.Constantes;
import es.toni.crytpo.utils.Validation;

public class Hash {
	
    /**
     * 
     * @param index
     * @param texto
     * @return string - cadena cifrada Hash
     */
    public static String cifrarHash(Integer index,String texto){
    	if (!Validation.validarDatos(texto)){
    		return Constantes.DATOS_INCORRECTOS;
    	}else{
    		return cifrar(texto,index);
    	}
    }
    
    /**
     * 
     * @param texto
     * @param tipo
     * @return string - cadena cifrada hash 
     */
    public static String cifrar(String texto,Integer tipo){

		byte[] mensajeACifrar = texto.getBytes();
		
		if (tipo == 0){
			return cifrarMessageDigest(mensajeACifrar, "MD5");
		}
		if (tipo == 1){
			return cifrarMessageDigest(mensajeACifrar, "SHA1");
		}
		if (tipo == 2){
			return cifrarMessageDigest(mensajeACifrar, "SHA-256");
		}
		if (tipo == 3){
			return cifrarMessageDigest(mensajeACifrar, "SHA-512");
		}
		if (tipo == 4 || tipo == 5){
			return cifrarSha3(mensajeACifrar, tipo);
		}
		return "Error de la aplicación al lanzar algoritmo";
    }
    
    /**
     * 
     * @param mensajeACifrar
     * @param cifrado
     * @return string - cadena cifrada hash 
     */
    public static String cifrarMessageDigest (byte[] mensajeACifrar, String cifrado) {
    	MessageDigest messageDigest;
		try {
			messageDigest = MessageDigest.getInstance(cifrado);
		} catch (NoSuchAlgorithmException e) {
			return "Error de la aplicación al lanzar algoritmo";
		}
		messageDigest.update(mensajeACifrar);
		byte[] resumen = messageDigest.digest();
		return Hex.toHexString(resumen);
    }
    
    /**
     * 
     * @param mensajeACifrar
     * @param tipo
     * @return string - cadena cifrada hash 
     */
    public static String cifrarSha3 (byte[] mensajeACifrar, Integer tipo) {
    	
       SHA3.DigestSHA3 digestSHA3 = null;
       if (tipo == 4) {
    	   digestSHA3 = new SHA3.Digest256();
       }
       if (tipo == 5) {
    	   digestSHA3 = new SHA3.Digest512();
       }
       if (digestSHA3 != null) {
           byte[] digest = digestSHA3.digest(mensajeACifrar);
           return Hex.toHexString(digest);
       }else {
    	   return "Error de la aplicación al lanzar algoritmo";
       }
    }
    
}
