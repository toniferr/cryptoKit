package es.toni.crytpoMain;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UtilsHash {
	
    /**
     * 
     * @param index
     * @param texto
     * @return string - cadena cifrada Hash
     */
    public static String cifrarHash(Integer index,String texto){
    	if (!UtilsValidation.validarDatos(index,texto)){
    		return Constantes.DATOS_INCORRECTOS;
    	}else{
    		switch(index){
    		case 0:
    			return cifrarMD5_SHA1(texto,0);
    		case 1:
    			return cifrarMD5_SHA1(texto,1);
    		default:
    			return "";
    		}
    	}
    }
    
    /**
     * 
     * @param texto
     * @return string - cadena cifrada MD5 
     */
    public static String cifrarMD5_SHA1(String texto,Integer tipo){

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
		} catch (NoSuchAlgorithmException e) {
			return "Error de la aplicación al lanzar algoritmo";
		} 
		messageDigest.update(mensajeACifrar);
		byte[] resumen = messageDigest.digest();
		return UtilsFormat.bytesToHex(resumen);
    }
    
}
