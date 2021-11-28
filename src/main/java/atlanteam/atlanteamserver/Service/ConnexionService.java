package atlanteam.atlanteamserver.Service;

import atlanteam.atlanteamserver.models.Player;
import atlanteam.atlanteamserver.models.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.websocket.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
public class ConnexionService {

    @Autowired
    StartGameService startGameService;

    public String generateRoomId()
    {
        String randomCharset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        Map<String, Set> roomMap = startGameService.roomMap;
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

    public void openWS(String roomId, String username, Session session) throws IOException {
        Set set = startGameService.roomMap.get(roomId);

        // If it's a new room, create a mapping, and if the room already exists, put the user in.
        if (set == null) {
            set = new CopyOnWriteArraySet();
            set.add(session);
            startGameService.roomMap.put(roomId,set);
            startGameService.userRoomMap.computeIfAbsent(roomId, k -> new ArrayList<String>()).add(username);
        } else {
            set.add(session);
            Set<Session> sessions = startGameService.roomMap.get(roomId);
            startGameService.userRoomMap.get(roomId).add(username);
            // Push messages to all users in the room
            sendTextConnexionToWS(sessions, roomId);
        }

        Player player = new Player(new Position(25,301), startGameService.obstacleList);
        player.setRoom(roomId);
        player.setUsername(username);
        startGameService.listPlayer.add(player);
    }

    public void sendTextConnexionToWS(Set<Session> sessions, String roomId) throws IOException {
        for (Session s : sessions){
            Object users = startGameService.userRoomMap.get(roomId);
            s.getBasicRemote().sendText(users.toString());
        }
    }
}
