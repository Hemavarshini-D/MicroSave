package com.microsave.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Hidden
@Controller
public class WebController {

    @GetMapping("/login")
    public String login() {
        return "redirect:/dashboard";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam(name = "username", required = false) String username,
                              @RequestParam(name = "password", required = false) String password,
                              Model model) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            model.addAttribute("error", "Please enter both username/email and password.");
            return "redirect:/dashboard";
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/index")
    public String indexPage() {
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/groups")
    public String groups() {
        return "groups";
    }

    @GetMapping("/members")
    public String members() {
        return "members";
    }

    @GetMapping("/contributions")
    public String contributions() {
        return "contributions";
    }

    @GetMapping("/loans")
    public String loans() {
        return "loans";
    }

    @GetMapping("/repayments")
    public String repayments() {
        return "repayments";
    }
}
