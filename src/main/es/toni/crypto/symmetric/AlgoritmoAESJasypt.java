package es.toni.crypto.symmetric;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.iv.RandomIvGenerator;
import org.jasypt.util.text.AES256TextEncryptor;

public class AlgoritmoAESJasypt {

    /**
     *
     * @param texto
     * @return string - cadena descifrada AES con jasypt
     * @throws Exception
     */
    public static String cifrarAESJasypt(String texto,String clave, String vector){
        try{
           /* AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
            textEncryptor.setPassword(clave);
            return textEncryptor.encrypt(texto);*/
            StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
            encryptor.setPassword(clave);                         // we HAVE TO set a password
            encryptor.setAlgorithm("PBEWithHMACSHA512AndAES_256");   // optionally set the algorithm
            encryptor.setIvGenerator(new RandomIvGenerator());       // for PBE-AES-based algorithms, the IV generator is MANDATORY
            return encryptor.encrypt(texto);
        }catch (Exception e){
            return e.toString();
        }
    }

    /**
     *
     * @param texto
     * @return string - cadena descifrada AES con jasypt
     * @throws Exception
     */
    public static String descifrarAESJasypt(String texto,String clave, String vector){
        try{
/*            AES256TextEncryptor textEncryptor = new AES256TextEncryptor();
            textEncryptor.setPassword(clave);
            return textEncryptor.encrypt(texto);*/
            StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
            encryptor.setPassword(clave);                         // we HAVE TO set a password
            encryptor.setAlgorithm("PBEWithHMACSHA512AndAES_256");   // optionally set the algorithm
            encryptor.setIvGenerator(new RandomIvGenerator());       // for PBE-AES-based algorithms, the IV generator is MANDATORY
            return encryptor.decrypt(texto);
        }catch (Exception e){
            return e.toString();
        }
    }
}
