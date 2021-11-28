package atlanteam.atlanteamserver.Controller.Websockets;

import atlanteam.atlanteamserver.Service.ConnexionService;
import atlanteam.atlanteamserver.Service.LoopGameService;
import atlanteam.atlanteamserver.Service.MoveFishService;
import atlanteam.atlanteamserver.Service.StartGameService;
import atlanteam.atlanteamserver.models.Obstacle;
import atlanteam.atlanteamserver.models.Player;
import atlanteam.atlanteamserver.models.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import java.util.stream.Collectors;

@Controller
@ServerEndpoint("/connect/{page}/{username}")
public class ConnexionWebSocketController {

    @Autowired
    StartGameService startGameService;

    @Autowired
    MoveFishService moveFishService;

    @Autowired
    LoopGameService loopGameService;

    @Autowired
    ConnexionService connexionService;

    @OnMessage
    public void startGame(String message) throws IOException {

        if (message.contains("startGame")) {
            startGameService.initiateGame(message);
        } else if (message.contains("moveFish")) {
            moveFishService.moveFish(message);
        } else if (message.contains("loopGame")) {
            startGameService.countIterations++;
            loopGameService.LoopGame(message);
        }
    }

    @OnOpen
    @ResponseStatus(HttpStatus.OK)
    public void open(@PathParam("page") String roomId, @PathParam("username") String username, Session session) throws IOException, EncodeException {
        connexionService.openWS(roomId, username, session);
    }

    @OnClose
    public void close(@PathParam("page") String page, Session session){
        if(startGameService.roomMap.containsKey(page)){
            startGameService.roomMap.get(page).remove(session);
        }
    }

    @OnError
    public void error(Throwable throwable){
        try {
            throw throwable;
        } catch (Throwable e) {
        }
    }
}