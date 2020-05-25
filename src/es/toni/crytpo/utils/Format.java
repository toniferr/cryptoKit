package es.toni.crytpo.utils;

public class Format {

    /**
     * 
     * @param datos
     * @return String
     */
    public static String bytesToHex(byte[] datos)  {

        StringBuilder localStringBuilder = new StringBuilder();
        for (int i = 0; i < datos.length; i++)  {
            String str;
            if ((str=Integer.toHexString(datos[i]&0xFF)).length()==1) {
                localStringBuilder.append(0);
            }
            localStringBuilder.append(str);
        }
        return localStringBuilder.substring(0, localStringBuilder.length());
        /* StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < datos.length; i++) {
        String hex = Integer.toHexString(0xff & datos[i]);
        if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString(); */
    }
    
    /**
     * 
     * @param hex
     * @return String
     */
    public static String stringFromHex(String hex)  {
    	try {
	        StringBuilder str = new StringBuilder();
	        for (int i=0;i < hex.length();i+=2){
	        	str.append((char)Integer.parseInt(hex.substring(i, i+2),16));
	        }
	        return str.toString();
    	}catch(Exception e){
    		return e.toString();
    	}
    }
    
    /**
     * 
     * @param datos
     * @return String
     */
    public static String hexFromString(byte[] datos)  {
    	try {
	        StringBuilder str = new StringBuilder();
	        for(int i=0; i < datos.length;i++){
	        	str.append(String.format("%02x", datos[i]));
	        }
	        return str.toString();
    	}catch(Exception e){
    		return e.toString();
    	}
    }
    
}
