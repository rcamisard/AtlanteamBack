package atlanteam.atlanteamserver.Controller.Websockets;

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

@Controller
@ServerEndpoint("/connect/{page}/{username}")
public class ConnexionWebSocketController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static AtomicInteger onlinePersons = new AtomicInteger(0);

    private static Map<String,Set> roomMap = new ConcurrentHashMap(8);
    private static Map<String, List<String>> userRoomMap = new HashMap<>();

    @OnMessage
    public void startGame(String message) throws IOException {
        if (message.contains("startGame")) {
            String roomId = message.replace("startGame", "");
            roomId = roomId.replace("/", "");
            Set<Session> sessions = roomMap.get(roomId);
            for (Session s : sessions){
                s.getBasicRemote().sendText("GAME_STARTING");
            }
        } else if (message.contains("moveFish")) {
            String roomId = message.replace("moveFish", "");
            roomId = roomId.replace("/", " ");
            Set<Session> sessions = roomMap.get(roomId);
        }
    }

    @OnOpen
    @ResponseStatus(HttpStatus.OK)
    public void open(@PathParam("page") String page, @PathParam("username") String username, Session session) throws IOException, EncodeException {

        System.out.println("RoomId : " + page);
        System.out.println("RoomMap : " + roomMap);
        Set set = roomMap.get(page);
        System.out.println("Set : " + set);

        // If it's a new room, create a mapping, and if the room already exists, put the user in.
        if (set == null) {
            set = new CopyOnWriteArraySet();
            set.add(session);
            roomMap.put(page,set);
            userRoomMap.computeIfAbsent(page, k -> new ArrayList<String>()).add(username);
            System.out.println("RoomMap : " + roomMap);
        } else {
            System.out.println("set: " + set.toString());
            set.add(session);
            Set<Session> sessions = roomMap.get(page);
            userRoomMap.get(page).add(username);
            // Push messages to all users in the room
            for (Session s : sessions){
                Object users = userRoomMap.get(page);
                s.getBasicRemote().sendText(users.toString());
            }
        }
        // Number of rooms + 1
        onlinePersons.incrementAndGet();
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