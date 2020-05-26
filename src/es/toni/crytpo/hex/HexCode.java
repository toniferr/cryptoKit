package es.toni.crytpo.hex;

import org.bouncycastle.util.encoders.Hex;

import es.toni.crytpo.utils.Constantes;
import es.toni.crytpo.utils.Validation;

public class HexCode {
	
    /**
     * 
     * @param texto
     * @return string - cadena cifrada
     */
    public static String cifrarHexToString(String texto){
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
    public static String descifrarStringToHex(String texto){
    	if (!Validation.validarDatos(texto)){
    		return Constantes.DATOS_INCORRECTOS;
    	}else{
    		return descifrar(texto);
    	}
    }
    
    /**
     * 
     * @param texto
     * @param tipo
     * @return string - cadena cifrada 
     */
    public static String cifrar(String texto){    	
		return new String(Hex.encode(texto.getBytes()));		
    }
    
    /**
     * 
     * @param texto
     * @param tipo
     * @return string - cadena cifrada
     */
    public static String descifrar(String texto){
		return new String(Hex.decode(texto.getBytes()));
    }

}
