package pg.net.ai_services.utils;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public final class CryptoUtils {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_LENGTH = 12;
    private static final byte[] SALT = "ai-services-pg".getBytes(StandardCharsets.UTF_8);

    private CryptoUtils() {
    }

    public static String encode(String plainText) {
        try {
            SecretKey key = deriveKey();
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
            System.arraycopy(encrypted, 0, combined, IV_LENGTH, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    public static String decode(String cipherText) {
        try {
            byte[] combined = Base64.getDecoder().decode(cipherText);

            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);

            byte[] encrypted = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, IV_LENGTH, encrypted, 0, encrypted.length);

            SecretKey key = deriveKey();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Decryption failed", e);
        }
    }

    private static SecretKey deriveKey() throws Exception {
        String rawKey = System.getProperty("HASH_PASSWORD");
        if (rawKey == null || rawKey.isBlank()) {
            throw new IllegalStateException("HASH_PASSWORD not loaded — check .env");
        }
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(rawKey.toCharArray(), SALT, 65536, 256);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }
}
