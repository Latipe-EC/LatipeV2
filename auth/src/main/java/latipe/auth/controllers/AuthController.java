package latipe.auth.controllers;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController("/auth")
public class AuthController {
    @PostMapping("/login")
    public String login() {
        return "auth";
    }

}
