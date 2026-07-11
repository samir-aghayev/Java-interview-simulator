package repository;

import model.Difficulty;
import model.Question;

import java.util.List;

public class QuestionBank {

    private static final List<Question> QUESTIONS = List.of(
            new Question("OOP", "Java-da hansı açar söz bir sinifin başqa sinifdən miras almasını təmin edir?",
                    List.of("implements", "extends", "inherits", "super"), 1, Difficulty.EASY),
            new Question("OOP", "Aşağıdakılardan hansı Java-da çoxvarislik (multiple inheritance) əldə etmək üçün istifadə olunur?",
                    List.of("Abstract class", "Interface", "Final class", "Static class"), 1, Difficulty.MEDIUM),
            new Question("OOP", "Encapsulation (inkapsulyasiya) nəyi ifadə edir?",
                    List.of("Kod təkrarının aradan qaldırılması", "Data və metodların bir sinif daxilində gizlədilməsi",
                            "Sinifin genişləndirilməsi", "Metodun overload edilməsi"), 1, Difficulty.EASY),
            new Question("OOP", "Java-da 'this' açar sözü nə üçün istifadə olunur?",
                    List.of("Statik dəyişənə müraciət üçün", "Cari obyektə istinad üçün",
                            "Valideyn sinifə müraciət üçün", "Yeni obyekt yaratmaq üçün"), 1, Difficulty.EASY),

            new Question("Kolleksiyalar", "ArrayList və LinkedList arasındakı əsas fərq nədir?",
                    List.of("ArrayList sıralanmır", "ArrayList array əsaslıdır, LinkedList node əsaslıdır",
                            "LinkedList thread-safe-dir", "Fərq yoxdur"), 1, Difficulty.MEDIUM),
            new Question("Kolleksiyalar", "HashMap-də açarların unikallığı necə təmin olunur?",
                    List.of("equals() və hashCode() metodları ilə", "compareTo() metodu ilə",
                            "toString() metodu ilə", "clone() metodu ilə"), 0, Difficulty.MEDIUM),
            new Question("Kolleksiyalar", "Hansı kolleksiya interfeysi dublikat elementlərə icazə vermir?",
                    List.of("List", "Set", "Queue", "Map (values)"), 1, Difficulty.EASY),
            new Question("Kolleksiyalar", "TreeMap elementləri necə saxlayır?",
                    List.of("Təsadüfi ardıcıllıqla", "Açarların təbii sıralaması ilə",
                            "Əlavə olunma ardıcıllığı ilə", "Heç bir sıralama olmadan"), 1, Difficulty.HARD),

            new Question("İstisnalar", "Checked və unchecked exception arasındakı fərq nədir?",
                    List.of("Checked exception-lar compile zamanı yoxlanılır", "Unchecked exception-lar compile zamanı yoxlanılır",
                            "Fərq yoxdur", "Checked exception-lar yalnız runtime-da olur"), 0, Difficulty.MEDIUM),
            new Question("İstisnalar", "finally bloku nə vaxt icra olunur?",
                    List.of("Yalnız exception atılarsa", "Yalnız exception atılmazsa",
                            "Həmişə, exception olub-olmamasından asılı olmayaraq", "Heç vaxt"), 2, Difficulty.EASY),
            new Question("İstisnalar", "Öz custom exception sinifinizi yaratmaq üçün hansı sinifi extend etməlisiniz?",
                    List.of("Throwable və ya Exception", "Object", "RuntimeError", "Error only"), 0, Difficulty.MEDIUM),
            new Question("İstisnalar", "try-with-resources nə üçün istifadə olunur?",
                    List.of("Exception-ları tutmaq üçün", "Resursların avtomatik bağlanması üçün",
                            "Yeni thread yaratmaq üçün", "Statik dəyişən elan etmək üçün"), 1, Difficulty.MEDIUM),

            new Question("Multithreading", "Java-da yeni thread yaratmağın neçə əsas yolu var?",
                    List.of("1", "2 (Thread sinifi və Runnable interfeysi)", "3", "4"), 1, Difficulty.EASY),
            new Question("Multithreading", "'synchronized' açar sözü nə üçün istifadə olunur?",
                    List.of("Kodu sürətləndirmək üçün", "Bir vaxtda yalnız bir thread-in bloka girməsini təmin etmək üçün",
                            "Yeni thread yaratmaq üçün", "Thread-i dayandırmaq üçün"), 1, Difficulty.MEDIUM),
            new Question("Multithreading", "Thread.sleep() metodu nə edir?",
                    List.of("Thread-i həmişəlik dayandırır", "Thread-i müəyyən müddətə pauza edir",
                            "Thread-i öldürür", "Yeni thread yaradır"), 1, Difficulty.EASY),
            new Question("Multithreading", "Deadlock nə deməkdir?",
                    List.of("Thread-lərin bir-birini gözləyərək sonsuz bloklanması", "Thread-in normal bitməsi",
                            "Thread-in sürətlə işləməsi", "Bir thread-in digərini yaratması"), 0, Difficulty.HARD),

            new Question("JVM və Yaddaş", "Garbage Collector-un vəzifəsi nədir?",
                    List.of("Kodu compile etmək", "İstifadə olunmayan obyektləri yaddaşdan silmək",
                            "Thread-ləri idarə etmək", "Faylları oxumaq"), 1, Difficulty.EASY),
            new Question("JVM və Yaddaş", "Java-da 'Heap' yaddaşı nə üçün istifadə olunur?",
                    List.of("Statik dəyişənlər üçün", "Obyektlərin saxlanması üçün",
                            "Metod çağırışları üçün", "Yalnız primitivlər üçün"), 1, Difficulty.MEDIUM),
            new Question("JVM və Yaddaş", "JVM, JRE və JDK arasındakı fərq nədir?",
                    List.of("Eynidir", "JDK development üçün, JRE işə salma üçün, JVM bytecode icrası üçündür",
                            "JVM yalnız compile edir", "JRE yalnız development üçündür"), 1, Difficulty.MEDIUM),
            new Question("JVM və Yaddaş", "'static' açar sözü ilə elan olunan dəyişən nəyə aiddir?",
                    List.of("Yalnız bir obyektə", "Sinifin özünə, bütün obyektlər üçün ortaqdır",
                            "Yalnız local metoda", "Heç nəyə"), 1, Difficulty.EASY)
    );

    public static List<Question> getQuestions() {
        return QUESTIONS;
    }
}
