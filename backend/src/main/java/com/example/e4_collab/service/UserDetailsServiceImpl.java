package com.example.e4_collab.service;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.e4_collab.entity.User;
import com.example.e4_collab.repository.UserRepository;
import com.example.e4_collab.security.CustomUserDetail;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    
    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findById(username);
    	
        if (user.isEmpty()) {
			throw new UsernameNotFoundException("User not found with username: " + username);
		}
        
        CustomUserDetail userDetails = new CustomUserDetail();
        userDetails.setUsername(user.get().getUsername());
        userDetails.setPassword(user.get().getPassword());
        userDetails.setRole(user.get().getRole());
        userDetails.setAuthorities(user.get().getAuthorities().stream()
			.map(authority -> new SimpleGrantedAuthority(authority.getUserRole().name()))
			.collect(Collectors.toList()));
        
    	return userDetails;
    }
}
