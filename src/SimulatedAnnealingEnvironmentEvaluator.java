
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import vacuumcleaner.base.*;

/**
 * Abschätzen der best möglichen Performance bei vollständigem Wissen zur
 * Evaluation eines Agenten durch Simulated Annealing
 */
public class SimulatedAnnealingEnvironmentEvaluator implements IEnvironmentEvaluator {

    private Double MAXTMP = 15d;
    private Double MINTMP = 10d;
    private Random random = new Random();
    private EnvironmentBase environment;
    private List<Node> visitedNodes;
    private int homeLimit;
    private List<Node> list;

    /**
     * Bestimmt ausgehend von der aktuellen Sitution des Agenten die 
     * maximal erreichbare Performance oder schätzt diese ab. Weiterhin wird noch
     * die Liste der dabei besuchten Dreckfelder gefüllt
     * @param environment Umgebung, zu der die bestmögliche Performance bestimmt 
     * werden soll
     * @return abgeschätzte oder bestimmte bestmögliche Performance
     */
    public int getOptimalPerformanceAtFullKnowledge(EnvironmentBase environment) {
        this.environment = environment;

        //1. Liste der besuchten Dreckfelder zurücksetzen
        visitedDirtPoints = new ArrayList<Point>();

        //2. die Startposition besuchen wir in jedem Fall
        visitedDirtPoints.add(environment.getAgentLocation());

        //3. die erreichbare Performance bestimmen
        // das ausschalten kostet in jedem Fall einen Punkt
        int reachablePerf = -1;

        //4. Am Ende ist noch die Entscheidung möglich, ob sich der Agent sofort ausschaltet
        //   (1000 Punkte Strafe), oder vorher zur Heimatposition fährt, wenn das weniger
        //   als diese 1000 Punkte kostet
        int wrongTurnoffFee = 1000;

        //5. letztendlich werden die Dreckpositionen zählen, diese machen die Größe des Problems aus
        //   für 2 potentielle Dreckpositionen kann jedoch schon im Voraus ein mögliches
        //   Vorgehen bestimmt werden - man muss natürlich aufpassen, dass diese "speziellen" Dreckpositionen
        //   später nicht doppelt gezählt werden:
        //   a) Dreck am Anfang wird immer aufgesaugt, das bringt netto 99 Punkte (100 Punkte Bonus - 1 Punkt Aktion Saugen)
        if (environment.containsDirt(environment.getAgentLocation().x, environment.getAgentLocation().y)) {
            reachablePerf += 99;
        }
        //   b) Dreck am Ende, der wird aufgesaugt, wenn der Agent zum Ende geht. D.h. es sind 99 Punkte mehr
        //      erreichbar, um diese erhöht sich aber auch die Strafe für falsches ausschalten
        //      -> das Ganze darf natürlich nur einmal gezählt werden, wenn die Start- und die Home-Position identisch sind
        if (!environment.getAgentLocation().equals(environment.getAgentHome()) &&
                environment.containsDirt(environment.getAgentHome().x, environment.getAgentHome().y)) {
            reachablePerf += 99;
            wrongTurnoffFee += 99;
        }

        //6. Umgebung in einen Graphen umwandeln (4 Zustände / Blickrichtungen) pro Raum
        list = new LinkedList<Node>();

        // fill the list with all dirt points
        for (Integer x = 0; x < environment.getWidth(); x++) {
            for (Integer y = 0; y < environment.getHeight(); y++) {
                if (environment.containsDirt(x, y)) {
                    for (Direction direction : Direction.values()) {
                        list.add(new Node(new Point(x, y), direction, environment, this));
                    }
                }
            }
        }
        // used for isHome()
        homeLimit = list.size() - 1;
        // homelocation must be at the end of the list
        for (Direction direction : Direction.values()) {
            list.add(new Node(environment.getAgentHome(), direction, environment, this));
        }

        // and the start
        Node start = new Node(environment.getAgentLocation(), environment.getAgentDirection(), environment, this);

        //    System.out.println(start);
        //    System.out.println(list);

        //7. Entfernungen in Aktionen bestimmen:
        //   a) von der AgentenPosition/Start-Blickrichtung
        //      ->(i) zu allen Dreckpositionen (ausser denen unter (5.) genannten) mit allen dabei möglichen Blickrichtungen
        //      ->(ii) zur nächsten der 4 möglichen Home-Positions-Blickrichtungen
        //   b) von jeder Dreckposition (ausser denen unter (5. genannten) mit allen dabei möglichen Blickrichtungen
        //      ->(i) zu allen anderen Dreckpositionen (ausser denen unter (5.) genannten) mit allen dabei möglichen Blickrichtungen
        //      ->(ii) zur nächsten der 4 möglichen Home-Positions-Blickrichtungen
        System.out.println(list.size() / 4 + " elements to search");
        // TODO: what if home is in unreachable ?
//                System.out.println(list);
//                System.out.println(list);
        //8. Ein Suchzustand ist nun eine Liste von durchfahrenen Dreckpositionen (ausser (5.)), jede Dreckposition wird maximal 1x in genau einer Blickrichtung durchfahren
        //   Diese Liste kann auch leer sein, dann wird keine Dreckposition angefahren.

        visitedNodes = SA(start, MAXTMP);

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

        return energy(start, visitedNodes);
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
    public ArrayList<Point> getVisitedPoints() {
        return visitedDirtPoints;
    }

    @Override
    public String toString() {
        //Anzeigename im Menü
        return "Abschätzung per Simulated Annealing";
    }

    private List<Node> SA(Node start, Double tmpMax) {
        List<Node> state, bestState;
        int energy, bestEnergy;

        state = makeRandomPlan(list);
//          state = makeSmartPlan(start);
        energy = energy(start, state);
        while(energy <= -10000){
            energy += 10000;
            System.out.println(state);
            state.remove(-energy);
            energy = energy(start, state);
        }

        bestState = state;
        bestEnergy = energy;

        Double tmp = tmpMax;


        while (true) {
      //      System.out.println(state);
            energy = energy(start, state);
            while(energy < -10000){
                energy += 10000;
                System.out.println(state);
                state.remove(-energy);
                energy = energy(start, state);
            }


            // save the best
            // (this is not exactly in the algorithm but an optimization
            if (energy > bestEnergy) {
                System.out.println("tmp " + tmp);
                //			System.out.println("state "+state);
                System.out.println("energy " + energy);

                bestEnergy = energy;
                bestState = state;
            }

            // terminate
            if (tmp <= MINTMP) {
                return bestState;
            }

            List<Node> next = neighbour(state);
            int nextEnergy = energy(start, next);
            while(nextEnergy <= -10000){
                nextEnergy += 10000;
                System.out.println(next);
                next.remove(-nextEnergy);
                nextEnergy = energy(start, state);
            }
            if (nextEnergy >= energy) {
                // always take it when it is better
                state = next;
            } else {
                // only take it if the temperatur allows it
                Double delta = (new Double(nextEnergy - energy));
//				System.out.println(chance);

                // calculate with e^tmp to get a nice curve
                Double temp = Math.pow(Math.E, tmp);

                Double chance = Math.pow(Math.E, delta / temp);
//				System.out.println("-> "+chance);

                Double probability = Math.random();
                //			System.out.println(probability+" "+chance);

                if (probability > chance) {
                    //			System.out.println("now ");
                    state = next;
                }


                //		System.out.println("probability "+chance+" e^ "+Math.pow(Math.E, chance));
            }
            tmp -= 0.00001;
        }
    }

    private int energy(Node start, List<Node> state) {
        int energy = 0;

        // do nothing ?
        if (state.isEmpty()) {
            // just turn off
            if(isHome(start)){
                energy = -1;
            } else {
                energy = -1001;
            }
        } else {
            // go through complete state
            int limit = state.size();

            // is the last one the home position ?
            if (isHome(state.get(state.size() - 1))) {
                // yes -> add moving cost to home
                Node node;
                // if the state only consists of the home position
                if (state.size() == 1) {
                    node = start;
                } else {
                    node = state.get(state.size() - 2);
                }
                // add meving cost to last position
                int distance = node.getDistance(state.get(state.size() - 1));
                //		System.out.println("HOME: from "+node+" to "+state.get(state.size() - 1)+" move "+distance+" fields");
                if(distance == -1){
                    System.out.println(state.get(state.size() - 1)+" not reachable");
                    return -(state.size() - 1) - 10000;
                }
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
            for (int i = 0; i < limit; i++) {
                // get the distance cost
                Node node;
                if (i == 0) {
                    node = start;
                } else {
                    node = state.get(i - 1);
                }
                int distance = node.getDistance(state.get(i));
                if(distance == -1)
                    return -i - 10000;
                //		System.out.println("MOVE: from "+node+" to "+state.get(i)+" move "+distance+" fields");
                // cost for moving
                energy -= distance;
                // price for cleaning
                energy += 99;
            }
        }
        return energy;
    }

    private boolean isHome(Node node) {
        return ((list.indexOf(node)) > homeLimit);
    }

    private List<Node> makeSmartPlan(Node start) {
        List<Node> result = new ArrayList<Node>();
        List<Node> copy = new ArrayList<Node>(list);

// delete the home
        for (int i = 0; i < 4; i++) {
            copy.remove(copy.size() - 1);
        }

 //       System.out.println("make plan");

        Node location = start;
        int limit = list.size() / 4 - 1;
        for (int i = 0; i < limit; i++) {
            Node nearest = null;
            Integer distance = null;
            for (Node node : copy) {
                int value = 0;
                value += Math.abs(location.getPoint().x - node.getPoint().x);
                value += Math.abs(location.getPoint().y - node.getPoint().y);

                if (((distance == null) || (value < distance)) && (!containsPoint(result, node))) {
                    nearest = node;
                    distance = value;
                }

            }

    //        System.out.println("add " + nearest);

            result.add(nearest);
            location = nearest;
    //        System.out.println("add " + nearest);
        }

// 50% chance to go to home at the end
        if (random.nextBoolean()) {
// randomly the direction
// the 4 nodes for the home are at the end of the list
            Node home = list.get(list.size() - random.nextInt(4) - 1);
            result.add(home);
        }

        return result;
    }

    private List<Node> makeRandomPlan(List<Node> list) {
        List<Node> points = new ArrayList<Node>();

        // 4 directions per point minus the home
        int countPoints = (list.size() / 4) - 1;
        System.out.println(list.size() + " " + countPoints);

        // choose randomly how many
        int number = 0;
        if(countPoints > 0){
            number = random.nextInt(countPoints) + 1;
        } else {
            // when there are no dirtpoints
            // -> just select the home position
            number = 0;
        }
        //	int number = countPoints;

        // fill the point list
        for (int i = 0; i < number; i++) {
            // get a random point minus the home
            int pick = random.nextInt(list.size() - 4);
            Node node = list.get(pick);

            // dont visit a point twice
            if (!containsPoint(points, node)) {
                points.add(node);
            } else {
                // try again
                i--;
            }
        }

        // 50% chance to go to home at the end
        if (random.nextBoolean()) {
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
    private boolean containsPoint(List<Node> state, Node point) {
        // check all nodes if the the point is in there
        for (Node node : state) {
            if (node.getPoint().equals(point.getPoint())) {
                return true;
            }
        }

        return false;
    }

    private ArrayList<Point> makeResult(Node start, List<Node> visitedNodes) {
        ArrayList<Point> result = new ArrayList<Point>();
        result.add(start.getPoint());
        for (Node node : visitedNodes) {
            result.add(node.getPoint());
        }
        return result;
    }

    /**
     * 5 ways:
     *	- turn direction of a node in the list
     *  - remove one thing from the list
     *  - add a thing to the list
     *  - switch 2 nodes
     *  - invert a sublist
     * @param state
     * @return
     */
    private List<Node> neighbour(List<Node> old) {
        List<Node> state = new ArrayList<Node>(old);

        int way = 0;
//	way = random.nextInt(5);
        int rand = random.nextInt(22);
        if (rand > 4) {
            way = 4;
        } else {
            way = rand;
        }
        if ((way == 0) && !state.isEmpty()) {
            //		System.out.println("= turn =");

            // turn direction
            // choose a node
            Integer index = random.nextInt(state.size());
            Integer number = list.indexOf(state.get(index));
            Integer position = number / 4;
            /* the 4 states
            System.out.println("index "+index+" number "+number+" position "+position);

            System.out.println(list.get(position * 4 + 0));
            System.out.println(list.get(position * 4 + 1));
            System.out.println(list.get(position * 4 + 2));
            System.out.println(list.get(position * 4 + 3));
             */
            Node newNode = list.get(position * 4 + random.nextInt(4));

            state.set(index, newNode);
        }
        if ((way == 1) && !state.isEmpty()) {
            // remove a thing
//			System.out.println("= remove =");

            int index = random.nextInt(state.size());
            state.remove(index);
//			System.out.println("removed "+index+" "+state.remove(index));
        }
        if (way == 2) {
            // add a thing
//			System.out.println("= add =");

            //check whether the state is at the maximum
            if ((state.size() * 4) == list.size()) {
                // cant add anything
            } else {
                // search something new
                Boolean isNew = false;
                Node node = null;
                while (!isNew) {
                    Integer index = random.nextInt(list.size());
                    node = list.get(index);
                    isNew = !containsPoint(state, node);
                }
                // insert the node somewhere
                Integer limit = state.size() + 1;
                if (isHome(node)) {
                    // if its the home position add it at the end
                    state.add(node);
                } else {
                    if (!state.isEmpty() && isHome(state.get(state.size() - 1))) {
                        // if there is already a home location dont put it at the end
                        limit -= 1;
                    }
                    //
                    Integer index = random.nextInt(limit);
                    List<Node> oldState = state;
                    // copy all nodes in a new state
                    state = new ArrayList<Node>();
                    for (Node oldNode : oldState) {
                        // add before this old node ?
                        if (state.size() == index) {
                            state.add(node);
                        }
                        state.add(oldNode);
                    }
                    // add at the end ?
                    if (state.size() == index) {
                        state.add(node);
                    }
                }
            }
        }
        if ((way == 3) && (state.size() >= 2)) {
            //		System.out.println("= swap =");
            Integer index1 = random.nextInt(state.size());
            Integer index2 = random.nextInt(state.size());
            // dont swap the home
            if (isHome(state.get(index1)) || isHome(state.get(index2))) {
                // do nothing
            } else {
                // swap
                Node node = state.get(index1);
                state.set(index1, state.get(index2));
                state.set(index2, node);
            }
        }
        if ((way == 4) && (state.size() > 2)) {
            // also invert the direction
            int from = random.nextInt(state.size() - 2);
//			System.out.println(state);
//			System.out.println("min from "+0);
//			System.out.println("max from "+(state.size()-2));
            int to = 0;
            if (isHome(state.get(state.size() - 1))) {
                to = random.nextInt(state.size() - from - 1) + from + 1;
//				System.out.println("min to "+(from + 1));
//				System.out.println("max to "+(state.size() - from - 1 + from + 1));
            } else {
                to = random.nextInt(state.size() - from - 1) + from + 2;
//				System.out.println("min to "+(from + 2));
//				System.out.println("max to "+(state.size() - from - 1 + from + 2));
            }
//			System.out.println("change from "+from+" to "+to);
            List<Node> sublist = state.subList(from, to);
            Collections.reverse(sublist);

            //          System.out.println(sublist);
            for (int i = 0; i < sublist.size(); i++) {
                Node node = sublist.get(i);
                Direction dir = node.turnRight(node.turnRight(node.getDirection()));
                Node com = new Node(node.getPoint(), dir, null, null);
                for (Node origin : list) {
                    if (com.equals(origin)) {
                        sublist.set(i, origin);
                        break;
                    }
                }
            }
            //            System.out.println("=> "+sublist);
        }

        return state;
    }

    void deletePoint(Point point) {
        List<Node> copy = new ArrayList<Node>(list);
        for(Node node : copy){
            // delete
            if(node.getPoint().equals(point)){
                list.remove(node);
                homeLimit--;
                System.out.println("removed "+node);
            }
        }
    }

}
