package es.toni.crytpoMain;

public class UtilsValidation {

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
