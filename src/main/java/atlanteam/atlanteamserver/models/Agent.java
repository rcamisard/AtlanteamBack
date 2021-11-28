package atlanteam.atlanteamserver.models;

import java.util.ArrayList;

public abstract class Agent {
    protected Position position;
    protected int speedY, height = 20, width = 20;
    protected double speedX;
    protected long lastTimeUpdated;

    public Agent(Position position){
        lastTimeUpdated = System.currentTimeMillis();
        this.speedX = 0.3;
        this.speedY = 10;
        this.position = position;
    }

    public abstract void moveX();

    public abstract void moveY(int value);

    public Position getPosition(){return position;}

}
