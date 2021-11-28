package atlanteam.atlanteamserver.Service;

import atlanteam.atlanteamserver.Controller.Websockets.ConnexionWebSocketController;
import atlanteam.atlanteamserver.models.Obstacle;
import atlanteam.atlanteamserver.models.Player;
import atlanteam.atlanteamserver.models.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.websocket.Session;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@Service
public class StartGameService {


    @Autowired
    ConnexionWebSocketController connexionWebSocketController;

    public void initiateGame(String message) throws IOException {
        connexionWebSocketController.countIterations = 0;
        String roomId = message.replace("startGame", "");
        roomId = roomId.replace("/", "");
        Set<Session> sessions = connexionWebSocketController.roomMap.get(roomId);

        initiateObstacles();
        sendTextObstacleToWS(sessions);

        for (Player player : connexionWebSocketController.listPlayer){
            player.resetLastTimeUpdated();
        }
        for (Obstacle obstacle : connexionWebSocketController.obstacleList){
            obstacle.resetLastTimeUpdated();
        }

        for (Session s : sessions) {
            s.getBasicRemote().sendText("GAME_STARTING");

        }
    }

    private void initiateObstacles() {
        for (int i = 1; i < connexionWebSocketController.NB_TRASH; i++) {
            Random ran = new Random();
            int x = ran.nextInt(50000);
            Obstacle obstacle = new Obstacle(new Position(x, 50));
            obstacle.setDelayFall(Math.floor(x * Math.random() / 3));
            obstacle.setType("trash");
            connexionWebSocketController.obstacleList.add(obstacle);

        }
        Obstacle shark = new Obstacle(new Position(40000, 450));
        shark.setType("shark");
        shark.setSpeedX(-0.5);
        connexionWebSocketController.obstacleList.add(shark);
    }

    private void sendTextObstacleToWS(Set<Session> sessions) throws IOException {
        String textObstacle = "";
        textObstacle = textObstacle + "{\"type\": \"obstacles\",";

        for (Obstacle obstacle : connexionWebSocketController.obstacleList){
            textObstacle = textObstacle + "\"" + connexionWebSocketController.obstacleList.indexOf(obstacle) + "\": {\"positionX\":\"" + obstacle.getPosition().getX() + "\", \"positionY\": " + "\"" + obstacle.getPosition().getY() + "\", \"typeObstacle\": " + "\"" + obstacle.getType() + "\"" + "}";
            if (connexionWebSocketController.obstacleList.indexOf(obstacle) != connexionWebSocketController.NB_TRASH) {
                textObstacle = textObstacle + ",";
            }
        }
        textObstacle = textObstacle + "}";
        for (Session s : sessions) {
            s.getBasicRemote().sendText(textObstacle);

        }
    }

}
