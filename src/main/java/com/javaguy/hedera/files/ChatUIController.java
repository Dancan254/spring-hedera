package com.javaguy.hedera.files;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatUIController {

    @GetMapping("/chat-ui")
    public String chatUI() {
        return "chat";
    }
}
