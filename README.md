# Java Interview Simulator

Konsol tətbiqi: Java texniki müsahibəsini simulyasiya edir.

- Java sualları verir (OOP, Kolleksiyalar, İstisnalar, Multithreading, JVM və Yaddaş)
- Cavabları yoxlayır
- Çətinlik səviyyəsinə görə bal hesablayır
- Zəif mövzuları göstərir
- İnkişaf statistikasını (keçmiş müsahibələr üzrə bal və nəticələr) saxlayır

## İşə salma

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
  repository/QuestionBank.java - hazır sual bankı
  repository/SessionRepository.java - keçmiş müsahibələrin yaddaşda saxlanması
  service/InterviewService.java     - müsahibənin aparılması, bal hesablanması, statistika
```
