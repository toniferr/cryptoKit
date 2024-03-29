package es.toni.crypto.symmetric;

import es.toni.crypto.utils.Constantes;
import es.toni.crypto.utils.Validation;

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
	    			return AlgoritmoBlowfish.cifrarBFcbcPKCS5(texto, clave, vector);
	    		case 6:
	    			return AlgoritmoBlowfish.cifrarBFecbPKCS5(texto, clave);
				case 7:
					return AlgoritmoAESJasypt.cifrarAESJasypt(texto, clave, vector);
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
    			return AlgoritmoBlowfish.descifrarBFcbcPKCS5(texto, clave, vector);
    		case 6:
    			return AlgoritmoBlowfish.descifrarBFecbPKCS5(texto, clave);
			case 7:
				return AlgoritmoAESJasypt.descifrarAESJasypt(texto, clave, vector);
    		default:
    			return "Error de la aplicación al lanzar algoritmo";
    		}
    	}
    }
  
}
