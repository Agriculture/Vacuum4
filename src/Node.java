
import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import vacuumcleaner.base.Direction;
import vacuumcleaner.base.EnvironmentBase;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author s5677658
 */
public class Node {

    private Point point;
    private Direction direction;
    private HashMap<Node, Integer> distance = new HashMap<Node, Integer>();
    private EnvironmentBase environment;
    private final SimulatedAnnealingEnvironmentEvaluator sim;
    private Boolean home;

    public Node(Point point, Direction direction, EnvironmentBase environment, SimulatedAnnealingEnvironmentEvaluator sim, Boolean home) {
        this.point = point;
        this.direction = direction;
        this.environment = environment;
        this.sim = sim;
        this.home = home;

    }

    @Override
    public String toString() {
        String dir = "";
        switch (direction) {
            case Up:
                dir = "\u2191";
                break;
            case Down:
                dir = "\u2193";
                break;
            case Left:
                dir = "\u2190";
                break;
            case Right:
                dir = "\u2192";
                break;
            default:
                dir = "?";
                break;
        }
        return "Node (" + point.x + ", " + point.y + ", " + dir + ")";
    }

    Direction getDirection() {
        return direction;
    }

    public Boolean isHome(){
        return home;
    }

    private int search(Node goal) {
        int count = 0;
		// make a new goal with homeLocation == false
		// -> all SearchNodes have this at false -> doesnt make a difference for finding the goal
		goal = new Node(goal.getPoint(), goal.getDirection(), environment, sim, false);
        PriorityQueue<SearchNode> queue = new PriorityQueue<SearchNode>();
        SearchNode root = new SearchNode(this.point, this.direction, this.environment, 0, goal, sim);
        queue.add(root);

        HashMap<SearchNode, Integer> visitedNodes = new HashMap<SearchNode, Integer>();
        visitedNodes.put(root, 0);

        while (!queue.isEmpty()) {
            SearchNode node = queue.poll();
// System.out.println("expand "+node);
// System.out.println("size of queue "+queue.size());
// System.out.println("queue "+queue);

            if (node.equals(goal)) {
                return node.getDepth();
            }

//			if(sim.getList().contains(node)){
//				if(node.hasDistance(goal)){
//					return node.getDepth()+node.getDistance(goal);
//				}
//			}

//            if (count > 5000) {
//                sim.deletePoint(goal.getPoint());
//				return -1;
//            }

            visitedNodes.put(node, node.getDepth());

            /**** expand ****/
// turn left
            SearchNode child = new SearchNode(node.getPoint(), turnLeft(node.getDirection()), this.environment,
                    node.getDepth() + 1, goal, sim);
            if (!visitedNodes.containsKey(child)) {
                queue.add(child);
            }
// System.out.println("WWWWWWWW"+child);
// turn right
            child = new SearchNode(node.getPoint(), turnRight(node.getDirection()), this.environment,
                    node.getDepth() + 1, goal, sim);
// System.out.println("WWWWWWWW"+child);
            if (!visitedNodes.containsKey(child)) {
                queue.add(child);
            }
// move
            child = new SearchNode(move(node.getPoint(), node.getDirection()), node.getDirection(), this.environment,
                    node.getDepth() + 1, goal, sim);
// System.out.println("WWWWWWWW"+child);
            if ((child.getPoint().x >= 0) && (child.getPoint().y >= 0) && (child.getPoint().x < environment.getWidth()) && (child.getPoint().y < environment.getHeight()) && environment.isRoom(child.getPoint().x, child.getPoint().y) && !visitedNodes.containsKey(child)) {
                queue.add(child);
            }
            count++;

        }
// not reachable
        sim.deletePoint(goal.getPoint());
        return -1;
    }

    public Point getPoint() {
        return point;
    }

    public int getDistance(Node node) {
        if (!distance.containsKey(node)) {
            int value = search(node);
            if(value != -1){
                distance.put(node, search(node));
            } else {
                return -1;
            }
        }
        return distance.get(node);
    }

	public boolean hasDistance(Node node){
		return distance.containsKey(node);
	}

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        /* if (getClass() != obj.getClass()) {
        return false;
        }
        -*/ final Node other = (Node) obj;
        if (this.point != other.point && (this.point == null || !this.point.equals(other.point))) {
            return false;
        }
        if (this.direction != other.direction) {
            return false;
        }
        if (this.isHome() != other.isHome()){
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.point != null ? this.point.hashCode() : 0);
        hash = 97 * hash + (this.direction != null ? this.direction.hashCode() : 0);
        return hash;
    }

    public Direction turnLeft(Direction direction) {
        switch (direction) {
            case Up:
                return Direction.Left;
            case Left:
                return Direction.Down;
            case Down:
                return Direction.Right;
            case Right:
                return Direction.Up;
            default:
                return null;
        }
    }

    public Direction turnRight(Direction direction) {
        switch (direction) {
            case Up:
                return Direction.Right;
            case Left:
                return Direction.Up;
            case Down:
                return Direction.Left;
            case Right:
                return Direction.Down;
            default:
                return null;
        }
    }

    private Point move(Point point, Direction direction) {
        switch (direction) {
            case Up:
                return new Point(point.x, point.y - 1);
            case Left:
                return new Point(point.x - 1, point.y);
            case Down:
                return new Point(point.x, point.y + 1);
            case Right:
                return new Point(point.x + 1, point.y);
            default:
                return null;
        }
    }
}

