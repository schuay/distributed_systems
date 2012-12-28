package com.ds.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import com.ds.loggers.Log;

public class SecurityUtils {

    public static final String RSA = "RSA/NONE/OAEPWithSHA256AndMGF1Padding";
    public static final String AES = "AES/CTR/NoPadding";
    public static final String SHA256 = "HmacSHA256";

    public static final int CHALLENGE_BYTES = 32;
    public static final int IV_BYTES = 16;

    static {
        /* Set BouncyCastle as the system default in the context of this application. */
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
    }

    public static byte[] toBase64(byte[] from) {
        return Base64.encode(from);
    }

    public static byte[] fromBase64(byte[] from) {
        return Base64.decode(from);
    }

    public static PrivateKey readPrivateKey(String path, PasswordFinder finder) throws IOException {
        PEMReader in = null;
        try {
            in = new PEMReader(new FileReader(path), finder);
            KeyPair keyPair = (KeyPair) in.readObject();
            return keyPair.getPrivate();
        } finally {
            try { in.close(); } catch (Throwable t) { }
        }
    }

    public static PrivateKey readPrivateKey(String path) throws IOException {
        PasswordFinder finder = new PasswordFinder() {
            @Override
            public char[] getPassword() {
                char[] password = null;
                try {
                    // reads the password from standard input for decrypting the private key
                    System.out.println("Enter pass phrase:");
                    password = new BufferedReader(new InputStreamReader(System.in)).readLine().toCharArray();
                } catch (IOException e) {
                    Log.e(e.getLocalizedMessage());
                }

                return password;
            }
        };

        return readPrivateKey(path, finder);
    }

    public static PublicKey readPublicKey(String path) throws IOException {
        PEMReader in = null;

        try {
            in = new PEMReader(new FileReader(path));
            return (PublicKey)in.readObject();
        } finally {
            try { in.close(); } catch (Throwable t) { }
        }
    }

    public static byte[] getSecureRandom(int bytes) {
        SecureRandom secureRandom = new SecureRandom();
        final byte[] number = new byte[bytes];
        secureRandom.nextBytes(number);
        return number;
    }

    public static SecretKey generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        // KEYSIZE is in bits
        generator.init(256);
        return generator.generateKey();
    }

    public static Cipher getCipher(String algorithm, int mode, Key key, AlgorithmParameterSpec iv)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        // make sure to use the right ALGORITHM for what you want to do
        // (see text)
        Cipher crypt = Cipher.getInstance(algorithm);
        // MODE is the encryption/decryption mode
        // KEY is either a private, public or secret key
        // IV is an init vector, needed for AES
        crypt.init(mode, key, iv);
        return crypt;
    }

    public static SecretKey readSecretKey(byte[] bytes, String algorithm) {
        return new SecretKeySpec(bytes, algorithm);
    }

    public static SecretKey readSecretKey(String path, String algorithm) throws IOException {
        byte[] keyBytes = new byte[1024];
        FileInputStream fis = null;

        try {
            fis = new FileInputStream(path);
            fis.read(keyBytes);
        } finally {
            fis.close();
        }

        byte[] input = Hex.decode(keyBytes);
        return readSecretKey(input, algorithm);
    }

    public static byte[] getSignature(byte[] message, PrivateKey key)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature instance = Signature.getInstance("SHA1withRSA");
        instance.initSign(key);
        instance.update(message);
        return instance.sign();
    }

    public static boolean verifySignature(byte[] message, byte[] sig, PublicKey key)
            throws InvalidKeyException, SignatureException, NoSuchAlgorithmException {
        Signature instance = Signature.getInstance("SHA1withRSA");
        instance.initVerify(key);
        instance.update(message);
        return instance.verify(sig);
    }

    public static byte[] getHMAC(Key key, String algorithm, byte[] message) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac hMac = Mac.getInstance(algorithm);
        hMac.init(key);
        hMac.update(message);
        return hMac.doFinal();
    }

    public static boolean verifyHMAC(Key key, String algorithm, byte[] hmac, byte[] message) throws InvalidKeyException, NoSuchAlgorithmException {
        byte[] computedHash = getHMAC(key, algorithm, message);
        return MessageDigest.isEqual(computedHash, hmac);
    }

}
