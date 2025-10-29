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

@Controller
public class RegisterController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public RegisterController(UserService userService,
                              UserRepository userRepository,
                              ApplicationEventPublisher eventPublisher) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
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

            // Публикуем событие подтверждения email (конструктор с одним параметром)
            try {
                User registered = userService.findUserByEmail(userDto.getEmail());
                if (registered != null) {
                    eventPublisher.publishEvent(new OnRegistrationCompleteEvent(registered));
                }
            } catch (Exception ignored) { }

            // Редирект на страницу верификации
            redirectAttributes.addFlashAttribute("registrationSuccess", true);
            return "redirect:/verify-email";
        } catch (Exception e) {
            // Если пользователь создан, но отправка письма упала — все равно редиректим на верификацию
            try {
                User maybeCreated = userService.findUserByEmail(userDto.getEmail());
                if (maybeCreated != null) {
                    redirectAttributes.addFlashAttribute("registrationSuccess", true);
                    return "redirect:/verify-email";
                }
            } catch (Exception ignored) { }

            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "register";
        }
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam(value = "token", required = false) String token,
                              @RequestParam(value = "message", required = false) String message,
                              Model model) {
        if (token == null || token.isBlank()) {
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


