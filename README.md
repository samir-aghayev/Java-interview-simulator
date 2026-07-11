# Java Interview Simulator

Java texniki müsahibəsini brauzerdə simulyasiya edən veb tətbiq.

- Java sualları verir (50 mövzu, 1000 sual — Core Java, kolleksiyalar/data strukturları, concurrency/JVM, dil xüsusiyyətləri, testing/keyfiyyət, Spring/veb, verilənlər bazası, DevOps/infra, memarlıq/təhlükəsizlik, proses/alətlər sahələrindən)
- Cavabları yoxlayır
- Çətinlik səviyyəsinə görə bal hesablayır
- Zəif mövzuları göstərir
- İnkişaf statistikasını (keçmiş müsahibələr üzrə bal və nəticələr) saxlayır
- Hər sualdan sonra "Asan / Orta / Çətin" seçimi ilə şəxsi qiymətləndirməyə imkan verir — "Asan" işarələnib düzgün cavablanan suallar həmin istifadəçiyə bir daha göstərilmir

Heç bir xarici asılılıq (Maven/Gradle/kitabxana) yoxdur — yalnız JDK.

## İşə salma

```
javac -d out $(find src -name "*.java")
java -cp out web.Server
```

Sonra brauzerdə aç: http://localhost:8080

Fərqli port üçün: `java -cp out web.Server 9090`

Server `public/` qovluğunu statik fayl kimi verir, ona görə əmri layihənin kök qovluğundan (bu README-nin olduğu yerdən) işə salmaq lazımdır.

## Struktur

```
src/
  model/Question.java          - sual (mövzu, variantlar, düzgün cavab, çətinlik)
  model/Difficulty.java        - EASY/MEDIUM/HARD, hər biri bal dəyəri ilə
  model/InterviewSession.java  - tamamlanmış müsahibənin nəticəsi
  model/GradeResult.java       - qiymətləndirmə nəticəsi (bal, düzgün cavablar, zəif mövzular)
  model/QuestionResult.java    - hər sualın fərdi nəticəsi
  model/AnswerSubmission.java  - cavab göndərişi (sual id, seçim, şəxsi çətinlik qiyməti)
  repository/QuestionBank.java - 10 bank faylını birləşdirən aqreqator (1000 sual, 50 mövzu, hərəsi 20 sual)
  repository/bank/*.java       - mövzu qruplarına görə bölünmüş sual faylları (hər biri 100 sual/5 mövzu)
  repository/SessionRepository.java - keçmiş müsahibələrin yaddaşda saxlanması
  repository/MasteryRepository.java - "Asan" işarələnib düzgün cavablanan sualların namizəd üzrə yaddaşda saxlanması
  service/InterviewService.java     - müsahibənin qiymətləndirilməsi, bal hesablanması, statistika
  web/Server.java              - JDK-nin daxili HttpServer-i ilə REST API + statik fayl serveri
  web/JsonUtil.java            - asılılıqsız JSON parse/serialize

public/
  index.html, style.css, app.js - veb UI (sadə HTML/CSS/JS, framework yoxdur)
```

## API

| Metod | Yol | Təsvir |
|---|---|---|
| POST | `/api/quiz/start` | `{candidateName, questionCount}` → təsadüfi seçilmiş suallar (namizədin "Asan+düzgün" işarələdiyi suallar çıxarılmış, düzgün cavab olmadan) |
| POST | `/api/quiz/submit` | `{candidateName, answers:[{questionId, selectedIndex, perceivedDifficulty}]}` → bal, düzgün cavablar, zəif mövzular |
| GET | `/api/stats/weak?candidate=` | Namizədin bütün müsahibələri üzrə mövzu statistikası |
| GET | `/api/stats/progress?candidate=` | Namizədin müsahibə tarixçəsi və orta bal |
