package es.toni.crytpo.symmetric;

import es.toni.crytpo.utils.Constantes;
import es.toni.crytpo.utils.Validation;

public class AlgSimetrico {
	
    /**
     * 
     * @param index
     * @param texto
     * @param clave
     * @param vector
     * @return string - cadena cifrada simetrico
     */
    public static String cifrarSimetrico(Integer index,String texto,String clave,String vector){
    	if (!Validation.validarDatos(texto)){
    		return Constantes.DATOS_INCORRECTOS;
    	}else{
    		switch(index){
	    		case 0:
	    			return AlgoritmoDES.cifrarDEScbcPkcs5(texto,clave,vector);
	    		case 1:
	    			return AlgoritmoDES.cifrarDESecbPkcs5(texto,clave);
	    		case 2:
	    			return AlgoritmoAES.cifrarAESPKCS5(texto,clave,vector);
	    		case 3:
	    			return AlgoritmoAES.cifrarAESPKCS7(texto,clave);
	    		case 4:
	    			return AlgoritmoAES.cifrarAESgcmSinPadding(texto, clave, vector);
	    		case 5:
	    			return AlgoritmoBlowfish.cifrarBFSinPadding(texto, clave);
	    		default:
    			return "Error de la aplicación al lanzar algoritmo";
    		}
    	}
    }
    
    /**
     * 
     * @param index
     * @param texto
     * @param clave
     * @param vector
     * @return string - cadena cifrada simetrico
     */
    public static String descifrarSimetrico(Integer index,String texto,String clave,String vector){
    	if (!Validation.validarDatos(texto)){
    		return Constantes.DATOS_INCORRECTOS;
    	}else{
    		switch(index){
    		case 0:
    			return AlgoritmoDES.descifrarDEScbcPkcs5(texto,clave,vector);
    		case 1:
    			return AlgoritmoDES.descifrarDESecbPkcs5(texto,clave);
    		case 2:
    			return AlgoritmoAES.descifrarAESPKCS5(texto,clave,vector);
    		case 3:
    			return AlgoritmoAES.descifrarAESPKCS7(texto,clave);
    		case 4:
    			return AlgoritmoAES.descifrarAESgcmSinPadding(texto, clave, vector);
    		case 5:
    			return AlgoritmoBlowfish.descifrarBFSinPadding(texto, clave);
    		default:
    			return "Error de la aplicación al lanzar algoritmo";
    		}
    	}
    }
  
}
