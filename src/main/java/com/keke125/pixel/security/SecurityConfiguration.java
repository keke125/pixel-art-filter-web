package com.keke125.pixel.security;

import com.keke125.pixel.views.login.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.*;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.HashMap;
import java.util.Map;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration extends VaadinWebSecurity {

    // default password encoder can be set by idForEncode
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt pbkdf2 argon2
        String idForEncode = "argon2";
        // the following value can be changed to meet your need
        // pbkdf2
        String secret = "";
        // byte
        int pbkdf2SaltLength = 16;
        int pbkdf2Iterations = 310000;
        Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm secretKeyFactoryAlgorithm = Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256;
        // argon2
        int argon2SaltLength = 16;
        int hashLength = 32;
        int parallelism = 1;
        int memory = 1 << 14;
        int argon2Iterations = 2;
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("BCrypt", new BCryptPasswordEncoder());
        encoders.put("pbkdf2", new Pbkdf2PasswordEncoder(secret, pbkdf2SaltLength, pbkdf2Iterations, secretKeyFactoryAlgorithm));
        encoders.put("pbkdf2@SpringSecurity_v5_8", Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8());
        encoders.put("argon2", new Argon2PasswordEncoder(argon2SaltLength, hashLength, parallelism, memory, argon2Iterations));
        encoders.put("argon2@SpringSecurity_v5_8", Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8());
        return new DelegatingPasswordEncoder(idForEncode, encoders);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // http.authorizeHttpRequests().requestMatchers(new AntPathRequestMatcher("/images/*.png")).permitAll();

        // Icons from the line-awesome addon
        http.authorizeHttpRequests().requestMatchers(new AntPathRequestMatcher("/line-awesome/**/*.svg")).permitAll();
        super.configure(http);
        setLoginView(http, LoginView.class);
    }

}
