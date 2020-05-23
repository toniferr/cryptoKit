package es.toni.crytpo.utils;

public class Validation {

    /**
     * 
     * @param index
     * @param text
     * @return
     */
    public static boolean validarDatos(Integer index, String text){
    	if (text.equals("")) {
    		return false;
    	}
    	return true;
    }
    
}
