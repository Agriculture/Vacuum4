
import java.awt.Point;
import vacuumcleaner.base.Direction;
import vacuumcleaner.base.EnvironmentBase;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author konrad
 */
class SearchNode extends Node implements Comparable {
	private Node goal;
	private int depth;
	private int heuristic;

	public SearchNode(Point point, Direction direction, EnvironmentBase environment, Integer depth, Node goal) {
		super(point, direction, environment);
		this.depth = depth;
		this.goal = goal;
		heuristic = calcHeuristic();
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	int getValue(){
		return depth + heuristic;
	}

	private int calcHeuristic() {
		Integer value = 0;
		// manhattan distance
		value += goal.getPoint().getX() - this.;


		return value;
	}

}
