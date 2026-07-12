# Java Interview Simulator

Java texniki müsahibəsini brauzerdə simulyasiya edən veb tətbiq.

- Java sualları verir (50 mövzu, 1000 sual — Core Java, kolleksiyalar/data strukturları, concurrency/JVM, dil xüsusiyyətləri, testing/keyfiyyət, Spring/veb, verilənlər bazası, DevOps/infra, memarlıq/təhlükəsizlik, proses/alətlər sahələrindən)
- Cavabları yoxlayır
- Çətinlik səviyyəsinə görə bal hesablayır
- Zəif mövzuları göstərir
- İnkişaf statistikasını (keçmiş müsahibələr üzrə bal və nəticələr) saxlayır
- Hər sualdan sonra "Asan / Orta / Çətin" seçimi ilə şəxsi qiymətləndirməyə imkan verir — "Asan" işarələnib düzgün cavablanan suallar həmin istifadəçiyə bir daha göstərilmir
- Cavab variantlarının sırası hər sorğuda yenidən qarışdırılır (mövqeyə görə əzbərləmənin qarşısını almaq üçün — eyni sualı ikinci dəfə görəndə düzgün cavab fərqli yerdə ola bilər)

## Stack

- **Gradle** (build)
- **Spring Boot 3** (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`)
- **PostgreSQL** (məlumat bazası)
- **Liquibase** (sxem və seed data miqrasiyaları)

## İşə salma

1. PostgreSQL-də baza və istifadəçi yaradın (bir dəfə):

   ```sql
   CREATE DATABASE interview_simulator;
   CREATE USER interview_app WITH PASSWORD 'interview_app_pw';
   GRANT ALL PRIVILEGES ON DATABASE interview_simulator TO interview_app;
   ```

2. Tətbiqi işə salın (Liquibase miqrasiyaları avtomatik icra olunur, 1000 sual seed data kimi yüklənir):

   ```
   ./gradlew bootRun
   ```

   Bağlantı parametrləri `application.yml`-də mühit dəyişənləri ilə override oluna bilər: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `SERVER_PORT`.

3. Brauzerdə aç: http://localhost:8080

## Struktur

```
src/main/java/com/interviewsimulator/
  InterviewSimulatorApplication.java   - Spring Boot giriş nöqtəsi
  entity/         - JPA entity-ləri (QuestionEntity, QuestionOptionEntity, InterviewSessionEntity, SessionTopicStatEntity, MasteredQuestionEntity)
  repository/     - Spring Data JPA repository interfeysləri
  service/        - InterviewService: sualların seçilməsi, qiymətləndirmə, statistika
  dto/            - REST API üçün request/response modelləri
  web/            - QuizController (REST endpoint-lər)

src/main/resources/
  application.yml               - server və verilənlər bazası konfiqurasiyası
  db/changelog/                 - Liquibase changelog-ları (sxem + 1000 sualın seed data-sı)
  static/                       - veb UI (sadə HTML/CSS/JS, framework yoxdur)
```

## API

| Metod | Yol | Təsvir |
|---|---|---|
| POST | `/api/quiz/start` | `{candidateName, questionCount}` → təsadüfi seçilmiş suallar (namizədin "Asan+düzgün" işarələdiyi suallar çıxarılmış, hər sualın variantları həmin sorğu üçün təzədən qarışdırılmış, `options: [{index, text}]` formatında — `index` sualın kanonik/authoring indeksidir, göstərilən sıra deyil) |
| POST | `/api/quiz/submit` | `{candidateName, answers:[{questionId, selectedIndex, perceivedDifficulty}]}` → bal, düzgün cavablar, zəif mövzular (`selectedIndex` kanonik indeksdir, `/api/quiz/start`-da alınan `option.index` dəyəri) |
| GET | `/api/stats/weak?candidate=` | Namizədin bütün müsahibələri üzrə mövzu statistikası |
| GET | `/api/stats/progress?candidate=` | Namizədin müsahibə tarixçəsi və orta bal |
