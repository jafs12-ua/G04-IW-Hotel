package com.villadictos.app.config;

import com.villadictos.app.model.Usuario;
import com.villadictos.app.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataVerificationRunner implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataVerificationRunner(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=============================================");
        System.out.println("DATA VERIFICATION RUNNER");
        System.out.println("=============================================");

        String email = "admin@hotel.com";
        Optional<Usuario> admin = usuarioRepository.findByEmail(email);

        if (admin.isPresent()) {
            System.out.println("User found: " + email);
            System.out.println("Role: " + admin.get().getRol());
            System.out.println("Password Hash: " + admin.get().getPasswordHash());

            boolean matches = passwordEncoder.matches("password", admin.get().getPasswordHash());
            System.out.println("Password 'password' matches: " + matches);

            if (!matches) {
                System.out.println("Trying '1234'...");
                boolean matches1234 = passwordEncoder.matches("1234", admin.get().getPasswordHash());
                System.out.println("Password '1234' matches: " + matches1234);

                System.out.println("GENERATING NEW HASH FOR 'password':");
                String newHash = passwordEncoder.encode("password");
                System.out.println("NEW HASH: " + newHash);
            }
        } else {
            System.out.println("User NOT found: " + email);
        }
        System.out.println("=============================================");
    }
}
