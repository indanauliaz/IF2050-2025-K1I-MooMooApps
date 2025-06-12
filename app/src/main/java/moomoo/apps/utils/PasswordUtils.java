package moomoo.apps.utils;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtils {

    private static final int SALT_LENGTH = 16;


    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }


    public static String hashPassword(String password, byte[] salt) {

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password tidak boleh null atau kosong");
        }
        try {
            int iterations = 10000; 
            int keyLength = 256; 
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hashedPassword = skf.generateSecret(spec).getEncoded();

            String encodedSalt = Base64.getEncoder().encodeToString(salt);
            String encodedHashedPassword = Base64.getEncoder().encodeToString(hashedPassword);
            
            return encodedSalt + ":" + encodedHashedPassword; 
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }


    public static boolean verifyPassword(String plainPassword, String storedPasswordHash) {
    try {
        String[] parts = storedPasswordHash.split(":");
        if (parts.length != 2) {
            System.err.println("Format storedPasswordHash tidak valid.");
            return false;
        }
        
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        String storedHashedPassword = parts[1];

        int iterations = 10000;
        int keyLength = 256;
        PBEKeySpec spec = new PBEKeySpec(plainPassword.toCharArray(), salt, iterations, keyLength);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] attemptHashedPasswordBytes = skf.generateSecret(spec).getEncoded();
    
        String attemptHashedPassword = Base64.getEncoder().encodeToString(attemptHashedPasswordBytes);

        return attemptHashedPassword.equals(storedHashedPassword);

    } catch (java.security.spec.InvalidKeySpecException | NoSuchAlgorithmException e) {
        System.err.println("Error saat memverifikasi password: " + e.getMessage());
        e.printStackTrace();
        return false;
    } catch (IllegalArgumentException e) {
        System.err.println("Error decoding Base64: " + e.getMessage());
        return false;
    }
}
}
