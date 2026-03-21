package com.spmss.intelliswine.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.spmss.intelliswine.repository.UserRepository;
import com.spmss.intelliswine.security.UserPrincipal;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmailAddress(username)
            .map(UserPrincipal::fromUser)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
