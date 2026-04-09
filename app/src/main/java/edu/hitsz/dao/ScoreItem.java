package edu.hitsz.dao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScoreItem implements Comparable<ScoreItem> {
    private final String playerName;
    private final int score;
    private final long timestamp;  // 存储时间戳（毫秒）
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());

    public ScoreItem(String playerName, int score, long timestamp) {
        this.playerName = playerName;
        this.score = score;
        this.timestamp = timestamp;
    }

    public String getPlayerName() { return playerName; }
    public int getScore() { return score; }
    public long getTimestamp() { return timestamp; }

    // 格式化时间字符串
    public String getFormattedTime() {
        return dateFormat.format(new Date(timestamp));
    }

    @Override
    public int compareTo(ScoreItem o) {
        return Integer.compare(o.score, this.score); // 降序
    }

    public String toConsoleString(int rank) {
        return String.format("%-4d| %-12s | %-6d | %s",
                rank, playerName, score, getFormattedTime());
    }

    // 用于排行榜列表显示的时间字符串
    public String getRankString() {
        return getFormattedTime();
    }
}