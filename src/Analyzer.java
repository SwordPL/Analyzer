import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.pf.tools.cda.base.model.ClassInformation;

public class Analyzer {
    static final Logger logger = LogManager.getLogger(Analyzer.class);

    private DirectedGraph<ClassInformation, DefaultEdge> graph;

    public Analyzer(DirectedGraph<ClassInformation, DefaultEdge> graph) {
        this.graph = graph;
    }

    private void analyze() {
        analyzeDistribution();
        analyzeEfficiency();
        analyzeTransitivity();
        analyzeAverageDistance();
    }

    private void analyzeAverageDistance () {
        logger.info("Average distance: TBD");
    }

    private void analyzeTransitivity () {
        //parameter C
        int numberOfConnectedTriples = 0;
        int numberOfTriangles = 0;
        for (ClassInformation x: graph.vertexSet()) {
            for (ClassInformation y: graph.vertexSet()) {
                for (ClassInformation z: graph.vertexSet()) {
                    if (x.equals(y) || y.equals(z) || z.equals(x)) {
                        continue;
                    }

                    if ((graph.containsEdge(x, y) || graph.containsEdge(y, x)) &&
                            (graph.containsEdge(z, y) || graph.containsEdge(y, z))) {
                        numberOfConnectedTriples++;
                        if (graph.containsEdge(z, x) || graph.containsEdge(x, z)) {
                            numberOfTriangles++;
                        }
                    }
                }
            }
        }
        double T = numberOfTriangles/numberOfConnectedTriples;
        logger.info("Transitivity:");
        logger.info("C = " + T);
    }

    private void analyzeEfficiency() {
        logger.info("Efficiency: TBD");
    }

    private void analyzeDistribution() {
        logger.info("Distribution: TBD");
    }
}
