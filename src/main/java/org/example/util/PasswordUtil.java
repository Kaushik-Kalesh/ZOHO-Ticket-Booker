package org.example.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

public class PasswordUtil {
    public static String hashPassword(String password, String usernameSalt) {
        if (password == null || usernameSalt == null) return null;
        try {
            byte[] salt = usernameSalt.getBytes(StandardCharsets.UTF_8);
            int iterations = 120_000;
            int keyLength = 256;
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
            byte[] derived = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(derived);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2WithHmacSHA512 not available", e);
        }
    }
}
