package org.userregistrationspringsecurity.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.userregistrationspringsecurity.dto.UserDto;
import org.userregistrationspringsecurity.entity.Role;
import org.userregistrationspringsecurity.entity.User;
import org.userregistrationspringsecurity.repository.RoleRepository;
import org.userregistrationspringsecurity.repository.UserRepository;
import org.userregistrationspringsecurity.service.UserService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final EmailService emailService;
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    public void saveUser(UserDto userDto) {
        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        //encrypt the password using spring security
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        user.setEnabled(false);
        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);


        Role role = roleRepository.findByName("default-user");
        if (role == null) {
            role = checkRoleExist();
        }
        user.setRoles(List.of(role));
        userRepository.save(user);

        String confirmationUrl = "http://localhost:8081/verify-email?token=" + token;
        emailService.sendEmail(user.getEmail(), "Email Verification", "Click the link to verify your email: " + confirmationUrl);
    }

    private Role checkRoleExist() {
        Role role = new Role();
        role.setName("default-user");
        return roleRepository.save(role);
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<UserDto> findAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map((user) -> convertEntityToDto(user))
                .collect(Collectors.toList());
    }

    private UserDto convertEntityToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setUsername(user.getUsername());
        userDto.setEmail(user.getEmail());
        userDto.setEmail(user.getEmail());
        return userDto;
    }

    public String validateVerificationToken(String token) {
        User user = userRepository.findByVerificationToken(token).orElse(null);
        if (user == null) {
            return "invalid";
        }

        user.setEnabled(true);
        userRepository.save(user);
        return "valid";
    }
}