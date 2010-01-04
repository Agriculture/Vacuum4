
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
	private Integer depth;

	public SearchNode(Point point, Direction direction, EnvironmentBase environment, Integer depth) {
		super(point, direction, environment);
		this.depth = depth;
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

	public int compareTo(Object arg0) {
		if(getClass() != arg0.getClass())
			return 0;
		SearchNode obj = (SearchNode) arg0;
		return depth.compareTo(obj.getDepth());
		/*		if(value < obj.getValue()){
			return 1;
		} else {
			if(value > obj.getValue()){
				return -1;
			} else {
				return 0;
			}
		}
*/	}

	@Override
	public String toString() {
		return super.toString()+" depth "+depth;
	}

}
