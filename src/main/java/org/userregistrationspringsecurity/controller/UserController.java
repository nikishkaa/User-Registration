package org.userregistrationspringsecurity.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    @GetMapping("/home-page")
    public String homePage() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }





    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/users")
    public String usersPage() {
        return "user";
    }
}
