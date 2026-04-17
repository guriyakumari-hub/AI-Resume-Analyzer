<<<<<<< HEAD
# AI Resume Analyzer

A beginner-friendly full-stack resume analyzer built with:

- Frontend: HTML, CSS, JavaScript
- Backend: Node.js, Express
- File upload: Multer
- PDF parsing: pdf-parse
- AI API: OpenAI GPT-4o-mini
- Deployment-ready: Vercel (frontend) and Render (backend)

## Project Structure

```
ai-resume-analyzer/
  backend/
    server.js
    package.json
  frontend/
    index.html
    style.css
    script.js
```

## Setup

1. Open a terminal in the project root.
2. Install backend dependencies:

   ```bash
   cd backend
   npm install
   ```

3. Set your OpenAI API key:

   - Create a `.env` file in `backend/` with:
     ```env
     OPENAI_API_KEY=YOUR_API_KEY
     ```
   - Or replace `YOUR_API_KEY` in `backend/server.js` with a real key for local testing.

## Run Locally

1. Start the backend:

   ```bash
   cd backend
   npm start
   ```

2. Open `frontend/index.html` in your browser, or use a static server to serve the `frontend/` folder.

## Deployment Notes

- Frontend: Deploy the `frontend/` folder on Vercel as a static site.
- Backend: Deploy `backend/` on Render using `npm start` and set `OPENAI_API_KEY` in Render environment variables.

## Usage

1. Upload a PDF resume.
2. Optionally paste a job description.
3. Click **Analyze Resume**.
4. Review skills, score, suggestions, and job fit results.

## Notes

- The backend expects a PDF file upload.
- The frontend uses a loading message while waiting for the backend.
- Errors are shown in the UI if file upload or analysis fails.
=======
# AI-Resume-Analyzer
🚀 AI Resume Analyzer built with Spring Boot that evaluates resumes against job descriptions. Extracts text using Apache Tika, calculates match percentage 📊, identifies missing keywords ⚠️, and provides smart suggestions 💡 to improve ATS compatibility and boost job selection chances.
>>>>>>> 75fb03d4dcd43270d8c643f7dc571189c237aa30
