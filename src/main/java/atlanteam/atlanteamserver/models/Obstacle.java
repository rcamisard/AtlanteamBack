package atlanteam.atlanteamserver.models;

import java.util.ArrayList;

public class Obstacle extends Agent {

    public Obstacle(Position position) {
        super(position);
    }

    public void moveX(){
        long deltaTime = System.currentTimeMillis() - lastTimeUpdated;
        position.setX((int) (position.getX() + speedX * deltaTime));
        lastTimeUpdated = System.currentTimeMillis();
    }

    public void moveY(int value){position.setY(value + speedY + position.getY());}
}
