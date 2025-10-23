package org.userregistrationspringsecurity.service;

import org.userregistrationspringsecurity.dto.UserDto;
import org.userregistrationspringsecurity.entity.User;

import java.util.List;

public interface UserService {
    void saveUser(UserDto userDto);

    User findUserByEmail(String email);

    List<UserDto> findAllUsers();
}