package com.trading.cripto.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfig {

    private static final Properties properties = new Properties();

    static {
        try (InputStream in = DatabaseConfig.class.getClassLoader().getResourceAsStream("config.preoperties")) {
            if (in != null) {
                properties.load(in);
            } else {
                System.err.println("Cannot load config.properties");
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Método estático para obtener una nueva conexión a la base de datos.
     * @return Una conexión a la BD.
     * @throws SQLException si ocurre un error al conectar.
     */

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                properties.getProperty("db.url"),
                properties.getProperty("db.username"),
                properties.getProperty("db.password")
        );

    }
}