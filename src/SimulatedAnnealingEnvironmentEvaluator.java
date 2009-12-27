import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import vacuumcleaner.base.*;

/**
 * Abschätzen der best möglichen Performance bei vollständigem Wissen zur
 * Evaluation eines Agenten durch Simulated Annealing
 */
public class SimulatedAnnealingEnvironmentEvaluator implements IEnvironmentEvaluator
{

	private Random random = new Random();
	private EnvironmentBase environment;
	private ArrayList<Node> visitedNodes;
    /**
     * Bestimmt ausgehend von der aktuellen Sitution des Agenten die 
     * maximal erreichbare Performance oder schätzt diese ab. Weiterhin wird noch
     * die Liste der dabei besuchten Dreckfelder gefüllt
     * @param environment Umgebung, zu der die bestmögliche Performance bestimmt 
     * werden soll
     * @return abgeschätzte oder bestimmte bestmögliche Performance
     */
    public int getOptimalPerformanceAtFullKnowledge(EnvironmentBase environment){
		this.environment = environment;

        //1. Liste der besuchten Dreckfelder zurücksetzen
        visitedDirtPoints=new ArrayList<Point>();

        //2. die Startposition besuchen wir in jedem Fall
        visitedDirtPoints.add(environment.getAgentLocation());

        //3. die erreichbare Performance bestimmen
        // das ausschalten kostet in jedem Fall einen Punkt
        int reachablePerf=-1;

        //4. Am Ende ist noch die Entscheidung möglich, ob sich der Agent sofort ausschaltet
        //   (1000 Punkte Strafe), oder vorher zur Heimatposition fährt, wenn das weniger
        //   als diese 1000 Punkte kostet
        int wrongTurnoffFee=1000;

        //5. letztendlich werden die Dreckpositionen zählen, diese machen die Größe des Problems aus
        //   für 2 potentielle Dreckpositionen kann jedoch schon im Voraus ein mögliches
        //   Vorgehen bestimmt werden - man muss natürlich aufpassen, dass diese "speziellen" Dreckpositionen
        //   später nicht doppelt gezählt werden:
        //   a) Dreck am Anfang wird immer aufgesaugt, das bringt netto 99 Punkte (100 Punkte Bonus - 1 Punkt Aktion Saugen)
        if(environment.containsDirt(environment.getAgentLocation().x, environment.getAgentLocation().y))
            reachablePerf+=99;
        //   b) Dreck am Ende, der wird aufgesaugt, wenn der Agent zum Ende geht. D.h. es sind 99 Punkte mehr
        //      erreichbar, um diese erhöht sich aber auch die Strafe für falsches ausschalten
        //      -> das Ganze darf natürlich nur einmal gezählt werden, wenn die Start- und die Home-Position identisch sind
        if(!environment.getAgentLocation().equals(environment.getAgentHome()) &&
                environment.containsDirt(environment.getAgentHome().x,environment.getAgentHome().y))
        {
            reachablePerf+=99;
            wrongTurnoffFee+=99;
        }

        //6. Umgebung in einen Graphen umwandeln (4 Zustände / Blickrichtungen) pro Raum
        List<Node> list = new LinkedList<Node>();

		// fill the list with all dirt points
        for(Integer x=0; x<environment.getWidth(); x++){
            for(Integer y=0; y<environment.getHeight(); y++){
                if(environment.containsDirt(x, y)){
                    for(Direction direction : Direction.values()){
                        list.add(new Node(new Point(x, y), direction, environment));
                    }
                }
            }
        }
		// homelocation must be at the end of the list
        for(Direction direction : Direction.values()){
			list.add(new Node(environment.getAgentHome(), direction, environment));
		}

		// and the start
		Node start =  new Node(environment.getAgentLocation(), environment.getAgentDirection(), environment);

        System.out.println(start);
        System.out.println(list);

        //7. Entfernungen in Aktionen bestimmen:
        //   a) von der AgentenPosition/Start-Blickrichtung
        //      ->(i) zu allen Dreckpositionen (ausser denen unter (5.) genannten) mit allen dabei möglichen Blickrichtungen
        //      ->(ii) zur nächsten der 4 möglichen Home-Positions-Blickrichtungen
        //   b) von jeder Dreckposition (ausser denen unter (5. genannten) mit allen dabei möglichen Blickrichtungen
        //      ->(i) zu allen anderen Dreckpositionen (ausser denen unter (5.) genannten) mit allen dabei möglichen Blickrichtungen
        //      ->(ii) zur nächsten der 4 möglichen Home-Positions-Blickrichtungen
		start.setList(list);
		calcDistance(list);
		//8. Ein Suchzustand ist nun eine Liste von durchfahrenen Dreckpositionen (ausser (5.)), jede Dreckposition wird maximal 1x in genau einer Blickrichtung durchfahren
		//   Diese Liste kann auch leer sein, dann wird keine Dreckposition angefahren.

		System.out.println(" make a random plan");
		visitedNodes = makeRandomPlan(list);
		System.out.println(visitedNodes);
		System.out.println("energy "+energy(start, visitedNodes));

        //9. Die Performance (d.h. die zu maximierende Größe) bestimmt sich aus:
        //   ->  Basis-Performance (reachablePerf)
        //   ->+ 99*Anzahl der durchfahrenen Dreckpositionen
        //   ->- kumulierte dabei verbrauchte Navigationsaktionen (Drehen, GoForward)
        //   ->- Endgebühr (aus Entfernung der letzten durchfahrenen Dreckposition oder der Start-Position, falls kein Dreck angefahren wird: lastDirtHomeDist)
        //       a) lastDirtHomeDist>wrongTurnoffFee
        //          -> es kostet mehr dahin als wrongTurnoffFee (meist =1000), daher lieber die Strafe in Kauf nehmen und gleich abschalten
        //          -> Endgebühr=wrongTurnoffFee
        //          -> Home-Position nicht in die Liste der besuchten Felder aufnehmen
        //       b) lastDirtHomeDist<=wrongTurnoffFee
        //          -> Hinfahren lohnt sich
        //          -> Endgebühr=lastDirtHomeDist
        //          -> Home-Position in die Liste der besuchten Felder aufnehmen

		visitedDirtPoints = makeResult(start, visitedNodes);

        return -energy(start, visitedNodes);
    }

    /**
     * Liste, der interessanten Punkte bei der letzten Schätzung, d.h. die Startposition des Agentne,
     * Liste der gesäuberten Dreckfelder sowie evtl. die Home-Position
     */
    private ArrayList<Point> visitedDirtPoints;

    /**
     * Bestimmt nach einer Performancebestimmung die dabei besuchten Dreckfelder
     * @return Liste der besuchten Dreckfelder (und anderer "interessanter" Punkte)
     */
    @Override
    public ArrayList<Point> getVisitedPoints() 
    {
        return visitedDirtPoints;
    }

    @Override
    public String toString()
    {
        //Anzeigename im Menü
        return "Abschätzung per Simulated Annealing";
    }

	private void calcDistance(List<Node> list) {
		for(Node node : list){
			System.out.println("====> "+node);
			node.setList(list);
		}
	}

	private int energy(Node start, List<Node> list) {
		int energy = 0;

		// do nothing ?
		if(list.isEmpty()){
			// just turn off
			energy = 999;
		} else {
			// go through complete list
			int limit = list.size();

			// is the last one the home position ?
			if(environment.getAgentHome().equals(list.get(list.size() -1).getPoint())){
				// yes -> add moving cost to home
				Node node;
				// if the list only consists of the home position
				if(list.size() == 1){
					node = start;
				} else {
					node = list.get(list.size() - 2);
				}
				// add meving cost to last position
				int distance = node.getDistance(list.get(list.size() - 1));
				System.out.println("HOME: from "+node+" to "+list.get(list.size() - 1)+" move "+distance+" fields");
				energy -= distance;
				// turn off
				energy -= 1;
				// the last thing is no dirt point
				limit -= 1;
			} else {
				// punishment for not stopping at the home position
				energy -= 999;
			}
			
			// for all dirt points
			for(int i=0; i < limit; i++){
				// get the distance cost
				Node node;
				if(i == 0){
					node = start;
				} else {
					node = list.get(i - 1);
				}
				int distance = node.getDistance(list.get(i));
				System.out.println("MOVE: from "+node+" to "+list.get(i)+" move "+distance+" fields");
				// cost for moving
				energy -= distance;
				// price for cleaning
				energy += 99;
			}
		}
		// invert energy so we can minimize;
		return -energy;
	}

	private ArrayList<Node> makeRandomPlan(List<Node> list) {
		ArrayList<Node> points = new ArrayList<Node>();

		// 4 directions per point minus the home
		int countPoints = (list.size() / 4) - 1;

		// choose randomly how many
		int number = random.nextInt(countPoints) + 1;
		
		// fill the point list
		for(int i=0; i<number; i++){
			// get a random point minus the home
			int pick = random.nextInt(list.size() - 4);
			Node node = list.get(pick);

			// dont visit a point twice
			if(!containsPoint(points, node)){
				points.add(node);
			} else {
				// try again
				i--;
			}
		}

		// 50% chance to go to home at the end
		if(random.nextBoolean()){
			// randomly the direction
			// the 4 nodes for the home are at the end of the list
			Node home = list.get(list.size() - random.nextInt(4) - 1);
			points.add(home);
		}

		return points;
	}

	/**
	 *
	 *  helper for randomStart
	 * @param points
	 * @param node
	 * @return
	 */
	private boolean containsPoint(ArrayList<Node> list, Node point) {
		// check all nodes if the the point is in there
		for(Node node : list){
			if(node.getPoint().equals(point.getPoint())){
				return true;
			}
		}

		return false;
	}

	private ArrayList<Point> makeResult(Node start, ArrayList<Node> visitedNodes) {
		ArrayList<Point> result = new ArrayList<Point>();
		result.add(start.getPoint());
		for(Node node : visitedNodes){
			result.add(node.getPoint());
		}
		return result;
	}
}
