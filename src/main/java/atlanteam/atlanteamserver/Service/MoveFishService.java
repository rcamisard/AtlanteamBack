package atlanteam.atlanteamserver.Service;

import atlanteam.atlanteamserver.Controller.Websockets.ConnexionWebSocketController;
import atlanteam.atlanteamserver.models.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Set;

@Service
public class MoveFishService {

    @Autowired
    StartGameService startGameService;

    public void moveFish(String message) throws IOException {
        //Parse du message
        String[] messageSplit = message.split("/");
        String roomId = messageSplit[2];
        String username = messageSplit[3];
        String deltaY = messageSplit[4];

        //On récupère le joueur qui a bougé et on met à jour ses données
        Player player = null;
        for (Player p : startGameService.listPlayer){
            if (p.getUsername().equals(username)){
                player = p;
            }
        }
        try {
            player.moveY(Integer.valueOf(deltaY));
        } catch (Error error) {
            System.out.printf(error.getMessage());
        }

        Set<Session> sessions = startGameService.roomMap.get(roomId);
        sendTextMoveFishToWS(sessions, username, deltaY);
    }

    public void sendTextMoveFishToWS(Set<Session> sessions, String username, String deltaY) throws IOException {
        String text = "{\"username\": \"" + username + "\", \"deltaY\": " + deltaY + "}";
        for (Session s : sessions) {
            s.getBasicRemote().sendText(text);
        }
    }
}
