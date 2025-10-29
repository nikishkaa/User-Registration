package org.userregistrationspringsecurity.controller;

import jakarta.validation.Valid;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.userregistrationspringsecurity.dto.UserDto;
import org.userregistrationspringsecurity.entity.User;
import org.userregistrationspringsecurity.event.OnRegistrationCompleteEvent;
import org.userregistrationspringsecurity.repository.UserRepository;
import org.userregistrationspringsecurity.service.UserService;

import java.util.List;

@Controller
public class UserController {

    private UserService userService;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public UserController(UserService userService,
                          UserRepository userRepository,
                          ApplicationEventPublisher eventPublisher) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }


    @GetMapping("/home-page")
    public String homePage() {
        return "index";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/home-page";
    }


    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String loginProcess(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               RedirectAttributes redirectAttributes) {
        return "redirect:/login";
    }


    @GetMapping("/register")
    public String registerPage(Model model, UserDto userDto) {
        model.addAttribute("user", userDto);
        return "register";
    }

    @PostMapping("/register/save")
    public String registerSave(@Valid @ModelAttribute("user") UserDto userDto,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            // Проверка занятости email до сохранения
            User existing = userService.findUserByEmail(userDto.getEmail());
            if (existing != null) {
                bindingResult.rejectValue("email", "error.user", "Email уже занят");
                return "register";
            }

            userService.saveUser(userDto);

            // Находим созданного пользователя и публикуем событие подтверждения email
            try {
                User registered = userService.findUserByEmail(userDto.getEmail());
                if (registered != null) {
                    // Используем доступный конструктор события с одним параметром
                    eventPublisher.publishEvent(new OnRegistrationCompleteEvent(registered));
                }
            } catch (Exception eventException) {
                // Игнорируем ошибки при публикации события, редирект все равно должен произойти
            }

            // Направляем на страницу с инструкцией подтвердить email
            redirectAttributes.addFlashAttribute("registrationSuccess", true);
            return "redirect:/verify-email";
        } catch (Exception e) {
            // Если во время сохранения произошла ошибка (например, при отправке письма),
            // но пользователь уже существует в БД, всё равно перенаправим на страницу верификации
            try {
                User maybeCreated = userService.findUserByEmail(userDto.getEmail());
                if (maybeCreated != null) {
                    redirectAttributes.addFlashAttribute("registrationSuccess", true);
                    return "redirect:/verify-email";
                }
            } catch (Exception ignored) {
            }

            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "register";
        }
    }

    @GetMapping("/users")
    public String usersPage(Model model) {
        List<UserDto> users = userService.findAllUsers();
        model.addAttribute("users", users);
        return "user";
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam(value = "token", required = false) String token,
                              @RequestParam(value = "message", required = false) String message,
                              Model model) {
        if (token == null || token.isBlank()) {
            // Проверяем флеш-атрибут для успешной регистрации
            if (model.containsAttribute("registrationSuccess")) {
                model.addAttribute("message", "Мы отправили ссылку подтверждения на ваш email. Перейдите по ссылке из письма.");
            } else if (message != null && !message.isBlank()) {
                model.addAttribute("message", message);
            } else {
                model.addAttribute("message", "Мы отправили ссылку для подтверждения на ваш email. Перейдите по ссылке из письма.");
            }
            return "verify-email";
        }

        User user = userRepository.findByVerificationToken(token).orElse(null);
        if (user == null) {
            model.addAttribute("message", "Неверный токен подтверждения.");
            return "verify-email";
        }

        user.setEnabled(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        model.addAttribute("message", "Ваш аккаунт успешно подтвержден.");
        return "verified";
    }
}