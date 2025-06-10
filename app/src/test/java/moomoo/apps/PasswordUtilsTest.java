package moomoo.apps;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import moomoo.apps.utils.PasswordUtils;

/**
 * Kelas ini berisi unit test untuk PasswordUtils.
 */
class PasswordUtilsTest {

    @Test
    void testPasswordHashingAndVerification() {
        // Arrange (Persiapan)
        String myPassword = "password123";
        System.out.println("Testing password verification for: " + myPassword);

        // Act (Aksi)
        byte[] salt = PasswordUtils.generateSalt();
        String hashedPassword = PasswordUtils.hashPassword(myPassword, salt);
        boolean isPasswordCorrect = PasswordUtils.verifyPassword(myPassword, hashedPassword);
        boolean isPasswordIncorrect = PasswordUtils.verifyPassword("wrongpassword", hashedPassword);

        // Assert (Pengecekan)
        assertNotNull(hashedPassword, "Hashed password tidak boleh null.");
        assertNotEquals(myPassword, hashedPassword, "Hashed password tidak boleh sama dengan password asli.");
        assertTrue(isPasswordCorrect, "Verifikasi untuk password yang benar harus mengembalikan true.");
        assertFalse(isPasswordIncorrect, "Verifikasi untuk password yang salah harus mengembalikan false.");

        System.out.println("Password verification test passed.");
    }
}