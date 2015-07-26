package com.score.senzors.utils;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import com.score.senzors.exceptions.RsaKeyException;
import javax.crypto.Cipher;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Public functions to handle all RSA key related functions
 * We do
 *      1. Encryption/decryption
 *      2. Save RSA keys
 *      3. Extract RSA keys
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class CryptoUtils {

    private static final String TAG = CryptoUtils.class.getName();

    /**
     * Save public/private keys for further usage.
     * Need to encode the key with Base64 and save it as a string, We use shared preference
     * to store the key
     * @param context application context
     * @param key public/private key
     * @param keyType public/private/server key
     */
    public static void saveRsaKey(Context context, Key key, String keyType) {
        // get bytes from key and save in shared preference
        String keyString =  Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);
        PreferenceUtils.saveRsaKey(context, keyString, keyType);
    }

    /**
     * Save server public key for further usage(all the messages need to be encrypt with this key)
     * Server key receives as Base64 encoded PEM format stream, so need to decode the stream
     * in order to extract the key
     * @param context application context
     * @param encodedKey Base64 encoded PEM stream
     */
    public static void saveServerPublicKey(Context context, String encodedKey) throws UnsupportedEncodingException {
        byte[] encodedKeyBytes = Base64.decode(encodedKey, Base64.DEFAULT);
        String pemFormatKey = new String(encodedKeyBytes, "UTF-8");

        // remove BEGIN and END from key(convert PEM to DMG format)
        String dmgFormatKey = pemFormatKey.replace("-----BEGIN PUBLIC KEY-----\n", "");
        dmgFormatKey = dmgFormatKey.replace("-----END PUBLIC KEY-----", "");

        PreferenceUtils.saveRsaKey(context, dmgFormatKey, "server_key");
    }

    /**
     * Get RSA public key of server. public keys stored in shared preference as string,
     * So need to get that string and convert it as PublicKey
     * @param context application context
     * @return public key
     */
    public static PublicKey getServerPublicKey(Context context) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // get key string from shared preference
        String keyString = PreferenceUtils.getRsaKey(context, "server_key");

        // convert to public key
        X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.decode(keyString, Base64.DEFAULT));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey publicKey = kf.generatePublic(spec);

        System.out.println("-----------------------");
        System.out.println(publicKey.getAlgorithm());
        System.out.println(publicKey.getFormat());
        System.out.println("-----------------------");

        return publicKey;
    }

    /**
     * Get RSA public key. public keys stored in shared preference as string,
     * So need to get that string and convert it as PublicKey
     * @param context application context
     * @return public key
     */
    public static PublicKey getRsaPublicKey(Context context) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // get key string from shared preference
        String keyString = PreferenceUtils.getRsaKey(context, "public_key");

        // convert to public key
        X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.decode(keyString, Base64.DEFAULT));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey publicKey = kf.generatePublic(spec);

        return publicKey;
    }

    /**
     * Get RSA private key. private keys stored in shared preference as string,
     * So need to get that string and convert it as PrivateKey
     * @param context application context
     * @return private key
     */
    public static PrivateKey getRsaPrivateKey(Context context) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // get key string from shared preference
        String keyString = PreferenceUtils.getRsaKey(context, "private_key");

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64.decode(keyString, Base64.DEFAULT));
        KeyFactory kf = KeyFactory.getInstance("RSA");

        return kf.generatePrivate(spec);
    }

    /**
     * Encrypt message with server public key
     * @param message message to encrypt
     * @return encrypted message
     */
    public static String encryptMessage(Context context, String message) throws RsaKeyException {
        try {
            // encrypt with server public key
            PublicKey publicKey = getServerPublicKey(context);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte []encodedBytes = cipher.doFinal(message.getBytes("UTF-8"));
            String encryptedMessage = Base64.encodeToString(encodedBytes, Base64.DEFAULT);
            //String encryptedMessage = new String(Base64.encode(encodedBytes, Base64.DEFAULT), "UTF-8");
            Log.d(TAG, "Encrypted message : " + encryptedMessage);

            return encryptedMessage;
        } catch (Exception e) {
            Log.e(TAG, "RSA encryption error " + e.getMessage());

            throw new RsaKeyException();
        }
    }

    /**
     * Encode string with SHA1
     * @param message message to encode
     * @return base64 encoded message
     */
    public static String encodeMessage(String message) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        messageDigest.update(message.getBytes("iso-8859-1"), 0, message.length());
        byte[] sha1hash = messageDigest.digest();

        // return convertToHex(sha1hash);
        return Base64.encodeToString(sha1hash, Base64.DEFAULT).replace("\n", "").replace("\r", "");
    }

    /**
     * Convert byte array to hex
     * @param data byte array
     * @return hex string
     */
    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte b : data) {
            int halfbyte = (b >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
                halfbyte = b & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }
}
