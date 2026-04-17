package com.example.resumeanalyzer.utils;

import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public final class TextUtils {

    private TextUtils() {
        // Utility class should not be instantiated.
    }

    public static final Set<String> STOP_WORDS = Set.of(
            "and", "or", "but", "the", "a", "an", "to", "for", "with",
            "of", "in", "on", "by", "at", "from", "is", "are", "was",
            "were", "be", "been", "have", "has", "had", "will", "would",
            "can", "may", "should", "as", "that", "this", "these", "those"
    );

    public static final Set<String> TECH_SKILLS = Set.of(
            "java", "spring", "spring boot", "hibernate", "docker", "kubernetes",
            "microservices", "sql", "rest", "api", "aws", "azure", "git"
    );

    public static boolean isSupportedFileType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return false;
        }
        String lowered = filename.toLowerCase();
        return lowered.endsWith(".pdf") || lowered.endsWith(".docx");
    }
}
