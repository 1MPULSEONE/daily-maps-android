package com.x3noku.daily_maps_android;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;

class Aes256Class {
    private SecretKey secretKey;

    Aes256Class() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256);
            this.secretKey = keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    private byte[] makeAes(byte[] rawMessage, int cipherMode){
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(cipherMode, this.secretKey);
            return cipher.doFinal(rawMessage);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    String encryptString(String message) {
        byte[] encryptedMessage = makeAes(message.getBytes(), Cipher.ENCRYPT_MODE);
        return new String( encryptedMessage );
    }

    String decryptString(String encryptedMessage ) {
        byte[] decryptedMessage = makeAes(encryptedMessage.getBytes(), Cipher.DECRYPT_MODE);
        return new String( decryptedMessage );
    }
}