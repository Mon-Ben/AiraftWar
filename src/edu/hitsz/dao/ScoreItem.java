package edu.hitsz.dao;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScoreItem implements Comparable<ScoreItem> {
    private final String playerName;
    private final int score;
    private final LocalDateTime recordTime;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    public ScoreItem(String playerName, int score, LocalDateTime recordTime) {
        this.playerName = playerName;
        this.score = score;
        this.recordTime = recordTime;
    }

    public String getPlayerName() { return playerName; }
    public int getScore() { return score; }
    public LocalDateTime getRecordTime() { return recordTime; }

    @Override
    public int compareTo(ScoreItem o) {
        return Integer.compare(o.score, this.score); // 降序
    }

    /** 控制台打印格式：名次 | 玩家名 | 得分 | 时间 */
    public String toConsoleString(int rank) {
        return String.format("%-4d| %-12s | %-6d | %s",
                rank, playerName, score, recordTime.format(FMT));
    }

    public String getRankString() {
        return recordTime.format(FMT);
    }
}