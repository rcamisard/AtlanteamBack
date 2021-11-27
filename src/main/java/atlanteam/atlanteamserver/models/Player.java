package atlanteam.atlanteamserver.models;

import java.util.ArrayList;

public class Player extends Agent {

    ArrayList<Obstacle> obstacles;

    public Player(Position position, ArrayList<Obstacle> obstacles) {
        super(position);
        this.obstacles = obstacles;
    }

    public void moveX(){
        if(!didHitObstacle()){
            long deltaTime = System.currentTimeMillis() - lastTimeUpdated;
            position.setX((int) (position.getX() + speedX * deltaTime));
        }
        lastTimeUpdated = System.currentTimeMillis();
    }

    public void moveY(int value){position.setY(value + speedY + position.getY());}

    public boolean didHitObstacle(){
        for(Obstacle obstacle : obstacles){

            if(Math.abs(obstacle.getPosition().getX() + obstacle.width/2 - (position.getX()-width/2)) <= obstacle.width + width
                    && Math.abs(obstacle.getPosition().getX() - obstacle.width/2 - (position.getX()+width/2)) <= obstacle.width + width
                    && Math.abs(obstacle.getPosition().getY() + obstacle.height/2 - (position.getY()-height/2)) <= obstacle.height + height
                    && Math.abs(obstacle.getPosition().getY() - obstacle.height/2 - (position.getY()+height/2)) <= obstacle.height + height)
                return true;
        }
        return false;
    }
}
