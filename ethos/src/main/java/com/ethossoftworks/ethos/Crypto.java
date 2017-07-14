package com.ethossoftworks.ethos;


import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class Crypto {
    public static final String HASH_SHA256 = "SHA-256";
    public static final String HASH_SHA512 = "SHA-512";
    public static final String HMAC_SHA256 = "HmacSHA256";
    public static final String HMAC_SHA512 = "HmacSHA512";


    public static String encrypt(String input, String key) {
        return encrypt(input.getBytes(), key.getBytes());
    }


    public static String encrypt(byte[] input, byte[] key) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] iv = new byte[16];
            random.nextBytes(iv);
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeySpec keySpec = new SecretKeySpec(hash(key, HASH_SHA256), "AES");
            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, keySpec, ivspec);
            byte[] encrypted = c.doFinal(input);

            byte[] encryptedWithIv = new byte[encrypted.length + 16];
            System.arraycopy(encrypted, 0, encryptedWithIv, 0, encrypted.length);
            System.arraycopy(iv, 0, encryptedWithIv, encrypted.length, iv.length);

            return Base64.encodeToString(encryptedWithIv, 0, encryptedWithIv.length, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    public static String decrypt(String input, String key) {
        return decrypt(input, key.getBytes());
    }


    public static String decrypt(String input, byte[] key) {
        try {
            byte[] encodedBytes = Base64.decode(input, Base64.NO_WRAP);

            byte[] iv = Arrays.copyOfRange(encodedBytes, encodedBytes.length - 16, encodedBytes.length);
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(hash(key, HASH_SHA256), "AES");
            c.init(Cipher.DECRYPT_MODE, keySpec, ivspec);
            byte[] decrypted = c.doFinal(Arrays.copyOfRange(encodedBytes, 0, encodedBytes.length - 16));

            return new String(decrypted);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xFF;
            hexChars[i * 2] = hexArray[value >>> 4];
            hexChars[i * 2 + 1] = hexArray[value & 0x0F];
        }
        return new String(hexChars);
    }


    public static byte[] hash(String data, String algorithm) {
        return hash(data.getBytes(), algorithm);
    }


    public static byte[] hash(byte[] data, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(data);
            byte[] hash = md.digest();
            md.reset();
            return hash;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return new byte[]{};
    }


    public static String hexHash(String data, String algorithm) {
        return bytesToHex(hash(data, algorithm));
    }


    public static String hexHash(byte[] data, String algorithm) {
        return bytesToHex(hash(data, algorithm));
    }


    public static byte[] hmac(byte[] input, byte[] key, String algorithm) {
        try {
            Mac mac = Mac.getInstance(algorithm);
            SecretKeySpec keySpec = new SecretKeySpec(key, algorithm);
            mac.init(keySpec);
            byte[] hash = mac.doFinal(input);
            mac.reset();
            return hash;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[] {};
    }


    public static byte[] hmac(String input, String key, String algorithm) {
        return hmac(input.getBytes(), key.getBytes(), algorithm);
    }


    public static String hexHmac(byte[] input, byte[] key, String algorithm) {
        return bytesToHex(hmac(input, key, algorithm));
    }


    public static String hexHmac(String input, String key, String algorithm) {
        return bytesToHex(hmac(input, key, algorithm));
    }


    public static String sha256(String input) {
        return hexHash(input, HASH_SHA256);
    }


    public static String sha512(String input) {
        return hexHash(input, HASH_SHA512);
    }


    public static String hmac256(String input, String key) {
        return hexHmac(input, key, HMAC_SHA256);
    }


    public static String hmac512(String input, String key) {
        return hexHmac(input, key, HMAC_SHA512);
    }
}