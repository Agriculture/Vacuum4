
import java.awt.Point;
import vacuumcleaner.base.Direction;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author s5677658
 */
class Node {
    private final Point point;
    private final Direction direction;

    public Node(Point point, Direction direction) {
        this.point = point;
        this.direction = direction;
    }

    @Override
    public String toString() {
        return "Node "+point.getX()+" "+point.getY()+" looking "+direction+"\n";
    }
}
