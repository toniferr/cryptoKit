package es.toni.crytpo.base64;

import es.toni.crytpo.utils.Constantes;
import es.toni.crytpo.utils.Validation;

public class Base64 {
	
	  /**
	  * 
	  * @param index
	  * @param texto
	  * @return string - cadena cifrada Base64
	  */
	 public static String cifrarBase64(String texto){
	 	if (!Validation.validarDatos(texto)){
	 		return Constantes.DATOS_INCORRECTOS;
	 	} else {
	 		try {
	 			return new String(java.util.Base64.getEncoder().encode(texto.getBytes()));
	 		} catch(Exception e) {
	 			return e.toString();
	 		}
	 	}
	 }
    
    /**
     * 
     * @param index
     * @param texto
     * @return string - cadena descifrada Base64
     */
    public static String descifrarBase64(String texto){
    	if (!Validation.validarDatos(texto)){
    		return Constantes.DATOS_INCORRECTOS;
    	}else{
	    	try {
	        	return new String(java.util.Base64.getDecoder().decode(texto));
	    	} catch (Exception e) {
	        	return e.toString();
	    	}
    	}
    }
}
