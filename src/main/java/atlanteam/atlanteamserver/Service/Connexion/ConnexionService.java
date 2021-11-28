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
        System.out.println("a: " + connexionWebSocketController);
        Map<String, Set> roomMap = connexionWebSocketController.getRoomMap();
        System.out.println("b: " + roomMap);
        String roomId = "";
        if (!roomMap.isEmpty()) {
            do {
                Random r = new Random();
                roomId = "";
                for (int i = 0; i < 5; i++) {
                    roomId += randomCharset.charAt(r.nextInt(randomCharset.length()));
                }
            } while (roomMap.containsKey(roomId));
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
