package es.toni.crytpoMain;

public class UtilsFormat {

    /**
     * 
     * @param hex
     * @return
     */
    public static String bytesToHex(byte[] datos)  {
//    	try {
//    		String hex= new String(datos);
//    		String s = "";
//			for (int i=0; i < datos.length; i++){
//				s += (char)(datos[i]);
//			}
//	        StringBuilder str = new StringBuilder();
//	        for (int i=0;i < hex.length();i+=2){
//	        	str.append((char)Integer.parseInt(hex.substring(i, i+2),16));
//	        }
//	        return str.toString();
//    	}catch(Exception e){
//    		return e.toString();
//    	}
    	//
//		String s = "";
//		for (int i=0; i < resumen.length; i++){
//			s += Integer.toHexString((resumen[i] >> 4) & 0xf);
//			s += Integer.toHexString(resumen[i] & 0xf);
//		}
//		return s;
//    	char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
//    	char[] hexChars = new char[datos.length * 2];
//    	for (int j=0; j < datos.length; j++){
//    		int v = datos[j] & 0xFF;
//    		hexChars[j * 2] = hexArray[v >> 4];
//    		hexChars[j * 2 +1]= hexArray[v & 0x0F];
//    	}
//    	return new String(hexChars);

        StringBuilder localStringBuilder = new StringBuilder();
        for (int i = 0; i < datos.length; i++)  {
            String str;
            if ((str=Integer.toHexString(datos[i]&0xFF)).length()==1) {
                localStringBuilder.append(0);
            }
        localStringBuilder.append(str);
        }
        return localStringBuilder.substring(0, localStringBuilder.length());
    }
    
    /**
     * 
     * @param hex
     * @return
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
     * @param data
     * @return
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
