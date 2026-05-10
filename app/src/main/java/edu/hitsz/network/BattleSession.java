package edu.hitsz.network;

public final class BattleSession {
    private static BattleSocketClient socketClient;

    private BattleSession() {
    }

    public static void setSocketClient(BattleSocketClient client) {
        socketClient = client;
    }

    public static BattleSocketClient getSocketClient() {
        return socketClient;
    }

    public static void clear() {
        socketClient = null;
    }
}
