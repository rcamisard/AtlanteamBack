package atlanteam.atlanteamserver.Controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@ServerEndpoint("/play/{page}")
public class PlayWebSocketController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static AtomicInteger onlinePersons = new AtomicInteger(0);

    private static Map<String, Set> roomMap = new ConcurrentHashMap(8);

    @OnOpen
    public void open(@PathParam("page") String page, Session session) throws IOException {
    Map<String, Set> roomMap = ConnexionWebSocketController.getRoomMap();
    Set room = roomMap.get(page);
    }
}