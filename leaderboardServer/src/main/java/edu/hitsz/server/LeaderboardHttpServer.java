package edu.hitsz.server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;

public class LeaderboardHttpServer {
    private static final int PORT = 8080;
    private static final String SCORE_PATH = "/api/scores";
    private static final String HEALTH_PATH = "/api/health";
    private static final File STORE_FILE = new File("leaderboard_scores.json");
    private static final Gson GSON = new Gson();
    private static final Object LOCK = new Object();
    private static final List<LeaderboardEntry> SCORES = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        loadScores();
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 10);
        server.createContext(SCORE_PATH, new ScoresHandler());
        server.createContext(HEALTH_PATH, new HealthHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Leaderboard HTTP server started: http://localhost:" + PORT);
        System.out.println("POST/GET " + SCORE_PATH);
    }

    private static final class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            writeJson(exchange, 200, "{\"status\":\"ok\"}");
        }
    }

    private static final class ScoresHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if ("OPTIONS".equalsIgnoreCase(method)) {
                addHeaders(exchange);
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }
            if ("GET".equalsIgnoreCase(method)) {
                handleGet(exchange);
                return;
            }
            if ("POST".equalsIgnoreCase(method)) {
                handlePost(exchange);
                return;
            }
            writeJson(exchange, 405, "{\"error\":\"method not allowed\"}");
        }

        private void handleGet(HttpExchange exchange) throws IOException {
            List<LeaderboardEntry> snapshot;
            synchronized (LOCK) {
                snapshot = new ArrayList<>(SCORES);
            }
            snapshot.sort(Comparator.comparingInt((LeaderboardEntry entry) -> entry.score).reversed());
            writeJson(exchange, 200, GSON.toJson(snapshot));
        }

        private void handlePost(HttpExchange exchange) throws IOException {
            String body = readBody(exchange.getRequestBody());
            LeaderboardEntry entry;
            try {
                entry = GSON.fromJson(body, LeaderboardEntry.class);
            } catch (Exception e) {
                writeJson(exchange, 400, "{\"error\":\"invalid json\"}");
                return;
            }
            if (entry == null || entry.nickname == null || entry.nickname.trim().isEmpty()) {
                writeJson(exchange, 400, "{\"error\":\"nickname required\"}");
                return;
            }
            if (entry.timestamp <= 0) {
                entry.timestamp = System.currentTimeMillis();
            }
            if (entry.difficulty == null || entry.difficulty.trim().isEmpty()) {
                entry.difficulty = "UNKNOWN";
            }
            synchronized (LOCK) {
                SCORES.add(entry);
                SCORES.sort(Comparator.comparingInt((LeaderboardEntry score) -> score.score).reversed());
                saveScores();
            }
            writeJson(exchange, 200, "{\"success\":true}");
        }
    }

    private static String readBody(InputStream inputStream) throws IOException {
        byte[] bytes = inputStream.readAllBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static void addHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=UTF-8");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void writeJson(HttpExchange exchange, int statusCode, String response) throws IOException {
        addHeaders(exchange);
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }

    private static void loadScores() {
        if (!STORE_FILE.exists()) {
            return;
        }
        synchronized (LOCK) {
            try (FileReader reader = new FileReader(STORE_FILE)) {
                Type type = new TypeToken<List<LeaderboardEntry>>() { }.getType();
                List<LeaderboardEntry> stored = GSON.fromJson(reader, type);
                if (stored != null) {
                    SCORES.clear();
                    SCORES.addAll(stored);
                }
            } catch (Exception e) {
                System.out.println("Failed to load scores: " + e.getMessage());
            }
        }
    }

    private static void saveScores() {
        try (FileWriter writer = new FileWriter(STORE_FILE)) {
            GSON.toJson(SCORES, writer);
        } catch (IOException e) {
            System.out.println("Failed to save scores: " + e.getMessage());
        }
    }

    private static final class LeaderboardEntry {
        String nickname;
        int score;
        String difficulty;
        long timestamp;
    }
}
