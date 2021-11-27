package atlanteam.atlanteamserver.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

@RestController
public class ConnexionController {

    @Autowired
    ConnexionWebSocketController connexionWebSocketController;

    @GetMapping("/connect")
    @ResponseStatus(HttpStatus.OK)
    public String generateRoomId(){
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

    public ConnexionWebSocketController getConnexionWebSocketController() {
        return connexionWebSocketController;
    }

    public void setConnexionWebSocketController(ConnexionWebSocketController connexionWebSocketController) {
        this.connexionWebSocketController = connexionWebSocketController;
    }
}
