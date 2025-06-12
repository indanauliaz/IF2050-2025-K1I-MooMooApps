package moomoo.apps;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;

import moomoo.apps.utils.PasswordUtils;

/**
 * Kelas ini berisi unit test untuk PasswordUtils.
 */
class PasswordUtilsTest {

    @Test
    void testPasswordHashingAndVerification() {
        String myPassword = "password123";
        System.out.println("Testing password verification for: " + myPassword);

        byte[] salt = PasswordUtils.generateSalt();
        String hashedPassword = PasswordUtils.hashPassword(myPassword, salt);
        boolean isPasswordCorrect = PasswordUtils.verifyPassword(myPassword, hashedPassword);
        boolean isPasswordIncorrect = PasswordUtils.verifyPassword("wrongpassword", hashedPassword);

        assertNotNull(hashedPassword, "Hashed password tidak boleh null.");
        assertNotEquals(myPassword, hashedPassword, "Hashed password tidak boleh sama dengan password asli.");
        assertTrue(isPasswordCorrect, "Verifikasi untuk password yang benar harus mengembalikan true.");
        assertFalse(isPasswordIncorrect, "Verifikasi untuk password yang salah harus mengembalikan false.");

        System.out.println("Password verification test passed.");
    }

    @Test
    @DisplayName("Hashing: Password null atau kosong harus melempar exception")
    void testHashingWithInvalidPasswords() {
        // Arrange
        byte[] salt = PasswordUtils.generateSalt();

        assertThrows(IllegalArgumentException.class, () -> {
            PasswordUtils.hashPassword(null, salt);
        }, "Password null seharusnya melempar IllegalArgumentException.");

        assertThrows(IllegalArgumentException.class, () -> {
            PasswordUtils.hashPassword("", salt);
        }, "Password kosong seharusnya melempar IllegalArgumentException.");
    }

    @Test
    @DisplayName("Hashing: Salt yang berbeda harus menghasilkan hash yang berbeda")
    void testDifferentSaltsProduceDifferentHashes() {
        // Arrange
        String password = "securepassword";
        
        // Act
        byte[] salt1 = PasswordUtils.generateSalt();
        String hashedPassword1 = PasswordUtils.hashPassword(password, salt1);

        byte[] salt2 = PasswordUtils.generateSalt();
        String hashedPassword2 = PasswordUtils.hashPassword(password, salt2);


        assertNotNull(hashedPassword1);
        assertNotNull(hashedPassword2);
        assertNotEquals(hashedPassword1, hashedPassword2, "Hash dari salt yang berbeda tidak boleh sama.");
    }
}