package com.spmss.intelliswine.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.spmss.intelliswine.dto.auth.AuthLoginRequest;
import com.spmss.intelliswine.dto.auth.AuthRegisterRequest;
import com.spmss.intelliswine.dto.auth.AuthResponse;
import com.spmss.intelliswine.entity.Farm;
import com.spmss.intelliswine.entity.Users;
import com.spmss.intelliswine.enums.UserRole;
import com.spmss.intelliswine.enums.UserStatus;
import com.spmss.intelliswine.repository.FarmRepository;
import com.spmss.intelliswine.repository.UserRepository;
import com.spmss.intelliswine.security.UserPrincipal;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final FarmRepository farmRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
        UserRepository userRepository,
        FarmRepository farmRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.farmRepository = farmRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponse register(AuthRegisterRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmailAddress(email)) {

            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");

        }

        Users user = new Users();

        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmailAddress(email);
        user.setUsername(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.FARMER);
        user.setStatus(UserStatus.ACTIVE);

        Users savedUser = userRepository.save(user);

        Farm farm = new Farm();

        farm.setOwner(savedUser);
        farm.setFarmName(resolveFarmName(request));
        farm.setFarmSize(normalizeFarmSize(request.getFarmSize()));

        Farm savedFarm = farmRepository.save(farm);

        String token = jwtService.generateToken(UserPrincipal.fromUser(savedUser));

        return AuthResponse.builder()
            .token(token)
            .userId(savedUser.getId())
            .email(savedUser.getEmailAddress())
            .firstName(savedUser.getFirstName())
            .lastName(savedUser.getLastName())
            .role(savedUser.getRole().name())
            .farmId(savedFarm.getId())
            .farmName(savedFarm.getFarmName())
            .build();
    }

    public AuthResponse login(AuthLoginRequest request) {
        
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail().trim().toLowerCase(),
                request.getPassword()
            )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Users user = userRepository.findByEmailAddress(userDetails.getUsername())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setLastLoginAt(java.time.LocalDateTime.now());

        userRepository.save(user);

        Farm farm = farmRepository.findByOwnerId(user.getId()).stream().findFirst().orElse(null);

        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
            .token(token)
            .userId(user.getId())
            .email(user.getEmailAddress())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .role(user.getRole().name())
            .farmId(farm != null ? farm.getId() : null)
            .farmName(farm != null ? farm.getFarmName() : null)
            .build();
    }

    private String resolveFarmName(AuthRegisterRequest request) {

        String farmName = request.getFarmName();
        if (farmName == null || farmName.isBlank()) {
            return request.getLastName().trim() + " Farm";
        }

        return farmName.trim();
    }

    private String normalizeFarmSize(String farmSize) {

        if (farmSize == null || farmSize.isBlank()) {
            return null;
        }
        
        return farmSize.trim().toLowerCase();
    }
}
