package es.toni.crytpo.symmetric;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.paddings.PKCS7Padding;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;

import es.toni.crytpo.utils.Format;

public class AlgoritmoAES {
	
	  
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
    public static String cifrarAESPKCS7(String texto,String clave){
    	try{
    		PaddedBufferedBlockCipher pbbc = new PaddedBufferedBlockCipher(new AESEngine(), new PKCS7Padding());
	        pbbc.init(true, new KeyParameter(clave.getBytes()));
	        byte[] output = new byte[pbbc.getOutputSize(texto.getBytes().length)];
	        int bytesWrittenOut = pbbc.processBytes(texto.getBytes(), 0, texto.getBytes().length, output, 0);
	        pbbc.doFinal(output, bytesWrittenOut);
	        return Format.bytesToHex(output);			
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
    public static String descifrarAESPKCS7(String texto,String clave){
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
	        return Format.bytesToHex(output);
    	}catch (Exception e){
    		return e.toString();
    	}
    }
    

}
