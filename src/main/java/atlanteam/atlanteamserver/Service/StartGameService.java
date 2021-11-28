package atlanteam.atlanteamserver.Service;

import atlanteam.atlanteamserver.Controller.Websockets.ConnexionWebSocketController;
import atlanteam.atlanteamserver.models.Obstacle;
import atlanteam.atlanteamserver.models.Player;
import atlanteam.atlanteamserver.models.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.websocket.Session;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StartGameService {

    public static int countIterations = 0;
    public static List<Player> listPlayer = new ArrayList<>();
    public static Map<String,Set> roomMap = new ConcurrentHashMap(8);
    public static ArrayList<Obstacle> obstacleList = new ArrayList<>();
    public static Map<String, List<String>> userRoomMap = new HashMap<>();
    static public int NB_TRASH = 400;


    public void initiateGame(String message) throws IOException {
        countIterations = 0;
        String roomId = message.replace("startGame", "");
        roomId = roomId.replace("/", "");
        Set<Session> sessions = roomMap.get(roomId);

        initiateObstacles();
        sendTextObstacleToWS(sessions);

        for (Player player : listPlayer){
            player.resetLastTimeUpdated();
        }
        for (Obstacle obstacle : obstacleList){
            obstacle.resetLastTimeUpdated();
        }

        for (Session s : sessions) {
            s.getBasicRemote().sendText("GAME_STARTING");

        }
    }

    private void initiateObstacles() {
        for (int i = 1; i < NB_TRASH; i++) {
            Random ran = new Random();
            int x = ran.nextInt(50000);
            Obstacle obstacle = new Obstacle(new Position(x, 50));
            obstacle.setDelayFall(Math.floor(x * Math.random() / 3));
            obstacle.setType("trash");
            obstacleList.add(obstacle);

        }
        Obstacle shark = new Obstacle(new Position(40000, 450));
        shark.setType("shark");
        shark.setSpeedX(-0.5);
        obstacleList.add(shark);
    }

    private void sendTextObstacleToWS(Set<Session> sessions) throws IOException {
        String textObstacle = "";
        textObstacle = textObstacle + "{\"type\": \"obstacles\",";

        for (Obstacle obstacle : obstacleList){
            textObstacle = textObstacle + "\"" + obstacleList.indexOf(obstacle) + "\": {\"positionX\":\"" + obstacle.getPosition().getX() + "\", \"positionY\": " + "\"" + obstacle.getPosition().getY() + "\", \"typeObstacle\": " + "\"" + obstacle.getType() + "\"" + "}";
            if (obstacleList.indexOf(obstacle) != NB_TRASH) {
                textObstacle = textObstacle + ",";
            }
        }
        textObstacle = textObstacle + "}";
        for (Session s : sessions) {
            s.getBasicRemote().sendText(textObstacle);

        }
    }

}
