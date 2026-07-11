package repository;

import model.InterviewSession;

import java.util.ArrayList;
import java.util.List;

public class SessionRepository {
    private static final List<InterviewSession> SESSIONS = new ArrayList<>();

    public static List<InterviewSession> getSessions() {
        return SESSIONS;
    }
}
