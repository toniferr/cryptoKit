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
	 public static String cifrarBase64(Integer index,String texto){
	 	if (!Validation.validarDatos(texto)){
	 		return Constantes.DATOS_INCORRECTOS;
	 	} else {
	 		if (index == 0) {
	 			return new String(java.util.Base64.getEncoder().encode(texto.getBytes()));
	 		} else {
	 			return "Error de la aplicación al lanzar algoritmo";
	 		}
	 	}
	 }
    
    /**
     * 
     * @param index
     * @param texto
     * @return string - cadena descifrada Base64
     */
    public static String descifrarBase64(Integer index,String texto){
    	if (!Validation.validarDatos(texto)){
    		return Constantes.DATOS_INCORRECTOS;
    	}else{
	    	if (index == 0 ) {
	        	return new String(java.util.Base64.getDecoder().decode(texto));
	    	} else {
	        	return "Error de la aplicación al lanzar algoritmo";
	    	}
    	}
    }
}
