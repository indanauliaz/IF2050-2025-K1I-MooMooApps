package moomoo.apps;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;

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


    @Test
    @DisplayName("Setter: Mengubah properti pengguna harus berhasil")
    void testSettersFunctionality() {
        // Arrange
        UserModel user = new UserModel(1, "initialUser", "initial@test.com", "Manajer");

        // Act
        String newUsername = "updatedUser";
        user.setUsername(newUsername); 

        String newRole = "Pemilik";
        user.setRole(newRole);
        // Assert
        assertEquals(newUsername, user.getUsername(), "setUsername harus memperbarui username.");
        assertEquals(newRole, user.getRole(), "setRole harus memperbarui role.");
    }

    @Test
    @DisplayName("Equality: Dua objek pengguna dengan ID sama harus dianggap equal")
    void testEqualsAndHashCode() {
        // Arrange
        UserModel user1 = new UserModel(1, "budi", "budi@mail.com", "Staf");
        UserModel user2 = new UserModel(1, "budi", "budi@mail.com", "Staf"); // Objek identik
        UserModel user3 = new UserModel(2, "andi", "andi@mail.com", "Staf"); // Objek berbeda

        // Assert
    
        assertEquals(user1, user2, "Dua pengguna dengan data identik harus equal.");
        assertEquals(user1.hashCode(), user2.hashCode(), "HashCode dari dua pengguna identik harus sama.");
        assertNotEquals(user1, user3, "Dua pengguna dengan ID berbeda tidak boleh equal.");
    }
}