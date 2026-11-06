PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS user_profile (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name TEXT,
    email TEXT,
    phone TEXT,
    address TEXT,
    linkedin_url TEXT,
    github_url TEXT,
    portfolio_url TEXT,
    current_title TEXT,
    career_direction TEXT,
    professional_summary TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS experience (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    company TEXT NOT NULL,
    role TEXT NOT NULL,
    employment_type TEXT,
    location TEXT,
    start_date TEXT,
    end_date TEXT,
    is_current BOOLEAN DEFAULT 0,
    description TEXT,
    responsibilities TEXT,
    achievements TEXT,
    technologies TEXT,
    skills TEXT,
    notes TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS project (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT,
    role TEXT,
    technologies TEXT,
    achievements TEXT,
    url TEXT,
    start_date TEXT,
    end_date TEXT,
    notes TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS education (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    institution TEXT NOT NULL,
    degree TEXT NOT NULL,
    field TEXT,
    start_date TEXT,
    end_date TEXT,
    is_current BOOLEAN DEFAULT 0,
    gpa TEXT,
    coursework TEXT,
    achievements TEXT,
    activities TEXT,
    notes TEXT
);

CREATE TABLE IF NOT EXISTS skill (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    category TEXT,
    proficiency TEXT,
    years_experience TEXT,
    notes TEXT
);

CREATE TABLE IF NOT EXISTS certification (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    issuing_organization TEXT,
    issue_date TEXT,
    expiration_date TEXT,
    credential_id TEXT,
    credential_url TEXT,
    notes TEXT
);

CREATE TABLE IF NOT EXISTS achievement (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT,
    date TEXT,
    organization TEXT,
    url TEXT
);

CREATE TABLE IF NOT EXISTS activity (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    organization TEXT NOT NULL,
    role TEXT,
    date TEXT,
    description TEXT,
    responsibilities TEXT,
    achievements TEXT,
    skills TEXT
);

CREATE TABLE IF NOT EXISTS application (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    job_title TEXT,
    company TEXT,
    job_url TEXT,
    location TEXT,
    deadline TEXT,
    raw_job_text TEXT NOT NULL,
    status TEXT DEFAULT 'Draft',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS application_analysis (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    application_id INTEGER NOT NULL,
    required_skills TEXT,
    preferred_skills TEXT,
    responsibilities TEXT,
    experience_requirements TEXT,
    education_requirements TEXT,
    soft_skills TEXT,
    keywords TEXT,
    matching_analysis TEXT,
    feedback TEXT,
    development_suggestions TEXT,
    FOREIGN KEY(application_id) REFERENCES application(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS generated_document (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    application_id INTEGER NOT NULL,
    type TEXT NOT NULL, -- 'RESUME', 'COVER_LETTER', 'EMAIL'
    content TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(application_id) REFERENCES application(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS profile_link (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    label TEXT NOT NULL,
    url TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS settings (
    key TEXT PRIMARY KEY,
    value TEXT
);
