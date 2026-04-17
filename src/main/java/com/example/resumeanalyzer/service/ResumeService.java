package com.example.resumeanalyzer.service;

import com.example.resumeanalyzer.model.ResumeFeedback;
import com.example.resumeanalyzer.utils.TextUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.ContentHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ResumeService {

    public ResumeFeedback analyzeResume(MultipartFile resumeFile, String jobDescription) {
        String resumeText = extractResumeText(resumeFile);
        List<String> keywords = extractKeywords(jobDescription);
        return buildFeedback(resumeText, keywords);
    }

    public String extractResumeText(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            ContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            AutoDetectParser parser = new AutoDetectParser();
            parser.parse(inputStream, handler, metadata, new ParseContext());
            return handler.toString().trim();
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to extract text from resume file.", ex);
        }
    }

    private List<String> extractKeywords(String text) {
        if (text == null) {
            return new ArrayList<>();
        }

        String cleaned = text
                .toLowerCase(Locale.ENGLISH)
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        String[] tokens = cleaned.split(" ");
        Set<String> keywords = new HashSet<>();

        for (String token : tokens) {
            if (token.isBlank()) {
                continue;
            }
            if (!TextUtils.STOP_WORDS.contains(token)) {
                keywords.add(token);
            }
        }

        return keywords.stream()
                .filter(word -> word.length() > 2)
                .collect(Collectors.toList());
    }

    private ResumeFeedback buildFeedback(String resumeText, List<String> keywords) {
        String normalizedResume = resumeText.toLowerCase(Locale.ENGLISH);
        List<String> matchedKeywords = new ArrayList<>();
        List<String> missingKeywords = new ArrayList<>();

        for (String keyword : keywords) {
            if (normalizedResume.contains(keyword)) {
                matchedKeywords.add(keyword);
            } else {
                missingKeywords.add(keyword);
            }
        }

        double matchPercentage = keywords.isEmpty() ? 0.0
                : (matchedKeywords.size() * 100.0) / keywords.size();

        List<String> detectedSections = detectSections(resumeText);
        List<String> missingSections = detectMissingSections(detectedSections);
        List<String> suggestions = buildSuggestions(matchPercentage, missingKeywords, resumeText, missingSections);
        int score = calculateScore(matchPercentage, resumeText, matchedKeywords);

        return new ResumeFeedback(resumeText, Math.round(matchPercentage * 10.0) / 10.0,
                matchedKeywords.size(), keywords.size(), missingKeywords, suggestions,
                detectedSections, missingSections, score);
    }

    private List<String> buildSuggestions(double matchPercentage,
                                          List<String> missingKeywords,
                                          String resumeText,
                                          List<String> missingSections) {
        List<String> suggestions = new ArrayList<>();

        if (!missingKeywords.isEmpty()) {
            suggestions.add("Add the following keywords from the job description: " + String.join(", ", missingKeywords) + ".");
        }

        if (!missingSections.isEmpty()) {
            suggestions.add("Consider adding or strengthening the following resume sections: " + String.join(", ", missingSections) + ".");
        }

        if (resumeText.length() < 700) {
            suggestions.add("Expand your resume with more project details, accomplishments, and measurable results.");
        }

        if (matchPercentage < 50 && missingKeywords.isEmpty() && resumeText.length() >= 700) {
            suggestions.add("Refine your achievements and use more concise, impactful language to improve clarity.");
        }

        if (suggestions.isEmpty()) {
            suggestions.add("Your resume is on the right track. Keep focusing on clear accomplishments and relevant skills.");
        }

        return suggestions;
    }

    private List<String> detectSections(String resumeText) {
        String normalized = resumeText.toLowerCase(Locale.ENGLISH);
        List<String> detected = new ArrayList<>();

        if (containsAny(normalized, "skills", "technologies", "tools", "frameworks", "proficiencies", "expertise")) {
            detected.add("Skills");
        }
        if (containsAny(normalized, "education", "degree", "bachelor", "master", "certification", "university", "college")) {
            detected.add("Education");
        }
        if (containsAny(normalized, "experience", "worked", "managed", "led", "developed", "implemented", "responsibilities")) {
            detected.add("Experience");
        }
        if (containsAny(normalized, "project", "projects", "portfolio", "achievement", "built", "delivered")) {
            detected.add("Projects");
        }

        return detected;
    }

    private List<String> detectMissingSections(List<String> detectedSections) {
        List<String> allSections = List.of("Skills", "Education", "Experience", "Projects");
        List<String> missing = new ArrayList<>();

        for (String section : allSections) {
            if (!detectedSections.contains(section)) {
                missing.add(section);
            }
        }

        return missing;
    }

    private boolean containsAny(String text, String... terms) {
        for (String term : terms) {
            if (text.contains(term)) {
                return true;
            }
        }
        return false;
    }

    private int calculateScore(double matchPercentage, String resumeText, List<String> matchedKeywords) {
        double lengthScore = Math.min(30, resumeText.length() / 30.0);
        double keywordScore = Math.min(50, matchPercentage * 0.5);
        double skillBonus = Math.min(20, matchedKeywords.stream()
                .filter(TextUtils.TECH_SKILLS::contains)
                .count() * 5.0);

        int score = (int) Math.round(keywordScore + lengthScore + skillBonus);
        return Math.min(100, Math.max(0, score));
    }

    public byte[] generatePdfReport(ResumeFeedback feedback, String jobDescription) {
        String descriptionText = jobDescription == null ? "" : jobDescription.trim();
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float margin = 50;
                float width = page.getMediaBox().getWidth() - margin * 2;
                float yPosition = page.getMediaBox().getHeight() - margin;
                float lineHeight = 16;

                content.beginText();
                content.setFont(PDType1Font.HELVETICA_BOLD, 18);
                content.newLineAtOffset(margin, yPosition);
                content.showText("AI Resume Analyzer Report");
                content.endText();

                yPosition -= 32;
                yPosition = drawText(content, "Match Score: " + feedback.getScore() + " / 100", margin, yPosition, width, PDType1Font.HELVETICA_BOLD, 14, lineHeight);
                yPosition = drawText(content, "Match Percentage: " + feedback.getMatchPercentage() + "%", margin, yPosition, width, PDType1Font.HELVETICA, 12, lineHeight);
                yPosition = drawText(content, "Job description overview:", margin, yPosition, width, PDType1Font.HELVETICA_BOLD, 12, lineHeight);
                yPosition = drawWrappedText(content, descriptionText, margin, yPosition, width, PDType1Font.HELVETICA, 11, lineHeight);
                yPosition -= 8;
                yPosition = drawText(content, "Detected Sections:", margin, yPosition, width, PDType1Font.HELVETICA_BOLD, 12, lineHeight);
                yPosition = drawList(content, feedback.getDetectedSections(), margin, yPosition, width, PDType1Font.HELVETICA, 11, lineHeight);
                yPosition -= 8;
                yPosition = drawText(content, "Missing Sections:", margin, yPosition, width, PDType1Font.HELVETICA_BOLD, 12, lineHeight);
                yPosition = drawList(content, feedback.getMissingSections(), margin, yPosition, width, PDType1Font.HELVETICA, 11, lineHeight);
                yPosition -= 8;
                yPosition = drawText(content, "Missing Keywords:", margin, yPosition, width, PDType1Font.HELVETICA_BOLD, 12, lineHeight);
                yPosition = drawList(content, feedback.getMissingKeywords(), margin, yPosition, width, PDType1Font.HELVETICA, 11, lineHeight);
                yPosition -= 8;
                yPosition = drawText(content, "Suggestions:", margin, yPosition, width, PDType1Font.HELVETICA_BOLD, 12, lineHeight);
                yPosition = drawList(content, feedback.getSuggestions(), margin, yPosition, width, PDType1Font.HELVETICA, 11, lineHeight);
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to create PDF report.", ex);
        }
    }

    private float drawText(PDPageContentStream content,
                           String text,
                           float x,
                           float y,
                           float width,
                           PDType1Font font,
                           int fontSize,
                           float lineHeight) throws IOException {
        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset(x, y);
        content.showText(text);
        content.endText();
        return y - lineHeight;
    }

    private float drawWrappedText(PDPageContentStream content,
                                  String text,
                                  float x,
                                  float y,
                                  float width,
                                  PDType1Font font,
                                  int fontSize,
                                  float lineHeight) throws IOException {
        if (text == null || text.isBlank()) {
            return y;
        }

        String[] words = text.replaceAll("\\s+", " ").trim().split(" ");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            float size = font.getStringWidth(candidate) / 1000 * fontSize;
            if (size > width) {
                y = drawText(content, line.toString(), x, y, width, font, fontSize, lineHeight);
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (line.length() > 0) {
            y = drawText(content, line.toString(), x, y, width, font, fontSize, lineHeight);
        }
        return y;
    }

    private float drawList(PDPageContentStream content,
                           List<String> items,
                           float x,
                           float y,
                           float width,
                           PDType1Font font,
                           int fontSize,
                           float lineHeight) throws IOException {
        if (items == null || items.isEmpty()) {
            return drawText(content, "- None", x, y, width, font, fontSize, lineHeight);
        }
        for (String item : items) {
            y = drawText(content, "- " + item, x, y, width, font, fontSize, lineHeight);
        }
        return y;
    }
}
