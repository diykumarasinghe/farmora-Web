package com.farmora.security;

import com.farmora.model.Role;
import com.farmora.model.User;
import com.farmora.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
    }

    private void seedUsers() {
        seedUser("admin@farmora.com", "Admin@123", Role.ADMIN, "System Admin", "+94770000001", "Colombo, Western Province");
        seedUser("farmer@farmora.com", "Farmer@123", Role.FARMER, "Kamal Perera (Farmer)", "+94770000002", "Nuwara Eliya, Central Province");
        seedUser("buyer@farmora.com", "Buyer@123", Role.BUYER, "Nimal Silva (Buyer)", "+94770000003", "Colombo 03, Western Province");
        seedUser("delivery@farmora.com", "Delivery@123", Role.DELIVERY_PARTNER, "Sunil Bandara (Delivery)", "+94770000004", "Kandy, Central Province");
    }

    private void seedUser(String email, String rawPassword, Role role, String fullName, String phone, String address) {
        userRepository.findByEmail(email).ifPresentOrElse(user -> {
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRole(role);
            user.setActive(true);
            userRepository.save(user);
            log.info("Default user credentials verified: {}", email);
        }, () -> {
            User newUser = User.builder()
                    .fullName(fullName)
                    .email(email)
                    .password(passwordEncoder.encode(rawPassword))
                    .role(role)
                    .phone(phone)
                    .address(address)
                    .active(true)
                    .build();
            userRepository.save(newUser);
            log.info("Default user created: {}", email);
        });
    }
}
