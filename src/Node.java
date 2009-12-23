
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
public class Node{
    private Point point;
    private Direction direction;
	private HashMap<Node, Integer> distance = new HashMap<Node, Integer>();
	private EnvironmentBase environment;

	public Node(Point point, Direction direction, EnvironmentBase environment) {
        this.point = point;
        this.direction = direction;
		this.environment = environment;
	}

    @Override
    public String toString() {
        return "Node "+point.getX()+" "+point.getY()+" looking "+direction+"\n";
    }

	public void setList(List<Node> list) {
		for(Node node : list){
			Integer dist = search(node);
			System.out.println("Distance from "+this.toString()+" to "+node+" is "+dist);
			distance.put(node, dist);
		}
	}

	Direction getDirection(){
		return direction;
	}


	private int search(Node goal) {
		Integer value = 99999;
		PriorityQueue<SearchNode> queue = new PriorityQueue<SearchNode>();
		SearchNode root = new SearchNode(this.point, this.direction, this.environment, 0, goal);
		queue.add(root);

		HashMap<SearchNode, Integer> visitedNodes = new HashMap<SearchNode, Integer>();
		visitedNodes.put(root, 0);

		while(!queue.isEmpty()){
			SearchNode node = queue.poll();

			if(node.equals(goal)){
				return node.getDepth();
			}

			/**** expand ****/
			// turn left
			SearchNode child = new SearchNode(node.getPoint(), turnLeft(node.getDirection()), this.environment,
					node.getDepth() + 1, goal);
			if(!visitedNodes.containsKey(child)){
				queue.add(child);
			}
			// turn right
			child = new SearchNode(node.getPoint(), turnRight(node.getDirection()), this.environment,
					node.getDepth() + 1, goal);
			if(!visitedNodes.containsKey(child)){
				queue.add(child);
			}
			// move
			child = new SearchNode(move(node.getPoint(), node.getDirection()), node.getDirection(), this.environment,
					node.getDepth() + 1, goal);
			if(environment.isRoom(child.getPoint().x, child.getPoint().y)
					&& !visitedNodes.containsKey(child)){
				queue.add(child);
			}
		}
		return value;
	}

	public Point getPoint(){
		return point;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Node other = (Node) obj;
		if (this.point != other.point && (this.point == null || !this.point.equals(other.point))) {
			return false;
		}
		if (this.direction != other.direction) {
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

	private Direction turnLeft(Direction direction) {
		switch(direction){
			case Up:	return Direction.Left;
			case Left:	return Direction.Down;
			case Down:	return Direction.Right;
			case Right:	return Direction.Up;
			default:	return null;
		}
	}

	private Direction turnRight(Direction direction) {
		switch(direction){
			case Up:	return Direction.Right;
			case Left:	return Direction.Up;
			case Down:	return Direction.Left;
			case Right:	return Direction.Down;
			default:	return null;
		}
	}

	private Point move(Point point, Direction direction) {
		switch(direction){
			case Up:	return new Point(point.x, point.y - 1);
			case Left:	return new Point(point.x - 1, point.y);
			case Down:	return new Point(point.x, point.y + 1);
			case Right:	return new Point(point.x + 1, point.y);
			default:	return null;
		}
	}

}
