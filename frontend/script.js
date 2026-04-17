const API_URL = "http://localhost:5000";
const resumeInput = document.getElementById("resume");
const jobDescriptionInput = document.getElementById("job-description");
const analyzeButton = document.getElementById("analyze-button");
const statusText = document.getElementById("status");
const resultPanel = document.getElementById("result");
const scoreValue = document.getElementById("score-value");
const scoreBar = document.getElementById("score-bar");
const skillsList = document.getElementById("skills-list");
const suggestionsList = document.getElementById("suggestions-list");
const jobFitSection = document.getElementById("job-fit-section");
const jobMatchText = document.getElementById("job-match-text");
const jobFitScore = document.getElementById("job-fit-score");
const themeToggle = document.getElementById("theme-toggle");

function setStatus(message, isError = false) {
  statusText.textContent = message;
  statusText.style.color = isError ? "#ef4444" : "#0f172a";
}

function resetResults() {
  resultPanel.classList.add("hidden");
  scoreValue.textContent = "0";
  scoreBar.style.width = "0%";
  skillsList.innerHTML = "";
  suggestionsList.innerHTML = "";
  jobFitSection.classList.add("hidden");
}

function renderChips(items) {
  return items.map((item) => {
    const chip = document.createElement("span");
    chip.className = "chip";
    chip.textContent = item;
    return chip;
  });
}

function renderSuggestions(items) {
  return items.map((item) => {
    const li = document.createElement("li");
    li.textContent = item;
    return li;
  });
}

async function analyzeResume() {
  resetResults();

  const file = resumeInput.files[0];
  if (!file) {
    setStatus("Please select a PDF resume to analyze.", true);
    return;
  }

  setStatus("Analyzing resume... This may take a few seconds.");
  analyzeButton.disabled = true;
  analyzeButton.textContent = "Analyzing...";

  try {
    const formData = new FormData();
    formData.append("resume", file);
    formData.append("jobDescription", jobDescriptionInput.value.trim());

    const response = await fetch(`${API_URL}/analyze`, {
      method: "POST",
      body: formData
    });

    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.error || "Something went wrong while analyzing the resume.");
    }

    const result = data.result || {};
    const skills = Array.isArray(result.skills) ? result.skills : [];
    const suggestions = Array.isArray(result.suggestions) ? result.suggestions : [];
    const score = Number(result.score) || 0;

    scoreValue.textContent = `${score}`;
    scoreBar.style.width = `${Math.max(0, Math.min(score, 100))}%`;

    skillsList.append(...renderChips(skills.length ? skills : ["No skills detected"]));
    suggestionsList.append(...renderSuggestions(suggestions.length ? suggestions : ["No suggestions available."]));

    if (result.match || result.job_fit_score) {
      jobFitSection.classList.remove("hidden");
      jobMatchText.textContent = result.match || "No job-match summary available.";
      jobFitScore.textContent = result.job_fit_score !== undefined ? result.job_fit_score : "0";
    }

    resultPanel.classList.remove("hidden");
    setStatus("Analysis complete! Review the results below.");
  } catch (error) {
    setStatus(error.message, true);
  } finally {
    analyzeButton.disabled = false;
    analyzeButton.textContent = "Analyze Resume";
  }
}

function toggleTheme() {
  document.body.classList.toggle("dark-mode");
  themeToggle.textContent = document.body.classList.contains("dark-mode") ? "Light Mode" : "Dark Mode";
}

analyzeButton.addEventListener("click", analyzeResume);
themeToggle.addEventListener("click", toggleTheme);
