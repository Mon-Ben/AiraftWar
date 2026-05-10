package edu.hitsz.network;

public final class NetworkConfig {
    public static final String SERVER_HOST = "10.0.2.2";
    public static final int HTTP_PORT = 8080;
    public static final int SOCKET_PORT = 9999;

    public static final String HTTP_BASE_URL = "http://" + SERVER_HOST + ":" + HTTP_PORT;
    public static final String SCORES_URL = HTTP_BASE_URL + "/api/scores";

    private NetworkConfig() {
    }
}
