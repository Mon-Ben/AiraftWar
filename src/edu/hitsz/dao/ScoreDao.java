package edu.hitsz.dao;

import java.util.List;

public interface ScoreDao {
    /** 写入一条记录（游戏结束时调用） */
    void addScore(String playerName, int score);

    /** 查询当前难度排行榜（降序） */
    List<ScoreItem> listByDifficulty(String difficulty);

    /** 控制台删除指定序号（1 开始） */
    void deleteByRank(String difficulty, int rank);

    /** 控制台打印排行榜（含标题） */
    default void printRank(String difficulty) {
        List<ScoreItem> list = listByDifficulty(difficulty);
        System.out.println("\n==========  " + difficulty + " 排行榜  ==========");
        if (list.isEmpty()) {
            System.out.println("( 暂无记录 )");
            return;
        }
        System.out.println("名次 | 玩家名       | 得分   | 记录时间");
        for (int i = 0; i < list.size(); i++) {
            System.out.println(list.get(i).toConsoleString(i + 1));
        }
        System.out.println("========================================\n");
    }
}