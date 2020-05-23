package es.toni.crytpoMain.symmetric;

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

import es.toni.crytpoMain.utils.Constantes;
import es.toni.crytpoMain.utils.Format;
import es.toni.crytpoMain.utils.Validation;

public class AlgSimetrico {
	
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
    	if (!Validation.validarDatos(index,texto)){
    		return Constantes.DATOS_INCORRECTOS;
    	}else{
    		switch(index){
	    		case 0:
	    			return AlgoritmoAES.cifrarAESPKCS5(texto,clave,vector);
	    		case 1:
	    			return AlgoritmoAES.cifrarAESPKCS7(texto,clave,vector);
	    		case 2:
	    			return AlgoritmoDES.cifrarDES(texto,clave,0);
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
    	if (!Validation.validarDatos(index,texto)){
    		return Constantes.DATOS_INCORRECTOS;
    	}else{
    		switch(index){
    		case 0:
    			return AlgoritmoAES.descifrarAESPKCS5(texto,clave,vector);
    		case 1:
    			return AlgoritmoAES.descifrarAESPKCS7(texto,clave,vector);
    		case 2:
    			return AlgoritmoDES.descifrarDES(texto,clave);
    		default:
    			return "";
    		}
    	}
    }
  
}
