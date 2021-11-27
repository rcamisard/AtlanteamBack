package atlanteam.atlanteamserver.models;

import java.util.ArrayList;

public class Obstacle extends Agent {

    private float delayFall;
    public Obstacle(Position position) {
        super(position);
    }

    public void moveX(){
        long deltaTime = System.currentTimeMillis() - lastTimeUpdated;
        position.setX((int) (position.getX() + speedX * deltaTime));
        lastTimeUpdated = System.currentTimeMillis();
    }

    public float getDelayFall() {
        return delayFall;
    }

    public void setDelayFall(float delayFall) {
        this.delayFall = delayFall;
    }

    public void moveY(int value){position.setY(value + speedY + position.getY());}
}
