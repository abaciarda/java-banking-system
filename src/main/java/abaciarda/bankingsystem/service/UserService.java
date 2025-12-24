package abaciarda.bankingsystem.service;

import abaciarda.bankingsystem.models.User;
import abaciarda.bankingsystem.types.AuthResponse;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {
    private final Connection connection;

    public UserService(Connection connection) {
        this.connection = connection;
    }

    public AuthResponse<User> registerUser(String name, String surname, String ssn, String password) throws SQLException {
        if (name == null || name.isBlank()) {
            return new AuthResponse<>(false, "İsim alanı boş bırakılamaz.", null);
        }

        if (surname == null || surname.isBlank()) {
            return new AuthResponse<>(false, "Soyad alanı boş bırakılamaz.", null);
        }

        if (ssn == null || ssn.isBlank()) {
            return new AuthResponse<>(false, "TC Kimlik numarası boş bırakılamaz.", null);
        }

        if (!ssn.matches("\\d{11}")) {
            return new AuthResponse<>(false, "TC Kimlik No 11 haneli ve sadece rakamlardan oluşmalıdır.", null);
        }

        if (password == null || password.isBlank()) {
            return new AuthResponse<>(false, "Şifre boş bırakılamaz.", null);
        }

        if (!isSSNUnique(ssn)) {
            return new AuthResponse<>(false, "Bu TC Kimlik numarasına ait başka bir hesap bulunmaktadır.", null);
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
        String sql = "INSERT INTO users (name, surname, ssn, password) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, surname);
            stmt.setString(3, ssn);
            stmt.setString(4, hashedPassword);
            stmt.executeUpdate();
            return new AuthResponse<>(true, "Hesap başarıyla oluşturuldu giriş yapabilirsiniz.", null);
        }
    }

    public boolean isSSNUnique(String ssn) throws SQLException{
        String sql = "SELECT name FROM users WHERE ssn = ?";

        try(PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, ssn);
            ResultSet res = stmt.executeQuery();

            return !res.next();
        }
    }

    public AuthResponse<User> authenticateUser(String ssn, String password) throws SQLException {
        if (ssn == null || ssn.isBlank()) {
            return new AuthResponse<>(false, "TC Kimlik No boş bırakılamaz", null);
        }

        if (!ssn.matches("\\d{11}")) {
            return new AuthResponse<>(false, "TC Kimlik No 11 haneli ve sadece rakamlardan oluşmalıdır.", null);
        }

        if (password == null || password.isBlank()) {
            return new AuthResponse<>(false, "Şifre boş bırakılamaz", null);
        }

        String sql = "SELECT id, name, surname, ssn, password FROM users WHERE ssn = ?";

        try(PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, ssn);
            ResultSet res = stmt.executeQuery();

            if (!res.next()) {
                return new AuthResponse<>(false, "Girdiğiniz bilgilerle uyuşan müşteri bulunamadı", null);
            }

            String hashedPassword = res.getString("password");

            if (!BCrypt.checkpw(password, hashedPassword)) {
                return new AuthResponse<>(false, "Şifreniz hatalı.", null);
            }

            User user = new User(
                    res.getInt("id"),
                    res.getString("name"),
                    res.getString("surname"),
                    res.getString("ssn")
            );

            return new AuthResponse<>(true, "Giriş başarılı!", user);
        }
    }



}
