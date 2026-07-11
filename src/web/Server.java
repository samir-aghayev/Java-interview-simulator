package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import model.AnswerSubmission;
import model.GradeResult;
import model.Question;
import model.QuestionResult;
import service.InterviewService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class Server {

    private static final Path STATIC_ROOT = Path.of("public");
    private static final InterviewService interviewService = new InterviewService();

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/quiz/start", Server::handleStart);
        server.createContext("/api/quiz/submit", Server::handleSubmit);
        server.createContext("/api/stats/weak", Server::handleWeak);
        server.createContext("/api/stats/progress", Server::handleProgress);
        server.createContext("/", Server::handleStatic);
        server.start();
        System.out.println("Java Interview Simulator: http://localhost:" + port);
    }

    private static void handleStart(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Method not allowed"));
            return;
        }
        Map<?, ?> body = parseBody(exchange);
        int count = body.containsKey("questionCount") ? ((Number) body.get("questionCount")).intValue() : 10;
        String candidateName = String.valueOf(body.get("candidateName"));

        List<Question> questions = interviewService.pickRandomQuestions(candidateName, count);
        List<Object> payload = questions.stream().map(Server::questionPayload).collect(Collectors.toList());
        sendJson(exchange, 200, Map.of("questions", payload));
    }

    private static void handleSubmit(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendJson(exchange, 405, Map.of("error", "Method not allowed"));
            return;
        }
        Map<?, ?> body = parseBody(exchange);
        String candidateName = String.valueOf(body.get("candidateName"));
        List<?> answers = body.get("answers") instanceof List<?> list ? list : List.of();

        List<AnswerSubmission> submissions = new ArrayList<>();
        for (Object o : answers) {
            Map<?, ?> a = (Map<?, ?>) o;
            UUID id = UUID.fromString(String.valueOf(a.get("questionId")));
            int selected = ((Number) a.get("selectedIndex")).intValue();
            Object perceivedDifficulty = a.get("perceivedDifficulty");
            submissions.add(new AnswerSubmission(id, selected, perceivedDifficulty == null ? null : String.valueOf(perceivedDifficulty)));
        }

        GradeResult result = interviewService.grade(candidateName, submissions);

        List<Object> details = new ArrayList<>();
        for (QuestionResult qr : result.details()) {
            Map<String, Object> row = questionPayload(qr.question());
            row.put("selectedIndex", qr.selectedIndex());
            row.put("correctIndex", qr.question().getCorrectOptionIndex());
            row.put("correct", qr.correct());
            details.add(row);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalQuestions", result.totalQuestions());
        response.put("correctAnswers", result.correctAnswers());
        response.put("score", result.score());
        response.put("weakTopics", new ArrayList<>(result.weakTopics()));
        response.put("details", details);
        sendJson(exchange, 200, response);
    }

    private static void handleWeak(HttpExchange exchange) throws IOException {
        String candidateName = queryParam(exchange, "candidate");
        sendJson(exchange, 200, interviewService.weakTopicsSummary(candidateName));
    }

    private static void handleProgress(HttpExchange exchange) throws IOException {
        String candidateName = queryParam(exchange, "candidate");
        sendJson(exchange, 200, interviewService.progressSummary(candidateName));
    }

    private static void handleStatic(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();
        if (requestPath.equals("/")) {
            requestPath = "/index.html";
        }
        Path filePath = STATIC_ROOT.resolve(requestPath.substring(1)).normalize();
        if (!filePath.startsWith(STATIC_ROOT) || !Files.exists(filePath) || Files.isDirectory(filePath)) {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
            return;
        }
        byte[] bytes = Files.readAllBytes(filePath);
        exchange.getResponseHeaders().add("Content-Type", contentType(filePath.toString()));
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, Object> questionPayload(Question question) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", question.getId().toString());
        row.put("topic", question.getTopic());
        row.put("text", question.getText());
        row.put("options", question.getOptions());
        return row;
    }

    private static String contentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=utf-8";
        if (path.endsWith(".css")) return "text/css; charset=utf-8";
        if (path.endsWith(".js")) return "application/javascript; charset=utf-8";
        return "application/octet-stream";
    }

    private static String queryParam(HttpExchange exchange, String name) {
        String query = exchange.getRequestURI().getRawQuery();
        if (query == null) {
            return null;
        }
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv[0].equals(name)) {
                return URLDecoder.decode(kv.length > 1 ? kv[1] : "", StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private static Map<?, ?> parseBody(HttpExchange exchange) throws IOException {
        String raw = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (raw.isBlank()) {
            return Map.of();
        }
        return (Map<?, ?>) JsonUtil.parse(raw);
    }

    private static void sendJson(HttpExchange exchange, int status, Object payload) throws IOException {
        byte[] bytes = JsonUtil.toJson(payload).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
