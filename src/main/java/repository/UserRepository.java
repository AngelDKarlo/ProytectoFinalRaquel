package repository;

import config.DatabaseConfig;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRepository {

    public User  findByEmail(String email) throws SQLException {
        String query = "SELECT * FROM users WHERE correo_electronico = ?";
        User user = null;

        try (Connection connection = DatabaseConfig.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, email);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    user = new User();
                    user.setId(resultSet.getInt("id"));
                    user.setEmail(resultSet.getString("correo_electronico"));
                    user.setNombre_usuario(resultSet.getString("nombre_usuario"));
                    user.setNombre_completo(resultSet.getString("nombre_completo"));
                    user.setFecha_nacimiento(resultSet.getDate("fecha_nac"));
                    user.setFecha_registro(resultSet.getDate("fecha_registro"));
                    user.setPassword_hash(resultSet.getString("contraseña_hash"));
                }
            }
        }
        return user;
    }

    public void save(User user) throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection()) {
            String query = "INSERT INTO users (nombre_usuario,nombre_completo,contraseña_hash, correo_electronico,fecha_registro, fecha_nac) VALUES (?,?,?,?,?,?)";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, user.getNombre_usuario());
            preparedStatement.setString(2, user.getNombre_completo());
            preparedStatement.setString(3, user.getPassword_hash());
            preparedStatement.setString(4, user.getEmail());
            preparedStatement.setDate(5, user.getFecha_registro());
            preparedStatement.setDate(6, user.getFecha_nacimiento());

            int insertedRows = preparedStatement.executeUpdate();

            if (insertedRows > 0) {
                System.out.println("Usuario guardado exitosamente");
            }

            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
