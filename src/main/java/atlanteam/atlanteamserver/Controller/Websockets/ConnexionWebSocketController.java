package atlanteam.atlanteamserver.Controller.Websockets;

import atlanteam.atlanteamserver.models.Obstacle;
import atlanteam.atlanteamserver.models.Player;
import atlanteam.atlanteamserver.models.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Controller
@ServerEndpoint("/connect/{page}/{username}")
public class ConnexionWebSocketController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static AtomicInteger onlinePersons = new AtomicInteger(0);

    private static List<Player> listPlayer = new ArrayList<>();
    private static Map<String,Set> roomMap = new ConcurrentHashMap(8);
    private static Map<String, List<String>> userRoomMap = new HashMap<>();

    @OnMessage
    public void startGame(String message) throws IOException {
        if (message.contains("startGame")) {
            String roomId = message.replace("startGame", "");
            roomId = roomId.replace("/", "");
            Set<Session> sessions = roomMap.get(roomId);
            String textObstacle = "";
            textObstacle = textObstacle + "{\"type\": \"obstacles\",";

            List<Obstacle> obstacleList = new ArrayList<>();
            for (int i = 1; i <= 90; i++){
                Random ran = new Random();
                int x = ran.nextInt(50000) - 50000;
                Obstacle obstacle = new Obstacle(new Position(x, 0));
                obstacle.setDelayFall(Math.abs(obstacle.getPosition().getX()));
                obstacleList.add(obstacle);
                textObstacle = textObstacle + "\"" + i + "\": {\"positionX\":\"" + obstacle.getPosition().getX() + "\", \"positionY\": " + obstacle.getPosition().getY() + "}";
                if (i != 90){
                    textObstacle = textObstacle + ",";
                }
            }
            textObstacle = textObstacle + "}";
            for (Session s : sessions){
                s.getBasicRemote().sendText("GAME_STARTING");
                s.getBasicRemote().sendText(textObstacle.toString());

            }
          } else if (message.contains("moveFish")) {
            String[] messageSplit = message.split("/");
            String roomId = messageSplit[2];
            String username = messageSplit[3];
            String deltaY = messageSplit[4];

            Optional<Player> player = listPlayer.stream().filter(p -> p.getUsername().equals(username)).findFirst();
            player.get().moveY(Integer.valueOf(deltaY));
            Set<Session> sessions = roomMap.get(roomId);

            String text = "{\"username\": \"" + username + "\", \"deltaY\": " + player.get().getPosition().getY() + "}";
            for (Session s : sessions){
                s.getBasicRemote().sendText(text);
                if (player.get().getPosition().getX() <= -50000){
                    String textFinish = "{\"username\": \"" + username + "\", \"place\": " + listPlayer.stream().filter(p-> p.getRoom().equals(roomId) && p.getPosition().getX() <= -50000).count() + 1 + "}";
                    s.getBasicRemote().sendText(textFinish);
                }
            }


        } else if (message.contains("loopGame")){
            String roomId = message.replace("loopGame", "");
            roomId = roomId.replace("/", "");
            Set<Session> sessions = roomMap.get(roomId);
            String finalRoomId = roomId;
            List<Player> listPlayerInRoom = listPlayer.stream().filter(p -> p.getRoom().equals(finalRoomId)).collect(Collectors.toList());
            List<Player> listPlayerInRoomStillInGame = listPlayerInRoom.stream().filter(p -> p.getPosition().getX() > -50000).collect(Collectors.toList());
            String textPlayer = "{\"type\": \"player\",";
                for (Player player : listPlayerInRoomStillInGame){
                    player.moveX();
                    textPlayer = textPlayer + "\"" + listPlayerInRoomStillInGame.indexOf(player) + 1 + "\": {\"positionX\":\"" + player.getPosition().getX() + "\", \"positionY\": " + player.getPosition().getY() + "}";
                    if (!player.equals(listPlayerInRoomStillInGame.get(listPlayerInRoomStillInGame.size() -1))){
                        textPlayer = textPlayer + ",";
                    };
                }
                textPlayer = textPlayer + "}";
                for (Session s : sessions){
                    s.getBasicRemote().sendText(textPlayer);
                }
            }
        }

    @OnOpen
    @ResponseStatus(HttpStatus.OK)
    public void open(@PathParam("page") String roomId, @PathParam("username") String username, Session session) throws IOException, EncodeException {

        System.out.println("RoomId : " + roomId);
        System.out.println("RoomMap : " + roomMap);
        Set set = roomMap.get(roomId);
        System.out.println("Set : " + set);

        // If it's a new room, create a mapping, and if the room already exists, put the user in.
        if (set == null) {
            set = new CopyOnWriteArraySet();
            set.add(session);
            roomMap.put(roomId,set);
            userRoomMap.computeIfAbsent(roomId, k -> new ArrayList<String>()).add(username);
            System.out.println("RoomMap : " + roomMap);
        } else {
            System.out.println("set: " + set.toString());
            set.add(session);
            Set<Session> sessions = roomMap.get(roomId);
            userRoomMap.get(roomId).add(username);
            // Push messages to all users in the room
            for (Session s : sessions){
                Object users = userRoomMap.get(roomId);
                s.getBasicRemote().sendText(users.toString());
            }
        }
        // Number of rooms + 1
        onlinePersons.incrementAndGet();

        Player player = new Player(new Position(0,0), new ArrayList<Obstacle>());
        player.setRoom(roomId);
        player.setUsername(username);
        listPlayer.add(player);
    }

    @OnClose
    public void close(@PathParam("page") String page, Session session){
        // If a user leaves, the corresponding information is removed.
        if(roomMap.containsKey(page)){
            roomMap.get(page).remove(session);
        }
        // Number of rooms - 1
        onlinePersons.decrementAndGet();
        log.info("user{}Quit chatting,Number of rooms:{}",session.getId(),onlinePersons);
    }

    @OnError
    public void error(Throwable throwable){
        try {
            throw throwable;
        } catch (Throwable e) {
            log.error("unknown error");
        }
    }

    public static Map<String, Set> getRoomMap() {
        return roomMap;
    }

    public static void setRoomMap(Map<String, Set> roomMap) {
        ConnexionWebSocketController.roomMap = roomMap;
    }
}