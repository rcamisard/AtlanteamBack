package atlanteam.atlanteamserver.Service;

import atlanteam.atlanteamserver.Controller.Websockets.ConnexionWebSocketController;
import atlanteam.atlanteamserver.models.Obstacle;
import atlanteam.atlanteamserver.models.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.websocket.Session;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class LoopGameService {

    @Autowired
    StartGameService startGameService;

    public void LoopGame(String message) throws IOException {
        final String roomId = message.replace("loopGame", "").replace("/", "");
        traitementObstacles(roomId);
        traitementPlayers(roomId);

    }

    public void traitementObstacles(String roomId) throws IOException {

        for (Obstacle obstacle : startGameService.obstacleList) {
            if (startGameService.countIterations > obstacle.getDelayFall() && obstacle.getPosition().getY() <= (600 - 600*55/800 - 65) && obstacle.getType().equals("trash")) {
                obstacle.getPosition().setY(obstacle.getPosition().getY() + 1);
            } else if (obstacle.getType().equals("shark")) {
                obstacle.moveX();
            }
        }
        Set<Session> sessions = startGameService.roomMap.get(roomId);
        sendTextObstacleToWS(sessions);
    }

    public void traitementPlayers(String roomId) throws IOException {
        List<Player> listPlayerInRoom = startGameService.listPlayer.stream().filter(p -> p.getRoom().equals(roomId)).collect(Collectors.toList());
        List<Player> listPlayerInRoomStillInGame = listPlayerInRoom.stream().filter(p -> p.getPosition().getX() <= 48000).collect(Collectors.toList());

        Set<Session> sessions = startGameService.roomMap.get(roomId);

        for (Player player : listPlayerInRoomStillInGame){
            player.moveX();
            }
        sendTextPlayersAndFinishToWS(sessions, listPlayerInRoomStillInGame, listPlayerInRoom);

        }

    private void sendTextPlayersAndFinishToWS(Set<Session> sessions, List<Player> listPlayerInRoomStillInGame, List<Player> listPlayerInRoom) throws IOException {
        String textPlayer = "{\"type\": \"player\",";
        String textFinish = "{\"type\": \"finish\",";

        for (Player player : listPlayerInRoomStillInGame){
            textPlayer = textPlayer + "\"" + player.getUsername() + "\": {\"positionX\":\"" + player.getPosition().getX() + "\", \"positionY\": " + player.getPosition().getY() + "}";
            if (!player.equals(listPlayerInRoomStillInGame.get(listPlayerInRoomStillInGame.size() -1))){
                textPlayer = textPlayer + ",";
            };
            if (player.getPosition().getX() >= 48000) {
                textFinish = textFinish + "\"username\": \"" + player.getUsername() + "\", \"place\": " + (listPlayerInRoomStillInGame.size() + 1) + "}";
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

    private void sendTextObstacleToWS(Set<Session> sessions) throws IOException {
        String textObstacle = "";
        textObstacle = textObstacle + "{\"type\": \"obstacles\",";

        for (Obstacle obstacle : startGameService.obstacleList){
            textObstacle = textObstacle + "\"" + startGameService.obstacleList.indexOf(obstacle) + "\": {\"positionX\":\"" + obstacle.getPosition().getX() + "\", \"positionY\": " + "\"" + obstacle.getPosition().getY() + "\", \"typeObstacle\": " + "\"" + obstacle.getType() + "\"" + "}";
            if (startGameService.obstacleList.indexOf(obstacle) != startGameService.NB_TRASH) {
                textObstacle = textObstacle + ",";
            }
        }
        textObstacle = textObstacle + "}";
        for (Session s : sessions) {
            s.getBasicRemote().sendText(textObstacle);

        }
    }

}
