
// package com.shreyas.worklynk_ai;

// import java.io.IOException;
// import java.io.InputStream;
// import java.util.List;

// import org.apache.pdfbox.Loader;
// import org.apache.pdfbox.pdmodel.PDDocument;
// import org.apache.pdfbox.text.PDFTextStripper;
// import org.springframework.ai.document.Document;
// import org.springframework.ai.vectorstore.VectorStore;
// import org.springframework.stereotype.Service;
// import org.springframework.web.multipart.MultipartFile;

// @Service
// public class PolicyIngestionService {

//     private final VectorStore vectorStore;

//     public PolicyIngestionService(VectorStore vectorStore) {
//         this.vectorStore = vectorStore;
//     }

//     public int ingest(MultipartFile file) throws IOException {
//         if (file.isEmpty()) {
//             throw new IllegalArgumentException("Uploaded PDF is empty");
//         }

//         String filename = file.getOriginalFilename();
//         if (filename == null ||
//                 !filename.toLowerCase().endsWith(".pdf")) {
//             throw new IllegalArgumentException("Please upload a PDF file");
//         }

//         String text;
//         try (InputStream input = file.getInputStream();
//              PDDocument pdf = Loader.loadPDF(input.readAllBytes())) {
//             text = new PDFTextStripper().getText(pdf);
//         }

//         if (text.isBlank()) {
//             throw new IllegalArgumentException(
//                     "No text found in PDF. Scanned PDFs need OCR.");
//         }

//         int chunkSize = 1000;
//         int overlap = 150;
//         List<Document> chunks = new java.util.ArrayList<>();

//         for (int start = 0; start < text.length();) {
//             int end = Math.min(start + chunkSize, text.length());

//             String chunk = text.substring(start, end).trim();
//             if (!chunk.isBlank()) {
//                 chunks.add(new Document(
//                         chunk,
//                         java.util.Map.of("source", filename)
//                 ));
//             }

//             if (end == text.length()) {
//                 break;
//             }
//             start = end - overlap;
//         }

//         vectorStore.add(chunks);
//         return chunks.size();
//     }
// }
package com.shreyas.worklynk_ai;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PolicyIngestionService {

    // Sections longer than this are split further by sentences.
    private static final int MAX_CHUNK_CHARS = 900;

    private final VectorStore vectorStore;

    public PolicyIngestionService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public int ingest(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded PDF is empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null ||
                !filename.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("Please upload a PDF file");
        }

        String text;
        try (InputStream input = file.getInputStream();
                PDDocument pdf = Loader.loadPDF(input.readAllBytes())) {
            text = new PDFTextStripper().getText(pdf);
        }

        if (text.isBlank()) {
            throw new IllegalArgumentException(
                    "No text found in PDF. Scanned PDFs need OCR.");
        }

        List<Document> chunks = new ArrayList<>();
        for (String chunk : splitIntoChunks(text)) {
            chunks.add(new Document(chunk, Map.of("source", filename)));
        }

        // Remove old chunks of the same file so re-uploads don't duplicate.
        vectorStore.delete("source == '" + filename.replace("'", "''") + "'");

        vectorStore.add(chunks);
        return chunks.size();
    }

    /**
     * Splits on numbered headings like "1. Casual Leave" so each section
     * (heading + body) becomes its own chunk. Falls back to sentence-based
     * splitting for long sections or documents without numbered headings.
     */
    private List<String> splitIntoChunks(String text) {
        List<String> result = new ArrayList<>();

        String[] sections = text.split("(?m)^(?=\\d+\\.\\s+\\p{Lu})");

        for (String section : sections) {
            // Join PDF line breaks into normal spaced text.
            // String clean = section.replaceAll("\\s*\\R\\s*", " ")
            // .replaceAll("\\s{2,}", " ")
            // .trim();
            String clean = section
                    .replaceAll("[ \\t]+", " ") // tidy spaces, keep line breaks
                    .replaceAll("(\\s*\\R)+", "\n") // collapse blank lines to one newline
                    .trim();
            if (clean.isBlank()) {
                continue;
            }

            if (clean.length() <= MAX_CHUNK_CHARS) {
                result.add(clean);
            } else {
                result.addAll(splitBySentences(clean));
            }
        }

        return result;
    }

    private List<String> splitBySentences(String text) {
        List<String> chunks = new ArrayList<>();
        String[] sentences = text.split("(?<=[.!?])\\s+");

        StringBuilder current = new StringBuilder();
        String lastSentence = "";

        for (String sentence : sentences) {
            if (current.length() + sentence.length() > MAX_CHUNK_CHARS
                    && current.length() > 0) {
                chunks.add(current.toString().trim());
                current = new StringBuilder(lastSentence).append(' ');
            }
            current.append(sentence).append(' ');
            lastSentence = sentence;
        }

        if (!current.toString().isBlank()) {
            chunks.add(current.toString().trim());
        }

        return chunks;
    }
}