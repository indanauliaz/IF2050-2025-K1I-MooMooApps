package moomoo.apps;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import moomoo.apps.model.UserModel;

/**
 * Kelas ini berisi unit test untuk UserModel.
 */
class UserModelTest {

    @Test
    void testUserModelCreationAndGetters() {
        // Arrange (Persiapan)
        int id = 1;
        String username = "testuser";
        String email = "test@example.com";
        String role = "Manajer";

        // Act (Aksi)

        UserModel user = new UserModel(id, username, email, role);

        // Assert (Pengecekan)
        assertEquals(id, user.getId(), "ID pengguna harus sesuai dengan yang diinput.");
        assertEquals(username, user.getUsername(), "Username harus sesuai dengan yang diinput.");
        assertEquals(email, user.getEmail(), "Email harus sesuai dengan yang diinput.");
        assertEquals(role, user.getRole(), "Role harus sesuai dengan yang diinput.");
    }

    @Test
    void testSecondConstructor() {
        // Arrange (Persiapan)
        String username = "newuser";
        String email = "new@example.com";
        String password = "password123";
        String role = "Pemilik";

        // Act (Aksi)
        UserModel user = new UserModel(username, email, password, role);

        // Assert (Pengecekan)
        assertEquals(0, user.getId(), "ID default harus 0 untuk konstruktor ini.");
        assertEquals(username, user.getUsername(), "Username dari konstruktor kedua harus benar.");
        assertEquals(email, user.getEmail(), "Email dari konstruktor kedua harus benar.");
        assertEquals(password, user.getPassword(), "Password dari konstruktor kedua harus benar.");
        assertEquals(role, user.getRole(), "Role dari konstruktor kedua harus benar.");
    }
}