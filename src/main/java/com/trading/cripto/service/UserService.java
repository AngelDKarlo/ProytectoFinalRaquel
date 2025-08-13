package com.trading.cripto.service;

import com.trading.cripto.model.User;
import com.trading.cripto.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.Optional;

@Service // 1. Marca esta clase como un "Servicio" de Spring
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 2. Inyección de dependencias por constructor (la mejor práctica)
    // Spring buscará un Bean de UserRepository y PasswordEncoder y los pasará aquí.
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(String email, String password, String nombre_usuario, String nombre_completo, Date fecha_nac, Date fecha_registro) throws Exception {

        // 3. Usamos el nuevo método del repositorio que devuelve un Optional
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new Exception("El email ya existe");
        }

        // El resto de la lógica es muy similar
        String hashedPassword = passwordEncoder.encode(password);

        User user = new User();
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setNombreUsuario(nombre_usuario);
        user.setNombreCompleto(nombre_completo);
        user.setFechaNacimiento(fecha_nac);
        user.setFechaRegistro(fecha_registro);

        // 4. Usamos el método save() que nos da JpaRepository
        return userRepository.save(user);
    }
}