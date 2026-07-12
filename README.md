# Java Interview Simulator

Java texniki müsahibəsini brauzerdə simulyasiya edən veb tətbiq.

- Java sualları verir (50 mövzu, 1000 sual — Core Java, kolleksiyalar/data strukturları, concurrency/JVM, dil xüsusiyyətləri, testing/keyfiyyət, Spring/veb, verilənlər bazası, DevOps/infra, memarlıq/təhlükəsizlik, proses/alətlər sahələrindən)
- Cavabları yoxlayır
- Çətinlik səviyyəsinə görə bal hesablayır
- Zəif mövzuları göstərir
- İnkişaf statistikasını (keçmiş müsahibələr üzrə bal və nəticələr) saxlayır
- Hər sualdan sonra "Asan / Orta / Çətin" seçimi ilə şəxsi qiymətləndirməyə imkan verir — "Asan" işarələnib düzgün cavablanan suallar həmin istifadəçiyə bir daha göstərilmir
- Cavab variantlarının sırası hər sorğuda yenidən qarışdırılır (mövqeyə görə əzbərləmənin qarşısını almaq üçün — eyni sualı ikinci dəfə görəndə düzgün cavab fərqli yerdə ola bilər)
- Sign Up / Sign In ilə real istifadəçi hesabları (email + şifrə, bcrypt hash) — eyni anda yüzlərlə istifadəçi öz müsahibə tarixçəsini ayrıca saxlaya bilər
- İstifadəçi rolları (USER/ADMIN) və Admin Panel: istifadəçi rollarının idarəsi, sual əlavə/redaktə/deaktiv/bərpa (soft delete — keçmiş nəticələrin bütövlüyü pozulmur), axtarış + səhifələmə, bütün admin əməliyyatlarının audit logu

## Stack

- **Gradle** (build)
- **Spring Boot 3** (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-security`, `spring-boot-starter-validation`)
- **PostgreSQL** (məlumat bazası)
- **Liquibase** (sxem və seed data miqrasiyaları)
- **JWT** (stateless autentifikasiya, `jjwt`)

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

   Bağlantı parametrləri `application.yml`-də mühit dəyişənləri ilə override oluna bilər: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `SERVER_PORT`, `JWT_SECRET`, `JWT_EXPIRATION_MINUTES`. **Production-a keçməzdən əvvəl `JWT_SECRET` mütləq dəyişdirilməlidir.**

   İlk admini yaratmaq üçün: `ADMIN_EMAIL=admin@example.com ADMIN_PASSWORD=... ./gradlew bootRun` — həmin email mövcuddursa ADMIN roluna yüksəldilir, yoxdursa bu şifrə ilə yaradılır.

3. Brauzerdə aç: http://localhost:8080

## Struktur

```
src/main/java/com/interviewsimulator/
  InterviewSimulatorApplication.java   - Spring Boot giriş nöqtəsi
  entity/         - JPA entity-ləri (UserEntity, QuestionEntity, QuestionOptionEntity, InterviewSessionEntity, SessionTopicStatEntity, MasteredQuestionEntity)
  repository/     - Spring Data JPA repository interfeysləri
  service/        - InterviewService: sualların seçilməsi, qiymətləndirmə, statistika
  security/       - JWT yaradılması/yoxlanması, Spring Security konfiqurasiyası, autentifikasiya endpoint-ləri üçün rate limiting
  dto/            - REST API üçün request/response modelləri
  web/            - QuizController və AuthController (REST endpoint-lər)

src/main/resources/
  application.yml               - server və verilənlər bazası konfiqurasiyası
  db/changelog/                 - Liquibase changelog-ları (sxem + 1000 sualın seed data-sı)
  static/                       - veb UI (sadə HTML/CSS/JS, framework yoxdur)
```

## API

Autentifikasiya endpoint-ləri istisna olmaqla, bütün endpoint-lər `Authorization: Bearer <token>` header-i tələb edir.

| Metod | Yol | Təsvir |
|---|---|---|
| POST | `/api/auth/register` | `{email, password, displayName}` → hesab yaradır, `{token, email, displayName, role}` qaytarır |
| POST | `/api/auth/login` | `{email, password}` → `{token, email, displayName, role}` qaytarır |
| POST | `/api/quiz/start` | `{questionCount}` → təsadüfi seçilmiş suallar (istifadəçinin "Asan+düzgün" işarələdiyi suallar çıxarılmış, hər sualın variantları həmin sorğu üçün təzədən qarışdırılmış, `options: [{index, text}]` formatında — `index` sualın kanonik/authoring indeksidir, göstərilən sıra deyil) |
| POST | `/api/quiz/submit` | `{answers:[{questionId, selectedIndex, perceivedDifficulty}]}` → bal, düzgün cavablar, zəif mövzular (`selectedIndex` kanonik indeksdir, `/api/quiz/start`-da alınan `option.index` dəyəri) |
| GET | `/api/stats/weak` | Cari istifadəçinin bütün müsahibələri üzrə mövzu statistikası |
| GET | `/api/stats/progress` | Cari istifadəçinin müsahibə tarixçəsi və orta bal |

Autentifikasiya endpoint-ləri IP üzrə sadə rate limiting ilə qorunur (dəqiqədə maks. 10 cəhd).

### Admin API (yalnız ADMIN rolu)

| Metod | Yol | Təsvir |
|---|---|---|
| GET | `/api/admin/users?search=&page=&size=` | İstifadəçi siyahısı (axtarış + səhifələmə) |
| PATCH | `/api/admin/users/{id}/role` | `{role: USER\|ADMIN}` — rol dəyişikliyi (admin öz rolunu dəyişə bilməz) |
| GET | `/api/admin/questions?search=&page=&size=` | Sual siyahısı (mövzu/mətn üzrə axtarış + səhifələmə, deaktivlər daxil) |
| POST | `/api/admin/questions` | `{topic, text, difficulty, options[2-6], correctIndex}` — yeni sual |
| PUT | `/api/admin/questions/{id}` | Sualın redaktəsi |
| DELETE | `/api/admin/questions/{id}` | Soft delete — sual deaktiv olur, quiz hovuzundan çıxır, keçmiş nəticələr toxunulmaz qalır |
| POST | `/api/admin/questions/{id}/restore` | Deaktiv sualın bərpası |
| GET | `/api/admin/audit?page=&size=` | Admin əməliyyatlarının audit logu (kim, nə vaxt, nə etdi) |

Qeyd: rol dəyişiklikləri dərhal təsirlidir — JWT filtri rolu tokendəki claim-dən deyil, hər sorğuda bazadan oxuyur (demote edilmiş adminin köhnə tokeni ilə admin qalması mümkün deyil). Admin panelin görünməsi üçün yüksəldilmiş istifadəçi yenidən daxil olmalıdır (frontend rolu login cavabından götürür).
