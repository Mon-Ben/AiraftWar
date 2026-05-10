package edu.hitsz.network;

public class BattleMessage {
    public String type;
    public String roomId;
    public String playerId;
    public String message;
    public int x;
    public int y;
    public int hp;
    public int score;
    public int p1Score;
    public int p2Score;
    public boolean dead;
    public long timestamp;

    public static BattleMessage create(String type) {
        BattleMessage battleMessage = new BattleMessage();
        battleMessage.type = type;
        battleMessage.timestamp = System.currentTimeMillis();
        return battleMessage;
    }
}
