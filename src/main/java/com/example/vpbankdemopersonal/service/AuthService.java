package com.example.vpbankdemopersonal.service;

import com.example.vpbankdemopersonal.dto.LoginRequest;
import com.example.vpbankdemopersonal.dto.RegisterRequest;
import com.example.vpbankdemopersonal.entity.Products;
import com.example.vpbankdemopersonal.entity.RefreshToken;
import com.example.vpbankdemopersonal.entity.UserRole;
import com.example.vpbankdemopersonal.entity.Users;
import com.example.vpbankdemopersonal.kafka.producer.AuthEventProducer;
import com.example.vpbankdemopersonal.repository.RefreshTokenRepository;
import com.example.vpbankdemopersonal.repository.UserRepository;
import com.example.vpbankdemopersonal.repository.UserRoleRepository;
import com.example.vpbankdemopersonal.security.JwtUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepo;
    private final UserRoleRepository userRoleRepo;
    private final RefreshTokenRepository refreshTokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthEventProducer producer;
    private final AuthenticationManager authenticationManager;
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    @PersistenceContext
    private EntityManager entityManager;

    public String login(LoginRequest req) {
        try {
            Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    req.getUsername(),
                    req.getPassword()
            ));
            UserDetails userDetails = (UserDetails) auth.getPrincipal();

            String refreshToken = UUID.randomUUID().toString();
            RefreshToken tokenEntity = new RefreshToken();
            Users user = userRepo.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            tokenEntity.setUserId(user.getId());
            tokenEntity.setRefreshToken(refreshToken);
            tokenEntity.setExpiredTime(LocalDateTime.now().plusDays(2));
            refreshTokenRepo.save(tokenEntity);

            producer.sendLoginEvent(userDetails.getUsername(), "success");

            log.info("Login success");

            return jwtUtils.generateToken(new org.springframework.security.core.userdetails.User(
                    req.getUsername(), "", List.of()));
        }
        catch (BadCredentialsException ex) {
            producer.sendLoginEvent(req.getUsername(), "failed");
            throw new IllegalArgumentException("Wrong username or password");
        }
    }

    public void register(RegisterRequest request) {
        if (userRepo.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        Users user = new Users();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        UserRole userRole = userRoleRepo.findByRoleCode("ROLE_USER")
                .orElseThrow(() -> new IllegalArgumentException("Default role not found"));
        user.setRoles(Collections.singleton(userRole));
        userRepo.save(user);
    }

    public List<Products> getAllProducts() {
        String jpql = "SELECT p FROM Products p";
        return entityManager.createQuery(jpql, Products.class).getResultList();
    }

    public Products getProductByName(String name) {
        List<Products> results = entityManager.createQuery(
                        "SELECT p FROM Products p WHERE p.productName = :name", Products.class)
                .setParameter("name", name)
                .getResultList();

        return results.stream().findFirst().orElse(null);
    }

    public void revoke(String refreshToken) {
        refreshTokenRepo.deleteByRefreshToken(refreshToken);
    }
}

