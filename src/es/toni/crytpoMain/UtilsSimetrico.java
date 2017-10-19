package es.toni.crytpoMain;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;

public class UtilsSimetrico {
	
    /**
     * 
     * @param index
     * @param texto
     * @return string - cadena cifrada simetrico
     * @throws BadPaddingException 
     * @throws IllegalBlockSizeException 
     * @throws InvalidAlgorithmParameterException 
     * @throws NoSuchPaddingException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     * @throws UnsupportedEncodingException 
     * @throws InvalidKeySpecException 
     */
    public static String cifrarSimetrico(Integer index,String texto,String clave,String vector){
    	if (!UtilsValidation.validarDatos(index,texto)){
    		return Constantes.DATOS_INCORRECTOS;
    	}else{
    		switch(index){
    		case 0:
    			return cifrarAESPKCS5(texto,clave,vector);
    		case 1:
    			return cifrarAESPKCS7(texto,clave,vector);
    		case 2:
    			return cifrarDES(texto,clave,0);
    		default:
    			return "";
    		}
    	}
    }
    
    /**
     * 
     * @param index
     * @param texto
     * @return string - cadena cifrada simetrico
     * @throws BadPaddingException 
     * @throws IllegalBlockSizeException 
     * @throws InvalidAlgorithmParameterException 
     * @throws NoSuchPaddingException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidKeyException 
     */
    public static String descifrarSimetrico(Integer index,String texto,String clave,String vector){
    	if (!UtilsValidation.validarDatos(index,texto)){
    		return Constantes.DATOS_INCORRECTOS;
    	}else{
    		switch(index){
    		case 0:
    			return descifrarAESPKCS5(texto,clave,vector);
    		case 1:
    			return descifrarAESPKCS7(texto,clave,vector);
    		case 2:
    			return descifrarDES(texto,clave);
    		default:
    			return "";
    		}
    	}
    }
    
    /**
     * 
     * @param texto
     * @return string - cadena descifrada AES 
     * @throws NoSuchPaddingException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidAlgorithmParameterException 
     * @throws InvalidKeyException 
     * @throws BadPaddingException 
     * @throws IllegalBlockSizeException 
     */
    public static String cifrarAESPKCS5(String texto,String clave,String vector){
    	try{
    		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			SecretKeySpec skeySpec = new SecretKeySpec(clave.getBytes(),"AES");
			IvParameterSpec ivParameterSpec = new IvParameterSpec(vector.getBytes());
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParameterSpec);
			byte[] encrypted = cipher.doFinal(texto.getBytes());
			return new String(java.util.Base64.getEncoder().encode((encrypted)));
    	}catch (Exception e){
    		return e.toString();
    	}
    }
    
    /**
     * 
     * @param texto
     * @return string - cadena descifrada AES 
     * @throws NoSuchPaddingException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidAlgorithmParameterException 
     * @throws InvalidKeyException 
     * @throws BadPaddingException 
     * @throws IllegalBlockSizeException 
     */
    public static String cifrarAESPKCS7(String texto,String clave,String vector){
    	try{
    		PaddedBufferedBlockCipher pbbc = new PaddedBufferedBlockCipher(new AESEngine(), new PKCS7Padding());
	        pbbc.init(true, new KeyParameter(clave.getBytes()));
	        byte[] output = new byte[pbbc.getOutputSize(texto.getBytes().length)];
	        int bytesWrittenOut = pbbc.processBytes(texto.getBytes(), 0, texto.getBytes().length, output, 0);
	        pbbc.doFinal(output, bytesWrittenOut);
	        return UtilsFormat.bytesToHex(output);			
    	}catch (Exception e){
    		return e.toString();
    	}
    }
    
    /**
     * 
     * @param texto
     * @return string - cadena cifrada AES 
     * @throws NoSuchPaddingException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidAlgorithmParameterException 
     * @throws InvalidKeyException 
     * @throws BadPaddingException 
     * @throws IllegalBlockSizeException 
     */
    public static String descifrarAESPKCS5(String texto,String clave,String vector){
    	try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			SecretKeySpec skeySpec = new SecretKeySpec(clave.getBytes(),"AES");
			IvParameterSpec ivParameterSpec = new IvParameterSpec(vector.getBytes());
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivParameterSpec);
			byte[] decrypted = cipher.doFinal(java.util.Base64.getDecoder().decode((texto)));
			return new String(decrypted);
    	}catch (Exception e){
    		return e.toString();
    	}
    }
    
    /**
     * 
     * @param texto
     * @return string - cadena cifrada AES 
     * @throws NoSuchPaddingException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidAlgorithmParameterException 
     * @throws InvalidKeyException 
     * @throws BadPaddingException 
     * @throws IllegalBlockSizeException 
     */
    public static String descifrarAESPKCS7(String texto,String clave,String vector){
    	try {
	        PaddedBufferedBlockCipher pbbc = new PaddedBufferedBlockCipher(new AESEngine(), new PKCS7Padding());
	        pbbc.init(false, new KeyParameter(clave.getBytes()));
	        byte[] textoCifrado = DatatypeConverter.parseHexBinary(texto);
	        byte[] output = new byte[pbbc.getOutputSize(textoCifrado.length)];
	        int bytesWrittenOut = pbbc.processBytes(textoCifrado, 0, textoCifrado.length, output, 0);
	        pbbc.doFinal(output, bytesWrittenOut);
	        int i = output.length-1;
	        while(output[i] == 0x00) {
	            i--;
	        }
	        byte res0[] = new byte[i+1];
	        System.arraycopy(output, 0, res0, 0, res0.length);
	        return UtilsFormat.bytesToHex(output);
    	}catch (Exception e){
    		return e.toString();
    	}
    }
    
    /**
     * 
     * @param texto
     * @return string - cadena descifrada DES  
     */
    public static String cifrarDES(String texto,String clave,Integer tipo){
    	try {
	    	SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
	    	DESKeySpec kspec = new DESKeySpec(clave.getBytes());
	    	SecretKey ks = skf.generateSecret(kspec);
	    	
	    	Cipher cifrado = Cipher.getInstance("DES/ECB/PKCS5Padding");
	    	cifrado.init(Cipher.ENCRYPT_MODE, ks);
	    	byte[] bloqueCifrado = cifrado.doFinal(texto.getBytes());
	    	return new String(java.util.Base64.getEncoder().encode((bloqueCifrado)));
    	}catch(Exception e){
    		return e.toString();
    	}
    }
    
    /**
     * 
     * @param texto
     * @return string - cadena descifrada DES  
     */
    public static String descifrarDES(String texto,String clave){
    	try {
	    	SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
	    	DESKeySpec kspec = new DESKeySpec(clave.getBytes());
	    	SecretKey ks = skf.generateSecret(kspec);
	    	
	    	Cipher cifrado = Cipher.getInstance("DES/ECB/PKCS5Padding");
	    	cifrado.init(Cipher.DECRYPT_MODE, ks);
	    	byte[] bloqueCifrado = cifrado.doFinal(java.util.Base64.getDecoder().decode((texto)));
	    	return new String(bloqueCifrado);
    	}catch(Exception e){
    		return e.toString();
    	}
    }
}
