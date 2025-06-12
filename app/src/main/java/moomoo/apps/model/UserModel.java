package moomoo.apps.model;

import java.util.Objects;

public class UserModel {
    private int id; 
    private String username;
    private String email;
    private String password; 
    private String role;


    public UserModel(int id, String username, String email, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = null; 
        this.role = role;
    }


    public UserModel(String username, String email, String password, String role) {
        this.username = username;
        this.email = email;
        this.password = password; 
        this.role = role;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "UserModel{" +
               "id=" + id + 
               ", username='" + username + '\'' +
               ", email='" + email + '\'' +
               ", role='" + role + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserModel userModel = (UserModel) o;
        if (id != 0 && userModel.id != 0) {
            return id == userModel.id;
        }
        return id == userModel.id &&
                Objects.equals(username, userModel.username) &&
                Objects.equals(email, userModel.email) &&
                Objects.equals(role, userModel.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, email, role);
    }
}