
package com.shreyas.worklynk_ai;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiAssistantController {

    private final AiAssistantService aiAssistantService;

    public AiAssistantController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    public record AskRequest(String question) {}

    public record AskResponse(String answer) {}

    @PostMapping("/ask")
    public AskResponse ask(@RequestBody AskRequest request) {
        String answer = aiAssistantService.ask(request.question());
        return new AskResponse(answer);
    }
}
