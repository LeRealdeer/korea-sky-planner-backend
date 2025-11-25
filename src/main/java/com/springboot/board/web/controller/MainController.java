package com.springboot.board.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/")
public class MainController {
    
    @GetMapping
    public RedirectView root() {
        return new RedirectView("/api/v1/questions");
    }

    

} 