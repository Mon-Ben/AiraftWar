package edu.hitsz.dao;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class FileScoreDao implements ScoreDao {
    private final File baseDir;

    public FileScoreDao(Context context) {
        baseDir = new File(context.getFilesDir(), "ranking");
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }

    private File getFile(String difficulty) {
        return new File(baseDir, difficulty + ".txt");
    }

    @Override
    public void addScore(String playerName, int score) {
        Log.d("FileScoreDao", "addScore: name=" + playerName + ", score=" + score + ", difficulty=" + GameConfig.difficulty);
        String diff = GameConfig.difficulty;
        long timestamp = System.currentTimeMillis(); // 当前时间戳
        try (PrintWriter pw = new PrintWriter(new FileWriter(getFile(diff), true))) {
            pw.printf("%s,%d,%d%n", playerName, score, timestamp);
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
                    .filter(arr -> arr.length >= 3)
                    .map(arr -> {
                        try {
                            String name = arr[0];
                            int score = Integer.parseInt(arr[1]);
                            long timestamp = Long.parseLong(arr[2]);
                            return new ScoreItem(name, score, timestamp);
                        } catch (NumberFormatException e) {
                            // 忽略格式错误的行
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
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
                pw.printf("%s,%d,%d%n", item.getPlayerName(), item.getScore(), item.getTimestamp());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}