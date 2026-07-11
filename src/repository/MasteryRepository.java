package repository;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MasteryRepository {
    private static final Map<String, Set<UUID>> MASTERED_QUESTIONS = new ConcurrentHashMap<>();

    public static Map<String, Set<UUID>> getMasteredQuestions() {
        return MASTERED_QUESTIONS;
    }
}
