package com.example.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.example.dto.UserRegistrationDto;
import com.example.model.User;

public interface UserService extends UserDetailsService {

	User findByEmail(String email);

	User create(UserRegistrationDto registration);

	User save(User user);
}
