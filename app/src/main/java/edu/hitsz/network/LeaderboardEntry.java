package edu.hitsz.network;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LeaderboardEntry {
    private String nickname;
    private int score;
    private String difficulty;
    private long timestamp;

    public LeaderboardEntry() {
    }

    public LeaderboardEntry(String nickname, int score, String difficulty, long timestamp) {
        this.nickname = nickname;
        this.score = score;
        this.difficulty = difficulty;
        this.timestamp = timestamp;
    }

    public String getNickname() {
        return nickname;
    }

    public int getScore() {
        return score;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getFormattedTime() {
        return new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(new Date(timestamp));
    }
}
