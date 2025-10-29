package org.userregistrationspringsecurity.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.userregistrationspringsecurity.dto.UserDto;
import org.userregistrationspringsecurity.service.UserService;

import java.util.List;

@Controller
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/home-page")
    public String homePage() {
        return "index";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/home-page";
    }


    @GetMapping("/users")
    public String usersPage(Model model) {
        List<UserDto> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "user";
    }
}