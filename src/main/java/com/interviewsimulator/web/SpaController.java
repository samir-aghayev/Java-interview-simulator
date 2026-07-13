package com.interviewsimulator.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    // React Router yolları — brauzer birbaşa açanda index.html verilir,
    // marşrutlaşdırma client tərəfdə aparılır.
    @GetMapping({"/login", "/admin", "/forgot-password", "/reset-password", "/oauth-callback"})
    public String spa() {
        return "forward:/index.html";
    }
}
