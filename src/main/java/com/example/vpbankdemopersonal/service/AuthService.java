package com.example.vpbankdemopersonal.service;

import com.example.vpbankdemopersonal.dto.request.auth.LoginRequest;
import com.example.vpbankdemopersonal.dto.request.product.ProductRequest;
import com.example.vpbankdemopersonal.dto.request.auth.RegisterRequest;
import com.example.vpbankdemopersonal.entity.Products;
import com.example.vpbankdemopersonal.entity.RefreshToken;
import com.example.vpbankdemopersonal.entity.UserRole;
import com.example.vpbankdemopersonal.entity.Users;
import com.example.vpbankdemopersonal.kafka.dto.KafkaLoginMessage;
import com.example.vpbankdemopersonal.kafka.producer.AuthEventProducer;
import com.example.vpbankdemopersonal.kafka.topic.KafkaTopic;
import com.example.vpbankdemopersonal.repository.RefreshTokenRepository;
import com.example.vpbankdemopersonal.repository.UserRepository;
import com.example.vpbankdemopersonal.repository.UserRoleRepository;
import com.example.vpbankdemopersonal.security.JwtUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepo;
    private final UserRoleRepository userRoleRepo;
    private final RefreshTokenRepository refreshTokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthEventProducer producer;
    private final AuthenticationManager authenticationManager;
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

            produceEvent(KafkaTopic.LOG_IN_USER,userDetails.getUsername(), "Success");

            log.info("Login success");

            return jwtUtils.generateToken(new org.springframework.security.core.userdetails.User(
                    req.getUsername(), "", List.of()));
        }
        catch (BadCredentialsException ex) {
            produceEvent(KafkaTopic.LOG_IN_USER,req.getUsername(), "Failed");
            throw new IllegalArgumentException("Wrong username or password");
        }
    }

    public void register(RegisterRequest request) {
        try {
            if (userRepo.findByUsername(request.getUsername()).isPresent()) {
                produceEvent(KafkaTopic.REGISTER_USER,request.getUsername(), "Failed");
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
            produceEvent(KafkaTopic.REGISTER_USER,request.getUsername(), "Success");
        }
        catch (Exception e) {
            produceEvent(KafkaTopic.REGISTER_USER,request.getUsername(), "Failed");
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public List<Products> getAllProducts(ProductRequest request) {
        Users user = getUserByToken(request.getRefreshToken());
        try {
            String jpql = "SELECT p FROM Products p";
            List<Products> productsList = entityManager.createQuery(jpql, Products.class).getResultList();
            produceEvent(KafkaTopic.SEARCH_PRODUCT_ALL,(user != null ? user.getUsername() : null), "Success");
            return productsList;
        }
        catch (Exception e) {
            produceEvent(KafkaTopic.SEARCH_PRODUCT_ALL,(user != null ? user.getUsername() : null), "Failed");
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public Products getProductByName(ProductRequest request) {
        Users user = getUserByToken(request.getRefreshToken());
        try {
            String productName = request.getProductName();
            List<Products> results = entityManager.createQuery(
                            "SELECT p FROM Products p WHERE (p.productName = :name and :name is not null)", Products.class)
                    .setParameter("name", productName)
                    .getResultList();
            produceEvent(KafkaTopic.SEARCH_PRODUCT_BY_NAME,(user != null ? user.getUsername() : null), "Success");
            return results.stream().findFirst().orElse(null);
        }
        catch (Exception e) {
            produceEvent(KafkaTopic.SEARCH_PRODUCT_BY_NAME,(user != null ? user.getUsername() : null), "Failed");
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public void revoke(String refreshToken) {
        refreshTokenRepo.deleteByRefreshToken(refreshToken);
    }

    private void produceEvent(String topic,String userName ,String status) {
        producer.produceEvent(
                topic,
                KafkaLoginMessage.builder()
                        .username(userName)
                        .status(status)
                        .build());
    }

    private Users getUserByToken(String token) {
        try {
            if (token != null) {
                RefreshToken refreshToken = refreshTokenRepo.findByRefreshToken(token).orElse(null);
                return (refreshToken != null) ? userRepo.getUsersById(refreshToken.getUserId()).orElse(null) : null;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}

