/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package src;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import vacuumcleaner.base.Action;
import vacuumcleaner.base.AgentBase;
import vacuumcleaner.base.Direction;

/**
 *
 * @author konrad
 */
public class ObstacleAgent extends AgentBase {
	//information about input
	private boolean collision;
	private boolean dirt;
	private boolean home;

	//information about agent
	private Action lastAction;
	private Direction direction;
	private Point position;
	private List<Point> way;
	private List<Point> obstacles;
	private List<Point> toVisit;

	//information about world
	private Point homeLocation;
	private NextField nextField;
	private boolean homeVisited;

	public ObstacleAgent(){
		super("ObstacleAgent");
		//just init it, doenst matter with which direction
		direction = Direction.Down;
		way = new ArrayList<Point>();
		obstacles = new ArrayList<Point>();
		toVisit = new ArrayList<Point>();
		nextField = new NextField();
		position = new Point(100, 100);
		way.add(position);
		toVisit.add(new Point(99, 100));
		toVisit.add(new Point(101, 100));
		toVisit.add(new Point(100, 99));
		toVisit.add(new Point(100, 101));
		homeVisited = false;
	}

	@Override
	public Action nextAction(boolean collision, boolean dirt, boolean home) {
		Action action = null;

		//make them global, im too lazy to push them through all functions
		this.collision = collision;
		this.dirt = dirt;
		this.home = home;

		//where am i ?
		calcPosition();

		action = nextField.getAction();

		lastAction = action;
		return action;
	}

	private class NextField {
		private NextGoal nextGoal;
		private Point goal;

		public NextField(){
			nextGoal = new NextGoal();
			goal = null;
		}

		public Action getAction(){
			Action action = null;
			if(dirt){
				action = Action.SuckUpDirt;
			} else {
				if(goal == null){
					//get new goal
					goal = nextGoal.getGoal();
					if(goal == null){
						action = Action.TurnOff;
					} else {
						System.err.println("next goal is: "+goal);
						action = getAction();
					}
				} else {
					if(position.equals(goal) || collision){
						collision = false;
						//reached goal or punched a wall -> so get new one
						goal = null;
						action = getAction();
					} else {
						//go towards goal
						if(goal.x < position.x) {
							if(direction == Direction.Left) {
								//its in front of me
								action = Action.GoForeward;
							} else {
								//turn left or right ?
								if(direction == Direction.Down){
									action = Action.TurnRight;
								} else {
									action = Action.TurnLeft;
								}
							}
						} else {
							if(goal.x > position.x) {
								if(direction == Direction.Right){
									action = Action.GoForeward;
								} else {
									//turn left or right
									if(direction == Direction.Up){
										action = Action.TurnRight;
									} else {
										action = Action.TurnLeft;
									}
								}
							} else {
								if(goal.y < position.y){
									if(direction == Direction.Up){
										action = Action.GoForeward;
									} else {
										if(direction == Direction.Left) {
											action = Action.TurnRight;
										} else {
											action = Action.TurnLeft;
										}
									}
								} else {
									if(goal.y > position.y){
										if(direction == Direction.Down){
											action= Action.GoForeward;
										} else {
											if(direction == Direction.Right){
												action = Action.TurnRight;
											} else {
												action = Action.TurnLeft;
											}
										}
									}
								}
							}
						}
					}
				}
			}
			return action;
		}
	}

	private class NextGoal{
		private List<Point> wayToGo;
		Dijkstra dijkstra;

		public NextGoal(){
			wayToGo = new ArrayList<Point>();
		}

		private Point getGoal() {
			Point p = null;

			if(wayToGo.isEmpty()){
				//calculate new way use getLongTermGoal
				Point longTermGoal = getNewLongTermGoal();
				if(longTermGoal == null){
					//turn off !
					p = null;
				} else {
					dijkstra = new Dijkstra(longTermGoal);
					wayToGo = dijkstra.calcWay();
					if(!wayToGo.isEmpty()){
						p = wayToGo.remove(wayToGo.size()-1);
					}
				}
			} else {
				p = wayToGo.remove(wayToGo.size()-1);
			}

			return p;
		}

		private Point getNewLongTermGoal(){
			Point point = null;
			if(toVisit.isEmpty()) {
				if(!homeVisited && (homeLocation!=null)){
					point = homeLocation;
					homeVisited = true;
					//go home
				}
			} else {
				//return the last one in list
				point = (Point) toVisit.remove(toVisit.size()-1);
			}
			return point;
		}
	}

	private class Dijkstra{
		private List<Point> calcWay;
		private List<Node> borderList;
		private List<Node> closedList;
		private Point longTermGoal;

		public Dijkstra(Point longTermGoal){
			this.longTermGoal = longTermGoal;
			borderList = new ArrayList<Node>();
			closedList = new ArrayList<Node>();
		}

		public List<Point> calcWay() {
			calcWay = new ArrayList<Point>();
			Node n = new Node(position);
			n.setLength(0);
			addClosedList(n);

			while(!n.getPoint().equals(longTermGoal)){
				n = Collections.min(borderList);
				addClosedList(n);
			}
			calcWay.add(new Point(longTermGoal));
			System.err.println("n in dijkstra:"+n);
			while((n.pred != null) && (n.pred.getLength() != 0)){
				n = closedList.remove(closedList.indexOf(n.pred));
				calcWay.add(new Point(n.getPoint()));
			}
			return calcWay;
		}

		private void addClosedList(Node n){
			borderList.remove(n);
			Point p = n.getPoint();
			List<Node> maybeBorder = new ArrayList<Node>();

			maybeBorder.add(new Node(new Point(p.x+1, p.y)));
			maybeBorder.add(new Node(new Point(p.x-1, p.y)));
			maybeBorder.add(new Node(new Point(p.x, p.y+1)));
			maybeBorder.add(new Node(new Point(p.x, p.y-1)));
			for(Node n1 : maybeBorder){
				if(((way.contains(n1.getPoint())) && !(borderList.contains(n1))
						&& !closedList.contains(n1)) || (n1.getPoint().equals(longTermGoal))){
					n1.setpred(n);
					n1.setLength(n.getLength()+1);
					borderList.add(n1);
				}
			}
			closedList.add(n);
		}

	}

	private class Node implements Comparable{
		Point p;
		int length;
		Node pred;

		public Node(Point p){
			this.p = p;
			length = 0;
			pred = null;
		}
		public void setLength(int l){
			length = l;
		}
		public void setpred(Node n){
			pred = n;
		}
		public int getLength(){
			return length;
		}
		public Node getPred(){
			return pred;
		}
		public Point getPoint(){
			return p;
		}

		public int compareTo(Object o) {
			Node n = (Node)o;
			if(n.length == this.length){
				return 0;
			} else {
				if(this.length < n.length){
					return -1;
				} else {
					return 1;
				}
			}
		}
		@Override
		public boolean equals(Object obj) {
			Node n = (Node) obj;
			return p.equals(n.getPoint());
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 37 * hash + (this.p != null ? this.p.hashCode() : 0);
			return hash;
		}
		public String toString(){
			return this.p.toString();
		}
	}

	/**
	 * to calculate the current position and save it
	 * will be needed to find location of home at the end !
	 */
	private void calcPosition() {
		if  (lastAction == Action.GoForeward){
			Point target = new Point(position);
			switch (direction){
				case Right:
					target.translate(1, 0); break;
				case Left:
					target.translate(-1, 0); break;
				case Up:
					target.translate(0, -1); break;
				case Down:
					target.translate(0, 1); break;
			}
			if(collision==true){
				//found obstacle
				obstacles.add(target);
				toVisit.remove(target);
			} else {
				//agent has moved -> new pos
				position = target;
				//save the position
				if(!way.contains(position)){
					way.add(new Point(position));
				} else {
					//nothing
				}
				//add new points to visit
				List<Point> visit = new ArrayList<Point>();
				int[] modify = null;
				switch(direction){
					case Up:	modify = new int[]{-1, 0, 1, 0, 0, -1}; break;
					case Down:	modify = new int[]{1, 0, -1, 0, 0, 1}; break;
					case Left:	modify = new int[]{0, 1, 0, -1, -1, 0}; break;
					case Right:	modify = new int[]{0, -1, 0, 1, 1, 0}; break;
				}
				for(int i = 0; i<=2; i++){
					visit.add(new Point(position.x + modify[2*i], position.y+modify[2*i+1]));
				}
				for(Point p : visit){
					if(way.contains(p) || obstacles.contains(p) || toVisit.contains(p)){
						//throw away
					} else {
						toVisit.add(p);
					}
				}
			}
		} else if(lastAction == Action.TurnRight) {
				//agent turned -> new direction
				switch (direction){
					case Right:
						direction = Direction.Down; break;
					case Left:
						direction = Direction.Up; break;
					case Up:
						direction = Direction.Right; break;
					case Down:
						direction = Direction.Left; break;
				}
			} else if(lastAction == Action.TurnLeft) {
				//agent turned -> new direction
				switch (direction){
					case Right:
						direction = Direction.Up; break;
					case Left:
						direction = Direction.Down; break;
					case Up:
						direction = Direction.Left; break;
					case Down:
						direction = Direction.Right; break;
				}

			}
		//!must be at the end
		//if we found home -> save it
		if(home) {
			this.homeLocation = new Point(position);
		}
		System.err.println("Position: "+position+"Dir: "+direction+" home: "+homeLocation);
		System.err.println("last action"+lastAction);
	}

}
