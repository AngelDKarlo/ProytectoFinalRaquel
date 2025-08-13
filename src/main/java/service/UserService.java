package service;

import model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import repository.UserRepository;

import java.sql.Date;
import java.time.LocalDate;


public class UserService {
    private final UserRepository userRepository =  new UserRepository();
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User registerUser(String email, String password, String nombre_usuario, String nombre_completo, Date fecha_nac, Date fecha_registro) throws Exception {

        //Verificamos si el correo ya esta en uso
        if (userRepository.findByEmail(email) != null){
            throw new Exception("El email ya existe");
        }

        // Encriptamos la contraseña recibida}
        String hashedPassword = passwordEncoder.encode(password);

        //Creamos el nuevo usuario y se guarda en la base de datos
        User user = new User();
        user.setEmail(email);
        user.setPassword_hash(hashedPassword);
        user.setNombre_usuario(nombre_usuario);
        user.setNombre_completo(nombre_completo);
        user.setFecha_nacimiento(fecha_nac);
        user.setFecha_registro(fecha_registro);
        userRepository.save(user);
        return user;
    }
}
