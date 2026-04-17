const express = require("express");
const multer = require("multer");
const cors = require("cors");
const pdf = require("pdf-parse");
const { OpenAI } = require("openai");

const app = express();
app.use(cors());
app.use(express.json());

const upload = multer({ storage: multer.memoryStorage(), limits: { fileSize: 15 * 1024 * 1024 } });
const openai = new OpenAI({ apiKey: process.env.OPENAI_API_KEY || "YOUR_API_KEY" });

function buildPrompt(resumeText, jobDescription) {
  let prompt = `You are a friendly resume analysis assistant. Analyze the resume text below and return a valid JSON object with these keys:\n`;
  prompt += `- skills: array of core skills found in the resume\n`;
  prompt += `- score: resume score from 0 to 100\n`;
  prompt += `- suggestions: an array of 3 to 5 actionable improvement suggestions\n`;

  if (jobDescription) {
    prompt += `- match: a short summary of how well the resume matches the job description\n`;
    prompt += `- job_fit_score: a score from 0 to 100 for job fit\n`;
  }

  prompt += `Respond with JSON only. Do not include any additional explanation or markup. Use this exact output structure.`;
  prompt += `\n\nResume text:\n"""${resumeText.slice(0, 20000)}"""\n`;

  if (jobDescription) {
    prompt += `\nJob description:\n"""${jobDescription.slice(0, 2000)}"""\n`;
  }

  prompt += `\n\nOutput example:\n{\n  "skills": ["JavaScript", "React", "Node.js"],\n  "score": 85,\n  "suggestions": ["..."],\n`;
  if (jobDescription) {
    prompt += `  "match": "...",\n  "job_fit_score": 80\n`;
  }
  prompt += `}`;

  return prompt;
}

app.post("/analyze", upload.single("resume"), async (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json({ error: "Please upload a PDF resume file." });
    }

    if (!req.file.mimetype.includes("pdf")) {
      return res.status(400).json({ error: "Only PDF files are supported." });
    }

    const data = await pdf(req.file.buffer);
    const resumeText = data.text?.trim();

    if (!resumeText) {
      return res.status(400).json({ error: "Could not extract text from the uploaded PDF. Please try another file." });
    }

    const jobDescription = typeof req.body.jobDescription === "string" ? req.body.jobDescription.trim() : "";
    const prompt = buildPrompt(resumeText, jobDescription);

    const aiResponse = await openai.responses.create({
      model: "gpt-4o-mini",
      input: prompt,
      max_output_tokens: 700
    });

    const rawOutput = aiResponse.output?.[0]?.content?.[0]?.text || aiResponse.output_text || "";
    let result = {};

    try {
      result = JSON.parse(rawOutput);
    } catch (parseError) {
      const jsonMatch = rawOutput.match(/\{[\s\S]*\}$/);
      if (jsonMatch) {
        result = JSON.parse(jsonMatch[0]);
      } else {
        throw new Error("Unable to parse AI response. Please try again.");
      }
    }

    return res.json({ result });
  } catch (error) {
    console.error("Analyze error:", error);
    const message = error.response?.statusText || error.message || "Server error";
    return res.status(500).json({ error: message });
  }
});

const port = process.env.PORT || 5000;
app.listen(port, () => {
  console.log(`AI Resume Analyzer backend listening on port ${port}`);
});
