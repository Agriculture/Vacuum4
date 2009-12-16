
import vacuumcleaner.base.*;
import vacuumcleaner.agents.*;
import vacuumcleaner.environments.*;
import java.io.File;
import java.awt.Point;

public class Main {

	/**
	 * Testet einen Evaluator direkt ohne Oberfläche. Die erreichte Performance und die durchlaufenen Felder werden angezeigt.
	 * @param environment Umgebung, für die der Agent die bestmögliche Performance bestimmen soll
	 * @param evaluator Agent, der die bestmögliche Performance bestimmt
	 */
	private static void testConsole(EnvironmentBase environment, IEnvironmentEvaluator evaluator) {
		System.out.println("Teste Performance mit " + evaluator.toString());
		int bestPerformance = evaluator.getOptimalPerformanceAtFullKnowledge(environment);
		System.out.println("best mögliche Performance: " + bestPerformance);
		System.out.print("Durchlaufene Felder: (");
		boolean first = true;
		for (Point p : evaluator.getVisitedPoints()) {
			if (first) {
				first = false;
			} else {
				System.out.print(",");
			}
			System.out.print("[");
			System.out.print(p.x);
			System.out.print(",");
			System.out.print(p.y);
			System.out.print("]");
		}
		System.out.println(")");
	}

	/**
	 * Zeigt die übergebene Umgebung an und fügt die übergebenen Evaluatoren in das "Performanceschätzung"-Menü ein
	 * @param environment Anzuzeigende Umgebung
	 * @param evaluators Liste der Evaluatoren
	 */
	private static void testAndView(EnvironmentBase environment, IEnvironmentEvaluator[] evaluators) {
		VacuumCleanerControler.start(
				//Envrionment
				environment,
				//irgendein Agent - um den gehts ja nicht
				new ReflexAgent1D(), //geht nur von einer Dimension aus

				//mögliche Evaluatoren - zu finden im Menü Performancetest
				evaluators);
	}

	public static void main(String[] args) {
		try {
			//1. Umgebung für Performanceschätzung wählen
			//   a) Auswürfeln lassen
			//   EnvironmentBase env=new ObstacleRectangleEnvironment(20,20);
			//   b) Um Verbesserungen des Evaluators zu messen, die selbe Umgebung aus Datei laden:
			String fileName = "problem_klein.env";
			EnvironmentBase env = EnvironmentBase.load(new File(fileName));

			//2. Anzeigen und Evaluator laufen lassen
			//   a) nur Evaluator laufen lassen und Ergebnis auf der Konsole anzeigen
			//testConsole(env,new SimulatedAnnealingEnvironmentEvaluator());

			//   b) Umgebung in "normaler" Simulation anzeigen lassen, Liste von
			//      Evaluatoren ist über Menü "Performanceschätzung" erreichbar
			testAndView(env, new IEnvironmentEvaluator[]{
						new OptimisticEnvironmentEvaluator(),
						new SimulatedAnnealingEnvironmentEvaluator(),});

		} catch (Exception ex) {
			System.out.println("Problem beim Laden:");
			System.out.println(ex.toString());
		}
	}
}
