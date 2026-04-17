package com.example.resumeanalyzer.model;

import java.util.List;
import java.util.Objects;

public class ResumeFeedback {
    private String extractedText;
    private double matchPercentage;
    private int matchedKeywordCount;
    private int totalKeywordCount;
    private List<String> missingKeywords;
    private List<String> suggestions;
    private List<String> detectedSections;
    private List<String> missingSections;
    private int score;

    public ResumeFeedback() {
    }

    public ResumeFeedback(String extractedText,
                          double matchPercentage,
                          int matchedKeywordCount,
                          int totalKeywordCount,
                          List<String> missingKeywords,
                          List<String> suggestions,
                          List<String> detectedSections,
                          List<String> missingSections,
                          int score) {
        this.extractedText = extractedText;
        this.matchPercentage = matchPercentage;
        this.matchedKeywordCount = matchedKeywordCount;
        this.totalKeywordCount = totalKeywordCount;
        this.missingKeywords = missingKeywords;
        this.suggestions = suggestions;
        this.detectedSections = detectedSections;
        this.missingSections = missingSections;
        this.score = score;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public double getMatchPercentage() {
        return matchPercentage;
    }

    public void setMatchPercentage(double matchPercentage) {
        this.matchPercentage = matchPercentage;
    }

    public List<String> getMissingKeywords() {
        return missingKeywords;
    }

    public void setMissingKeywords(List<String> missingKeywords) {
        this.missingKeywords = missingKeywords;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public int getMatchedKeywordCount() {
        return matchedKeywordCount;
    }

    public void setMatchedKeywordCount(int matchedKeywordCount) {
        this.matchedKeywordCount = matchedKeywordCount;
    }

    public int getTotalKeywordCount() {
        return totalKeywordCount;
    }

    public void setTotalKeywordCount(int totalKeywordCount) {
        this.totalKeywordCount = totalKeywordCount;
    }

    public List<String> getDetectedSections() {
        return detectedSections;
    }

    public void setDetectedSections(List<String> detectedSections) {
        this.detectedSections = detectedSections;
    }

    public List<String> getMissingSections() {
        return missingSections;
    }

    public void setMissingSections(List<String> missingSections) {
        this.missingSections = missingSections;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResumeFeedback)) return false;
        ResumeFeedback that = (ResumeFeedback) o;
        return Double.compare(that.matchPercentage, matchPercentage) == 0 &&
                score == that.score &&
                Objects.equals(extractedText, that.extractedText) &&
                Objects.equals(missingKeywords, that.missingKeywords) &&
                Objects.equals(suggestions, that.suggestions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(extractedText, matchPercentage, matchedKeywordCount, totalKeywordCount, missingKeywords, suggestions, detectedSections, missingSections, score);
    }

    @Override
    public String toString() {
        return "ResumeFeedback{" +
                "extractedText='" + extractedText + '\'' +
                ", matchPercentage=" + matchPercentage +
                ", missingKeywords=" + missingKeywords +
                ", suggestions=" + suggestions +
                ", score=" + score +
                '}';
    }
}
