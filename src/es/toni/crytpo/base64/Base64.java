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
		    	switch(index){
		        case 0: 
		        	return new String(java.util.Base64.getEncoder().encode(texto.getBytes()));
		        case 1:
		        	return new String(org.bouncycastle.util.encoders.Base64.encode(texto.getBytes()));
		        default: 
		        	return "";
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
	    	switch(index){
	        case 0: 
	        	return new String(java.util.Base64.getDecoder().decode(texto));
	        case 1: 
	        	return new String(org.bouncycastle.util.encoders.Base64.decode(texto));
	        default: 
	        	return "";
	    	}
    	}
    }
}
