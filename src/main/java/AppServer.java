import config.DatabaseConfig;
import controller.AuthController;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

public class AppServer {


    public static void main(String[] args) {
        System.out.println("Iniciando el programa...");
        AuthController authController = new AuthController();

        Date fecha_registro;
        fecha_registro = Date.valueOf(LocalDate.now());

        Date fecha_nac;
        fecha_nac = Date.valueOf(LocalDate.of(2005,5,26));


        authController.handleRegistrationRequest("felipe", "admin", "a", "pepito", fecha_registro, fecha_nac);

        authController.handleRegistrationRequest("juan", "admin", "b", "pepito", fecha_registro, fecha_nac);
    }
}
