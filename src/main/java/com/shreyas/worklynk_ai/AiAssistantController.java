package com.shreyas.worklynk_ai;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.shreyas.worklynk_ai.ticket.Ticket;

@RestController
@RequestMapping("/api/ai")
public class AiAssistantController {

    private static final int MAX_SUBJECT_LENGTH = 120;

    private final AiAssistantService aiAssistantService;

    public AiAssistantController(AiAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    public record AskRequest(String question) {}

    /** Prefilled values the frontend can send to POST /api/tickets. */
    public record SuggestedTicket(
            String subject,
            String description,
            Ticket.Category category) {}

    public record AskResponse(
            String answer,
            boolean answered,
            SuggestedTicket suggestedTicket) {}

    @PostMapping("/ask")
    public AskResponse ask(@RequestBody AskRequest request) {
        if (request == null || request.question() == null
                || request.question().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Question must not be blank");
        }

        String question = request.question().trim();
        AiAssistantService.AskResult result = aiAssistantService.ask(question);

        if (result.answered()) {
            return new AskResponse(result.answer(), true, null);
        }

        return new AskResponse(
                result.answer(), false, buildSuggestion(question));
    }

    private SuggestedTicket buildSuggestion(String question) {
        String subject = question.length() > MAX_SUBJECT_LENGTH
                ? question.substring(0, MAX_SUBJECT_LENGTH - 3) + "..."
                : question;

        String description = "Employee asked the HR assistant: " + question
                + "\n\nThe assistant could not find this information in "
                + "the uploaded policies.";

        return new SuggestedTicket(
                subject,
                description,
                aiAssistantService.suggestCategory(question));
    }
}