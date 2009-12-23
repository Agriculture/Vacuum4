
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
class Node{
    private final Point point;
    private final Direction direction;
	private HashMap<Node, Integer> distance = new HashMap<Node, Integer>();
	private EnvironmentBase environment;

	Node(Point point, Direction direction, EnvironmentBase environment) {
        this.point = point;
        this.direction = direction;
		this.environment = environment;
	}

    @Override
    public String toString() {
        return "Node "+point.getX()+" "+point.getY()+" looking "+direction+"\n";
    }

	void setList(List<Node> list) {
		for(Node node : list){
			search(node);
		}
	}

	private void search(Node node) {
		PriorityQueue<SearchNode> queue = new PriorityQueue<SearchNode>();
		SearchNode root = new SearchNode(this.point, this.direction, this.environment, 0, node);
	}

	public Point getPoint(){
		return point;
	}

	public int compareTo(Object arg0) {
		throw new UnsupportedOperationException("Not supported yet.");
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

}
