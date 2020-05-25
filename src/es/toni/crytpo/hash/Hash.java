package es.toni.crytpo.hash;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.util.encoders.Hex;

import es.toni.crytpo.utils.Constantes;
import es.toni.crytpo.utils.Format;
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

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		try {
			outputStream.write(texto.getBytes());
		} catch (IOException e1) {
			return "Error de la aplicación en el texto a cifrar Hash";
		}
		byte[] mensajeACifrar = outputStream.toByteArray( );
		
		MessageDigest messageDigest = null;
		try {
			if (tipo == 0){
				messageDigest = MessageDigest.getInstance("MD5");
			}
			if (tipo == 1){
				messageDigest = MessageDigest.getInstance("SHA1");
			}
			if (tipo == 2){
				messageDigest = MessageDigest.getInstance("SHA-256");
			}
			if (tipo == 3){
				messageDigest = MessageDigest.getInstance("SHA-512");
			}
			if (tipo == 4){
		       SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest256();
		       byte[] digest = digestSHA3.digest(texto.getBytes());
		       return Hex.toHexString(digest);
			}
		} catch (NoSuchAlgorithmException e) {
			return "Error de la aplicación al lanzar algoritmo";
		} 
		messageDigest.update(mensajeACifrar);
		byte[] resumen = messageDigest.digest();
		return Format.bytesToHex(resumen);
    }
    
}
