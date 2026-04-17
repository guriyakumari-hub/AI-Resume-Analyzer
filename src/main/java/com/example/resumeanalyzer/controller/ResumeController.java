package com.example.resumeanalyzer.controller;

import com.example.resumeanalyzer.model.ResumeFeedback;
import com.example.resumeanalyzer.service.ResumeService;
import com.example.resumeanalyzer.utils.TextUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @PostMapping("/analyze")
    public String analyzeResume(
            @RequestParam("resumeFile") MultipartFile resumeFile,
            @RequestParam("jobDescription") String jobDescription,
            Model model,
            HttpSession session) {

        if (resumeFile == null || resumeFile.isEmpty()) {
            model.addAttribute("errorMessage", "Please upload your resume file.");
            return "index";
        }

        if (!TextUtils.isSupportedFileType(resumeFile)) {
            model.addAttribute("errorMessage", "Only PDF and DOCX files are supported.");
            return "index";
        }

        try {
            ResumeFeedback feedback = resumeService.analyzeResume(resumeFile, jobDescription);
            session.setAttribute("lastFeedback", feedback);
            session.setAttribute("lastJobDescription", jobDescription);
            model.addAttribute("feedback", feedback);
            model.addAttribute("jobDescription", jobDescription);
            return "result";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Failed to analyze resume. Please try again with a valid file.");
            return "index";
        }
    }

    @GetMapping("/download-report")
    public void downloadReport(HttpSession session, HttpServletResponse response) throws IOException {
        ResumeFeedback feedback = (ResumeFeedback) session.getAttribute("lastFeedback");
        String jobDescription = (String) session.getAttribute("lastJobDescription");

        if (feedback == null) {
            response.sendRedirect("/");
            return;
        }

        byte[] pdfBytes = resumeService.generatePdfReport(feedback, jobDescription);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=resume-analysis-report.pdf");
        response.setContentLength(pdfBytes.length);
        response.getOutputStream().write(pdfBytes);
        response.flushBuffer();
    }
}
