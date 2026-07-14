export interface QuestionOption {
  index: number;
  text: string;
}

export interface Question {
  id: string;
  topic: string;
  text: string;
  options: QuestionOption[];
}

export interface QuizStartResponse {
  questions: Question[];
}

export interface QuestionResult extends Question {
  selectedIndex: number;
  correctIndex: number;
  correct: boolean;
}

export interface GradeResponse {
  totalQuestions: number;
  correctAnswers: number;
  score: number;
  weakTopics: string[];
  details: QuestionResult[];
}

export interface TopicStat {
  topic: string;
  correct: number;
  total: number;
  percent: number;
}

export interface WeakTopicsResponse {
  topics: TopicStat[];
  weakTopics: string[];
}

export interface SessionSummary {
  dateTime: string;
  score: number;
  correctAnswers: number;
  totalQuestions: number;
  percent: number;
}

export interface ProgressResponse {
  sessions: SessionSummary[];
  averagePercent: number;
  improvementPercent: number | null;
}

export interface LeaderboardEntry {
  rank: number;
  displayName: string;
  sessionsCount: number;
  totalScore: number;
  averagePercent: number;
  isCurrentUser: boolean;
}

export interface LeaderboardResponse {
  entries: LeaderboardEntry[];
  minSessionsRequired: number;
  minQuestionsRequired: number;
}

export interface AuthUser {
  email: string;
  displayName: string;
  role: string;
}

export interface AuthResponse extends AuthUser {
  token: string;
}

export interface RegisterPayload {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  birthDate?: string | null;
  gender?: string | null;
  country?: string | null;
  employmentStatus?: string | null;
  educationStatus?: string | null;
}

export interface SubjectTopics {
  subject: string;
  topics: string[];
}

export interface Paged<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface AdminQuestionOption {
  index: number;
  text: string;
  correct: boolean;
}

export interface AdminQuestion {
  id: string;
  subject: string;
  topic: string;
  text: string;
  difficulty: string;
  active: boolean;
  options: AdminQuestionOption[];
}

export interface QuestionUpsertPayload {
  subject: string;
  topic: string;
  text: string;
  difficulty: string;
  options: string[];
  correctIndex: number;
}

export interface UserSummary {
  id: string;
  email: string;
  displayName: string;
  role: string;
  createdAt: string;
  firstName: string | null;
  lastName: string | null;
  birthDate: string | null;
  gender: string | null;
  country: string | null;
  employmentStatus: string | null;
  educationStatus: string | null;
}

export interface Report {
  id: string;
  questionId: string;
  questionText: string;
  reporterEmail: string;
  message: string;
  status: string;
  createdAt: string;
}

export interface AuditLog {
  id: string;
  adminEmail: string;
  action: string;
  targetType: string;
  targetId: string | null;
  details: string | null;
  createdAt: string;
}

export interface AdminSession {
  id: string;
  userEmail: string;
  userDisplayName: string;
  totalQuestions: number;
  correctAnswers: number;
  score: number;
  percent: number;
  dateTime: string;
}
