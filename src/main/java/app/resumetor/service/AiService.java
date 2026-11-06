package app.resumetor.service;

import app.resumetor.ai.OpenRouterLLMService;
import app.resumetor.model.Profile;
import app.resumetor.repository.CareerDao;
import app.resumetor.repository.ProfileDao;
import app.resumetor.security.PiiSanitizer;

import java.util.concurrent.CompletableFuture;

public class AiService {
    
    private final OpenRouterLLMService llmService;
    private final ProfileDao profileDao;
    private final CareerDao careerDao;

    public AiService() {
        this.llmService = new OpenRouterLLMService();
        this.profileDao = new ProfileDao();
        this.careerDao = new CareerDao();
    }

    public CompletableFuture<String> analyzeJob(String rawJobDescription) {
        String systemPrompt = "You are a technical recruiter. Extract structured information from the provided job description. " +
                "Return ONLY a JSON object with the following schema: \n" +
                "{\n" +
                "  \"jobTitle\": \"\",\n" +
                "  \"company\": \"\",\n" +
                "  \"requiredSkills\": [],\n" +
                "  \"preferredSkills\": [],\n" +
                "  \"responsibilities\": [],\n" +
                "  \"experienceRequirements\": [],\n" +
                "  \"educationRequirements\": [],\n" +
                "  \"softSkills\": [],\n" +
                "  \"keywords\": []\n" +
                "}";

        return llmService.sendRequestAsync(systemPrompt, rawJobDescription, true);
    }
    
    public CompletableFuture<String> matchProfileToJob(String jobAnalysisJson) {
        String profileContext = sanitizedProfile();
        
        String systemPrompt = "You are an expert career advisor. Compare the candidate's profile against the job analysis. " +
                "NEVER invent evidence. If a requirement is missing from the profile, say so explicitly.\n" +
                "Each value must be a short, well-written paragraph (2-4 sentences) of plain prose addressed to the candidate " +
                "(use 'you'). No bullet characters, no markdown, no nested objects.\n" +
                "Return ONLY a JSON object with exactly these keys:\n" +
                "{\n" +
                "  \"overallFit\": \"\",\n" +
                "  \"strongMatches\": \"\",\n" +
                "  \"partialMatches\": \"\",\n" +
                "  \"missingRequirements\": \"\",\n" +
                "  \"keywordOpportunities\": \"\",\n" +
                "  \"developmentSuggestions\": \"\"\n" +
                "}";
                
        String userPrompt = "Candidate Profile:\n" + profileContext + "\n\nJob Analysis:\n" + jobAnalysisJson;

        return llmService.sendRequestAsync(systemPrompt, userPrompt, true);
    }

    public CompletableFuture<String> generateResume(String jobAnalysisJson, String matchingJson) {
        String profileContext = sanitizedProfile();

        String systemPrompt = "You are a professional resume writer. Generate ATS-friendly resume content tailored to the job. " +
                "Use ONLY information contained in the candidate profile. NEVER invent facts, employers, projects, skills, metrics or dates. " +
                "Order experiences and projects by relevance to the job. Write 2-5 concise, achievement-oriented bullets per entry " +
                "starting with strong action verbs and naturally including job keywords that the candidate genuinely has. " +
                "Leave 'dates' empty if unknown. Do not include contact details or education (they are added separately).\n" +
                "Return ONLY a JSON object with this schema:\n" +
                "{\n" +
                "  \"professionalSummary\": \"2-3 sentences\",\n" +
                "  \"skills\": [\"...\"],\n" +
                "  \"experiences\": [{ \"company\": \"\", \"role\": \"\", \"dates\": \"\", \"bullets\": [\"...\"] }],\n" +
                "  \"projects\": [{ \"title\": \"\", \"bullets\": [\"...\"] }]\n" +
                "}";

        String userPrompt = "Candidate Profile:\n" + profileContext + "\n\nJob Analysis:\n" + jobAnalysisJson + "\n\nMatching Insights:\n" + matchingJson;

        return llmService.sendRequestAsync(systemPrompt, userPrompt, true);
    }

    public CompletableFuture<String> generateCoverLetter(String jobAnalysisJson, String matchingJson) {
        String profileContext = sanitizedProfile();

        String systemPrompt = "You are a professional career writer. Write a tailored cover letter (250-350 words) for the candidate. " +
                "Use ONLY verified facts from the candidate profile. Do not invent experience. " +
                "Explain why their background fits, using concrete examples from their profile.\n" +
                "Start with 'Dear Hiring Manager,' (or the company's team if known) and end with 'Sincerely,' followed by [NAME] on its own line. " +
                "Output ONLY the letter as plain text paragraphs separated by blank lines. No markdown, no JSON, no headers, no address block." +
                "Do not use include career direction unless something relevant in mentioned in the job description.";

        String userPrompt = "Candidate Profile:\n" + profileContext + "\n\nJob Analysis:\n" + jobAnalysisJson;

        return llmService.sendRequestAsync(systemPrompt, userPrompt, false);
    }

    public CompletableFuture<String> generateEmail(String jobAnalysisJson) {
        String profileContext = sanitizedProfile();

        String systemPrompt = "You are an expert copywriter writing a short job-application email to a recruiter. " +
                "Use the P.A.S. framework, compressed into at most 120 words in the body(it shouldn't contain labels like problem agitate and solve. The lines should be included in a paragraph or two neatly):\n" +
                "1. Problem: one sentence naming the specific challenge/need this role exists to solve (inferred from the job description).\n" +
                "2. Agitate: one sentence on why getting this hire right matters (impact, cost of the gap) - confident, not dramatic.\n" +
                "3. Solve: 2-3 sentences showing how the candidate solves it, citing one or two concrete facts from their profile, " +
                "then mention the attached CV and a clear call to action (a short call/interview).\n" +
                "Use ONLY verified facts from the profile. No clichés like 'I am writing to apply'. No placeholders other than [NAME].\n" +
                "Output plain text in exactly this format:\n" +
                "Subject: <concise subject with role title>\n\n" +
                "Hi <Hiring Manager or team>,\n\n<body>\n\nBest regards,\n[NAME]";

        String userPrompt = "Candidate Profile:\n" + profileContext + "\n\nJob Analysis:\n" + jobAnalysisJson;

        return llmService.sendRequestAsync(systemPrompt, userPrompt, false);
    }

    private String sanitizedProfile() {
        return PiiSanitizer.buildSanitizedProfileContext(
                profileDao.getProfile(),
                careerDao.getAllExperiences(),
                careerDao.getAllProjects(),
                careerDao.getAllEducations(),
                careerDao.getAllSkills()
        );
    }
}
