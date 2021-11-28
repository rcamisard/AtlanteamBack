package atlanteam.atlanteamserver.models;

import java.util.ArrayList;

public class Obstacle extends Agent {

    private String type;
    private double delayFall;

    public Obstacle(Position position) {
        super(position);
    }

    public void moveX() {
        long deltaTime = System.currentTimeMillis() - lastTimeUpdated;
        position.setX((int) (position.getX() + speedX * deltaTime));
        lastTimeUpdated = System.currentTimeMillis();
    }

    public double getDelayFall() {
        return delayFall;
    }

    public void setDelayFall(double delayFall) {
        this.delayFall = delayFall;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void moveY(int value) {
        position.setY(value + speedY + position.getY());
    }


    public void setSpeedX(double speedX) {
        this.speedX = speedX;
    }
}
