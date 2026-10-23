
package com.shreyas.worklynk_ai;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    private final PolicyIngestionService ingestionService;

    public PolicyController(PolicyIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    public record UploadResponse(String message, int chunksStored) {}

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponse upload(@RequestParam("file") MultipartFile file) {
        try {
            int chunks = ingestionService.ingest(file);
            return new UploadResponse(
                    "Policy PDF processed successfully", chunks);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Could not read PDF file");
        }
    }
}
