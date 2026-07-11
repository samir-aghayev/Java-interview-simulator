package repository;

import model.Question;
import repository.bank.ArchitectureSecurityQuestions;
import repository.bank.CollectionsDataStructuresQuestions;
import repository.bank.ConcurrencyJvmQuestions;
import repository.bank.CoreOopQuestions;
import repository.bank.DatabaseQuestions;
import repository.bank.DevOpsInfraQuestions;
import repository.bank.LanguageFeaturesQuestions;
import repository.bank.ProcessToolsQuestions;
import repository.bank.SpringWebQuestions;
import repository.bank.TestingQualityQuestions;

import java.util.List;
import java.util.stream.Stream;

public class QuestionBank {

    private static final List<Question> QUESTIONS = Stream.of(
                    CoreOopQuestions.get(),
                    CollectionsDataStructuresQuestions.get(),
                    ConcurrencyJvmQuestions.get(),
                    LanguageFeaturesQuestions.get(),
                    TestingQualityQuestions.get(),
                    SpringWebQuestions.get(),
                    DatabaseQuestions.get(),
                    DevOpsInfraQuestions.get(),
                    ArchitectureSecurityQuestions.get(),
                    ProcessToolsQuestions.get())
            .flatMap(List::stream)
            .toList();

    public static List<Question> getQuestions() {
        return QUESTIONS;
    }
}
