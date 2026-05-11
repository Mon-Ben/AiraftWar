package edu.hitsz.server;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BattleSocketServer {
    private static final int PORT = 9999;
    private static final Gson GSON = new Gson();
    private static final Map<String, Room> ROOMS = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        new BattleSocketServer().start();
    }

    private void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Battle socket server started on port " + PORT);
            while (true) {
                System.out.println("waiting client connect");
                Socket socket = serverSocket.accept();
                System.out.println("accept client connect " + socket);
                new Thread(new ClientService(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final class ClientService implements Runnable {
        private final Socket socket;
        private volatile boolean disconnected;
        private BufferedReader in;
        private PrintWriter out;
        private Room room;
        private String playerId;

        ClientService(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)), true);
                send(BattleMessage.info("CONNECTED", "connection successful"));
                String content;
                while (!disconnected && (content = in.readLine()) != null) {
                    System.out.println("receive from client: " + content);
                    handleMessage(content);
                }
                if (!disconnected) {
                    System.out.println("client readLine returned null, peer closed connection, playerId=" + playerId);
                }
            } catch (Exception e) {
                if (!disconnected) {
                    System.out.println("client disconnected: " + e.getMessage());
                }
            } finally {
                disconnect();
            }
        }

        private void handleMessage(String content) {
            BattleMessage message;
            try {
                message = GSON.fromJson(content, BattleMessage.class);
            } catch (Exception e) {
                send(BattleMessage.error("invalid json"));
                return;
            }
            if (message == null || message.type == null) {
                send(BattleMessage.error("message type required"));
                return;
            }
            System.out.println("handle message type: " + message.type + ", roomId=" + message.roomId + ", playerId=" + playerId);
            switch (message.type) {
                case "CREATE_ROOM":
                    createRoom();
                    break;
                case "JOIN_ROOM":
                    joinRoom(message.roomId);
                    break;
                case "PLAYER_STATE":
                    relayState(message);
                    break;
                case "GAME_OVER":
                    markGameOver(message);
                    break;
                case "DISCONNECT":
                    disconnect();
                    break;
                default:
                    send(BattleMessage.error("unknown type: " + message.type));
            }
        }

        private void createRoom() {
            if (room != null) {
                send(BattleMessage.error("already in room"));
                return;
            }
            String roomId = String.valueOf(1000 + Math.abs(UUID.randomUUID().hashCode() % 9000));
            while (ROOMS.containsKey(roomId)) {
                roomId = String.valueOf(1000 + Math.abs(UUID.randomUUID().hashCode() % 9000));
            }
            room = new Room(roomId);
            playerId = "P1";
            room.player1 = this;
            ROOMS.put(roomId, room);
            BattleMessage response = BattleMessage.info("ROOM_CREATED", "room created");
            response.roomId = roomId;
            response.playerId = playerId;
            send(response);
            System.out.println("room created, roomId=" + roomId + ", playerId=" + playerId);
        }

        private void joinRoom(String roomId) {
            if (room != null) {
                send(BattleMessage.error("already in room"));
                return;
            }
            if (roomId == null || roomId.trim().isEmpty()) {
                send(BattleMessage.error("room id required"));
                return;
            }
            Room target = ROOMS.get(roomId);
            if (target == null) {
                send(BattleMessage.error("room not found"));
                return;
            }
            synchronized (target) {
                if (target.player1 == null) {
                    send(BattleMessage.error("room owner has left"));
                    ROOMS.remove(roomId);
                    return;
                }
                if (target.player2 != null) {
                    send(BattleMessage.error("room is full"));
                    return;
                }
                room = target;
                playerId = "P2";
                target.player2 = this;
            }
            BattleMessage joined = BattleMessage.info("JOINED_ROOM", "joined room");
            joined.roomId = roomId;
            joined.playerId = playerId;
            send(joined);
            System.out.println("joined room, roomId=" + roomId + ", playerId=" + playerId);

            BattleMessage ready = BattleMessage.info("BATTLE_READY", "both players ready");
            ready.roomId = roomId;
            System.out.println("broadcast BATTLE_READY, roomId=" + roomId);
            target.broadcast(ready);
        }

        private void relayState(BattleMessage message) {
            if (room == null) {
                send(BattleMessage.error("not in room"));
                return;
            }
            message.roomId = room.roomId;
            message.playerId = playerId;
            room.updatePlayerState(playerId, message);
            room.sendToOpponent(playerId, message);
        }

        private void markGameOver(BattleMessage message) {
            if (room == null) {
                send(BattleMessage.error("not in room"));
                return;
            }
            message.roomId = room.roomId;
            message.playerId = playerId;
            message.dead = true;
            room.updatePlayerState(playerId, message);
            room.sendToOpponent(playerId, message);
            room.tryEndBattle();
        }

        private void send(BattleMessage message) {
            if (out != null) {
                String json = GSON.toJson(message);
                System.out.println("send to client: " + json);
                out.println(json);
                if (out.checkError()) {
                    System.out.println("send failed, client output stream has error, playerId=" + playerId);
                }
            }
        }

        private void disconnect() {
            if (disconnected) {
                return;
            }
            disconnected = true;
            Room currentRoom = room;
            String currentPlayerId = playerId;
            room = null;
            playerId = null;
            if (currentRoom != null) {
                currentRoom.remove(this, currentPlayerId);
            }
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            System.out.println("client cleanup finished, playerId=" + currentPlayerId);
        }
    }

    private static final class Room {
        final String roomId;
        ClientService player1;
        ClientService player2;
        int p1Score;
        int p2Score;
        boolean p1Dead;
        boolean p2Dead;

        Room(String roomId) {
            this.roomId = roomId;
        }

        synchronized void updatePlayerState(String playerId, BattleMessage message) {
            if ("P1".equals(playerId)) {
                p1Score = message.score;
                p1Dead = message.dead;
            } else if ("P2".equals(playerId)) {
                p2Score = message.score;
                p2Dead = message.dead;
            }
        }

        synchronized void sendToOpponent(String playerId, BattleMessage message) {
            ClientService opponent = "P1".equals(playerId) ? player2 : player1;
            if (opponent != null) {
                opponent.send(message);
            }
        }

        synchronized void broadcast(BattleMessage message) {
            if (player1 != null) {
                player1.send(message);
            }
            if (player2 != null) {
                player2.send(message);
            }
        }

        synchronized void tryEndBattle() {
            if (player1 != null && player2 != null && p1Dead && p2Dead) {
                BattleMessage end = BattleMessage.info("BATTLE_END", "battle ended");
                end.roomId = roomId;
                end.p1Score = p1Score;
                end.p2Score = p2Score;
                broadcast(end);
                ROOMS.remove(roomId);
            }
        }

        synchronized void remove(ClientService client, String playerId) {
            boolean hadOpponent = false;
            if (player1 == client) {
                hadOpponent = player2 != null;
                player1 = null;
            }
            if (player2 == client) {
                hadOpponent = player1 != null;
                player2 = null;
            }
            System.out.println("remove client from room, roomId=" + roomId + ", playerId=" + playerId + ", hadOpponent=" + hadOpponent);
            if (hadOpponent) {
                BattleMessage disconnected = BattleMessage.info("OPPONENT_LEFT", "opponent disconnected");
                disconnected.roomId = roomId;
                broadcast(disconnected);
            }
            if (player1 == null && player2 == null) {
                ROOMS.remove(roomId);
                System.out.println("room removed, roomId=" + roomId);
            }
        }
    }

    private static final class BattleMessage {
        String type;
        String roomId;
        String playerId;
        String message;
        int x;
        int y;
        int hp;
        int score;
        int p1Score;
        int p2Score;
        boolean dead;
        long timestamp;

        static BattleMessage info(String type, String message) {
            BattleMessage battleMessage = new BattleMessage();
            battleMessage.type = type;
            battleMessage.message = message;
            battleMessage.timestamp = System.currentTimeMillis();
            return battleMessage;
        }

        static BattleMessage error(String message) {
            return info("ERROR", message);
        }
    }
}
