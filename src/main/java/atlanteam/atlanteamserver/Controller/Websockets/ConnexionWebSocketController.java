package atlanteam.atlanteamserver.Controller.Websockets;

import atlanteam.atlanteamserver.Service.MoveFishService;
import atlanteam.atlanteamserver.Service.StartGameService;
import atlanteam.atlanteamserver.models.Obstacle;
import atlanteam.atlanteamserver.models.Player;
import atlanteam.atlanteamserver.models.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    @Autowired
    StartGameService startGameService;

    @Autowired
    MoveFishService moveFishService;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public static AtomicInteger onlinePersons = new AtomicInteger(0);

    public static List<Player> listPlayer = new ArrayList<>();
    public static Map<String,Set> roomMap = new ConcurrentHashMap(8);
    public static Map<String, List<String>> userRoomMap = new HashMap<>();
    public static int countIterations = 0;
    public static ArrayList<Obstacle> obstacleList = new ArrayList<>();
    static public int NB_TRASH = 400;

    @OnMessage
    public void startGame(String message) throws IOException {

        if (message.contains("startGame")) {
            startGameService.initiateGame(message);
        } else if (message.contains("moveFish")) {
            moveFishService.moveFish(message);
        } else if (message.contains("loopGame")) {
            countIterations++;
            for (Obstacle obstacle : obstacleList) {
                if (countIterations > obstacle.getDelayFall() && obstacle.getPosition().getY() <= (600 - 600*55/800 - 65) && obstacle.getType().equals("trash")) {
                    obstacle.getPosition().setY(obstacle.getPosition().getY() + 1);
                } else if (obstacle.getType().equals("shark")){
                    obstacle.moveX();
                }
            }

            String roomId = message.replace("loopGame", "");
            roomId = roomId.replace("/", "");
            Set<Session> sessions = roomMap.get(roomId);
            String textObstacle = "";
            textObstacle = textObstacle + "{\"type\": \"obstacles\",";
            for (int i = 1; i <= NB_TRASH + 1; i++) {
                textObstacle = textObstacle + "\"" + i + "\": {\"positionX\":\"" + obstacleList.get(i - 1).getPosition().getX() + "\", \"positionY\": " + "\"" + obstacleList.get(i - 1).getPosition().getY() + "\", \"typeObstacle\": " + "\"" + obstacleList.get(i-1).getType() + "\"" + "}";
                if (i != NB_TRASH + 1) {
                    textObstacle = textObstacle + ",";
                }
            }

            textObstacle = textObstacle + "}";

            for (Session s : sessions) {
                s.getBasicRemote().sendText(textObstacle.toString());
            }
            String finalRoomId = roomId;
            List<Player> listPlayerInRoom = listPlayer.stream().filter(p -> p.getRoom().equals(finalRoomId)).collect(Collectors.toList());
            List<Player> listPlayerInRoomStillInGame = listPlayerInRoom.stream().filter(p -> p.getPosition().getX() <= 48000).collect(Collectors.toList());
            String textPlayer = "{\"type\": \"player\",";
            String textFinish = "{\"type\": \"finish\",";
            for (Player player : listPlayerInRoomStillInGame){
                player.moveX();
                textPlayer = textPlayer + "\"" + player.getUsername() + "\": {\"positionX\":\"" + player.getPosition().getX() + "\", \"positionY\": " + player.getPosition().getY() + "}";
                if (!player.equals(listPlayerInRoomStillInGame.get(listPlayerInRoomStillInGame.size() -1))){
                    textPlayer = textPlayer + ",";
                };
                if (player.getPosition().getX() >= 48000) {
                    textFinish = textFinish + "\"username\": \"" + player.getUsername() + "\", \"place\": " + (listPlayer.stream().filter(p -> p.getRoom().equals(finalRoomId) && p.getPosition().getX() >= 48000).count() + 1) + "}";
                    for (Session s : sessions){
                        s.getBasicRemote().sendText(textFinish);
                    }
                }
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

        Set set = roomMap.get(roomId);

        // If it's a new room, create a mapping, and if the room already exists, put the user in.
        if (set == null) {
            set = new CopyOnWriteArraySet();
            set.add(session);
            roomMap.put(roomId,set);
            userRoomMap.computeIfAbsent(roomId, k -> new ArrayList<String>()).add(username);
        } else {
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

        Player player = new Player(new Position(25,301), obstacleList);
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