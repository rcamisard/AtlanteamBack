package atlanteam.atlanteamserver.Service.Connexion;

import atlanteam.atlanteamserver.Controller.Websockets.ConnexionWebSocketController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.Set;

@Service
public class ConnexionService {

    @Autowired
    ConnexionWebSocketController connexionWebSocketController;

    public String generateRoomId()
    {
        String randomCharset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Map<String, Set> roomMap = connexionWebSocketController.getRoomMap();
        String roomId = "";
        if (!roomMap.isEmpty()) {
            while (roomMap.get("page").contains(roomId) && roomId.equals("")) {
                Random r = new Random();
                roomId = "";
                for (int i = 0; i < 5; i++) {
                    roomId += randomCharset.charAt(r.nextInt(randomCharset.length()));
                }
            }
        } else {
            Random r = new Random();
            roomId = "";
            for (int i = 0; i < 5; i++) {
                roomId += randomCharset.charAt(r.nextInt(randomCharset.length()));
            }
        }
        return roomId;
    }
}
