package atlanteam.atlanteamserver.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ServerEndpoint("/webSocket/{page}")
public class ConnexionWebSocketController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static AtomicInteger onlinePersons = new AtomicInteger(0);

    private static Map<String,Set> roomMap = new ConcurrentHashMap(8);

    @OnOpen
    public void open(@PathParam("page") String page, Session session) throws IOException {
        Set set = roomMap.get(page);
        // If it's a new room, create a mapping, and if the room already exists, put the user in.
        if(set == null){
            set = new CopyOnWriteArraySet();
            set.add(session);
            roomMap.put(page,set);
        }else{
            set.add(session);
        }
        // Number of rooms + 1
        onlinePersons.incrementAndGet();
        log.info("new user{}Enter the chat,Number of rooms:{}",session.getId(),onlinePersons);
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

    @OnMessage
    public void reveiveMessage(@PathParam("page") String page, Session session,String message) throws IOException {
        log.info("Accept Users{}Data:{}",session.getId(),message);
        // Stitching together user information
        String msg = session.getId()+" : "+ message;
        Set<Session> sessions = roomMap.get(page);
        // Push messages to all users in the room
        for(Session s : sessions){
            s.getBasicRemote().sendText(msg);
        }
    }

    @OnError
    public void error(Throwable throwable){
        try {
            throw throwable;
        } catch (Throwable e) {
            log.error("unknown error");
        }
    }
}