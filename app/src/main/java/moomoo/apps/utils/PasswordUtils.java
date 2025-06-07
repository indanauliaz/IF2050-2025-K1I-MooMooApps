package moomoo.apps.utils;

import java.security.MessageDigest;
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
        try {
            int iterations = 10000; // Secure iteration count
            int keyLength = 256; // Key length in bits
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
                // Format hash tidak sesuai
                System.err.println("Format storedPasswordHash tidak valid.");
                return false;
            }
            byte[] salt = Base64.getDecoder().decode(parts[0]);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedAttempt = md.digest(plainPassword.getBytes());
            String encodedHashedAttempt = Base64.getEncoder().encodeToString(hashedAttempt);

            return encodedHashedAttempt.equals(parts[1]);
        } catch (NoSuchAlgorithmException | IllegalArgumentException e) {
            System.err.println("Error verifying password: " + e.getMessage());
            return false;
        }
    }
}
