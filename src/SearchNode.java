
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

    private int depth;
    private int heuristic;

    public SearchNode(Point point, Direction direction, EnvironmentBase environment, Integer depth, Node goal, SimulatedAnnealingEnvironmentEvaluator sim) {
        super(point, direction, environment, sim, false);
        this.depth = depth;
        heuristic = calcHeuristic(goal);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    int getDepth() {
        return depth;
    }

    int getValue() {
        return depth + heuristic;
    }

    private int calcHeuristic(Node goal) {
        Integer value = 0;
// manhattan distance
        value += Math.abs(goal.getPoint().x - this.getPoint().x);
        value += Math.abs(goal.getPoint().y - this.getPoint().y);
		Direction direction = this.getDirection();
		if(direction == goal.getDirection()){
			value += 0;
		} else{
			if(((super.turnRight(direction) == goal.getDirection())
				|| super.turnLeft(direction) == goal.getDirection())){
				value += 1;
			} else {
				value += 2;
			}
		}

        return value;
    }

    public int compareTo(Object arg0) {
        if (getClass() != arg0.getClass()) {
            return 0;
        }
        SearchNode obj = (SearchNode) arg0;
        Integer value = depth + heuristic;
        return value.compareTo(obj.getValue());
        /* if(value < obj.getValue()){
        return 1;
        } else {
        if(value > obj.getValue()){
        return -1;
        } else {
        return 0;
        }
        }
         */ }

    @Override
    public String toString() {
        return super.toString() + " value " + (depth + heuristic);
    }
}
