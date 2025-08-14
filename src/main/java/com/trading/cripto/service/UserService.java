package com.trading.cripto.service;

import com.trading.cripto.model.Portafolio;
import com.trading.cripto.model.User;
import com.trading.cripto.repository.PortafolioRepository;
import com.trading.cripto.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PortafolioRepository portafolioRepository;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       PortafolioRepository portafolioRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.portafolioRepository = portafolioRepository;
    }

    @Transactional
    public User registerUser(String email, String password, String nombre_usuario,
                             String nombre_completo, Date fecha_nac, Date fecha_registro) throws Exception {

        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new Exception("El email ya existe");
        }

        String hashedPassword = passwordEncoder.encode(password);

        User user = new User();
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setNombreUsuario(nombre_usuario);
        user.setNombreCompleto(nombre_completo);
        user.setFechaNacimiento(fecha_nac);
        user.setFechaRegistro(fecha_registro);

        // Guardar usuario
        User savedUser = userRepository.save(user);

        // Crear portafolio automáticamente con $10,000 USD
        Portafolio portafolio = new Portafolio(savedUser.getId(), new BigDecimal("10000.00"));
        portafolioRepository.save(portafolio);

        return savedUser;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }
}