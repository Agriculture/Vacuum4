import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import vacuumcleaner.base.*;

/**
 * Abschätzen der best möglichen Performance bei vollständigem Wissen zur
 * Evaluation eines Agenten durch Simulated Annealing
 */
public class SimulatedAnnealingEnvironmentEvaluator implements IEnvironmentEvaluator
{
    /**
     * Bestimmt ausgehend von der aktuellen Sitution des Agenten die 
     * maximal erreichbare Performance oder schätzt diese ab. Weiterhin wird noch
     * die Liste der dabei besuchten Dreckfelder gefüllt
     * @param environment Umgebung, zu der die bestmögliche Performance bestimmt 
     * werden soll
     * @return abgeschätzte oder bestimmte bestmögliche Performance
     */
    public int getOptimalPerformanceAtFullKnowledge(EnvironmentBase environment)
    {
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

        for(Integer x=0; x<environment.getWidth(); x++){
            for(Integer y=0; y<environment.getHeight(); y++){
                if(environment.containsDirt(x, y) || (environment.getAgentHome().equals(new Point(x, y)))
                        || (environment.getAgentLocation().equals(new Point(x, y)))){
                    for(Direction direction : Direction.values()){
                        list.add(new Node(new Point(x, y), direction, environment));
                    }
                }
            }
        }

        System.out.println(list);

        //7. Entfernungen in Aktionen bestimmen:
        //   a) von der AgentenPosition/Start-Blickrichtung
        //      ->(i) zu allen Dreckpositionen (ausser denen unter (5.) genannten) mit allen dabei möglichen Blickrichtungen
        //      ->(ii) zur nächsten der 4 möglichen Home-Positions-Blickrichtungen
        //   b) von jeder Dreckposition (ausser denen unter (5. genannten) mit allen dabei möglichen Blickrichtungen
        //      ->(i) zu allen anderen Dreckpositionen (ausser denen unter (5.) genannten) mit allen dabei möglichen Blickrichtungen
        //      ->(ii) zur nächsten der 4 möglichen Home-Positions-Blickrichtungen
		calcDistance(list);

        //8. Ein Suchzustand ist nun eine Liste von durchfahrenen Dreckpositionen (ausser (5.)), jede Dreckposition wird maximal 1x in genau einer Blickrichtung durchfahren
        //   Diese Liste kann auch leer sein, dann wird keine Dreckposition angefahren.

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

        return reachablePerf;
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
}
