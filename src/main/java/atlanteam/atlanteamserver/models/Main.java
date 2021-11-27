package atlanteam.atlanteamserver.models;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        ArrayList<Obstacle> obstacles = new ArrayList<>();
        Obstacle obstacle = new Obstacle(new Position(10, 10));

        obstacles.add(obstacle);

        Player player = new Player(new Position(0, 30), obstacles);

        System.out.println(player.didHitObstacle());
    }
}
