# Java Interview Simulator

Java texniki müsahibəsini simulyasiya edir — həm konsolda, həm də veb brauzerdə.

- Java sualları verir (9 mövzu, 54 sual: OOP, Kolleksiyalar, İstisnalar, Multithreading, JVM və Yaddaş, String, Generics, Stream və Lambda, SOLID Prinsipləri)
- Cavabları yoxlayır
- Çətinlik səviyyəsinə görə bal hesablayır
- Zəif mövzuları göstərir
- İnkişaf statistikasını (keçmiş müsahibələr üzrə bal və nəticələr) saxlayır

Heç bir xarici asılılıq (Maven/Gradle/kitabxana) yoxdur — yalnız JDK.

## Veb UI ilə işə salma

```
javac -d out $(find src -name "*.java")
java -cp out web.Server
```

Sonra brauzerdə aç: http://localhost:8080

Fərqli port üçün: `java -cp out web.Server 9090`

Server `public/` qovluğunu statik fayl kimi verir, ona görə əmri layihənin kök qovluğundan (bu README-nin olduğu yerdən) işə salmaq lazımdır.

## Konsol versiyası ilə işə salma

```
javac -d out $(find src -name "*.java")
java -cp out Main
```

## Struktur

```
src/
  Main.java                    - konsol menyusu
  model/Question.java          - sual (mövzu, variantlar, düzgün cavab, çətinlik)
  model/Difficulty.java        - EASY/MEDIUM/HARD, hər biri bal dəyəri ilə
  model/InterviewSession.java  - tamamlanmış müsahibənin nəticəsi
  model/GradeResult.java       - qiymətləndirmə nəticəsi (bal, düzgün cavablar, zəif mövzular)
  model/QuestionResult.java    - hər sualın fərdi nəticəsi
  repository/QuestionBank.java - hazır sual bankı
  repository/SessionRepository.java - keçmiş müsahibələrin yaddaşda saxlanması
  service/InterviewService.java     - müsahibənin aparılması, bal hesablanması, statistika
                                       (həm konsol, həm veb API tərəfindən istifadə olunur)
  web/Server.java              - JDK-nin daxili HttpServer-i ilə REST API + statik fayl serveri
  web/JsonUtil.java            - asılılıqsız JSON parse/serialize

public/
  index.html, style.css, app.js - veb UI (sadə HTML/CSS/JS, framework yoxdur)
```

## API

| Metod | Yol | Təsvir |
|---|---|---|
| POST | `/api/quiz/start` | `{questionCount}` → təsadüfi seçilmiş suallar (düzgün cavab olmadan) |
| POST | `/api/quiz/submit` | `{candidateName, answers:[{questionId, selectedIndex}]}` → bal, düzgün cavablar, zəif mövzular |
| GET | `/api/stats/weak?candidate=` | Namizədin bütün müsahibələri üzrə mövzu statistikası |
| GET | `/api/stats/progress?candidate=` | Namizədin müsahibə tarixçəsi və orta bal |
