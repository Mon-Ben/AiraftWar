package edu.hitsz.dao;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class FileScoreDao implements ScoreDao {
    private static final String BASE_DIR = "ranking";

    public FileScoreDao() {
        try {
            Files.createDirectories(Paths.get(BASE_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getFile(String difficulty) {
        return new File(BASE_DIR, difficulty + ".txt");
    }

    @Override
    public void addScore(String playerName, int score) {
        String diff = GameConfig.difficulty;
        ScoreItem item = new ScoreItem(playerName, score, LocalDateTime.now());
        try (PrintWriter pw = new PrintWriter(new FileWriter(getFile(diff), true))) {
            pw.printf("%s,%d,%s%n", item.getPlayerName(), item.getScore(), item.getRecordTime());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<ScoreItem> listByDifficulty(String difficulty) {
        File file = getFile(difficulty);
        if (!file.exists()) return new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            return br.lines()
                    .map(l -> l.split(","))
                    .map(arr -> new ScoreItem(arr[0], Integer.parseInt(arr[1]), LocalDateTime.parse(arr[2])))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public void deleteByRank(String difficulty, int rank) {
        List<ScoreItem> list = listByDifficulty(difficulty);
        if (rank < 1 || rank > list.size()) return;
        list.remove(rank - 1);
        try (PrintWriter pw = new PrintWriter(new FileWriter(getFile(difficulty)))) {
            for (ScoreItem item : list) {
                pw.printf("%s,%d,%s%n", item.getPlayerName(), item.getScore(), item.getRecordTime());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}