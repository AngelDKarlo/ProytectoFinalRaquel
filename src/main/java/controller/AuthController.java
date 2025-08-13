package controller;

import service.UserService;

import java.sql.Date;

public class AuthController {
    private final UserService userService = new UserService();

    public void handleRegistrationRequest(String email, String password, String nombre_completo, String nombre_usuario, Date fecha_registro, Date fecha_nac) {
        try{
            userService.registerUser(email,password, nombre_completo, nombre_usuario, fecha_registro, fecha_nac);

        }catch (Exception e){
            System.out.println("Error al registrar el usuario");
        }

    }
}
