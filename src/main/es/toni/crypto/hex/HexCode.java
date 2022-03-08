package es.toni.crypto.hex;

import org.bouncycastle.util.encoders.Hex;

import es.toni.crypto.utils.Constantes;
import es.toni.crypto.utils.Validation;

public class HexCode {
	
	
    /**
     * 
     * @param texto
     * @return string - cadena cifrada
     */
    public static String cifrarStringToHex(String texto){
    	if (!Validation.validarDatos(texto)){
    		return Constantes.DATOS_INCORRECTOS;
    	}else{
    		return cifrar(texto);
    	}
    }
    
    /**
     * 
     * @param texto
     * @return string - cadena descifrada
     */
    public static String descifrarHexToString(String texto){
    	if (!Validation.validarDatos(texto)){
    		return Constantes.DATOS_INCORRECTOS;
    	}else{
    		return descifrar(texto);
    	}
    } 
    
    /**
     * 
     * @param texto
     * @return string - cadena cifrada
     */
    public static String cifrar(String texto){
		try {
    		return new String(Hex.encode(texto.getBytes()));
		} catch (Exception e) {
			return e.toString();
		}
    }

    /**
     * 
     * @param texto
     * @return string - cadena descifrada 
     */
    public static String descifrar(String texto){
    	try {
    		return new String(Hex.decode(texto.getBytes()));	
    	} catch (Exception e) {
    		return e.toString();
    	}
    }
    
}
