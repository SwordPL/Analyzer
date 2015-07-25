import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.pf.tools.cda.base.model.ClassInformation;

public class Analyzer {
    static final Logger logger = LogManager.getLogger(Analyzer.class);

    private DirectedGraph<ClassInformation, DefaultEdge> graph;

    public Analyzer(DirectedGraph<ClassInformation, DefaultEdge> graph) {
        this.graph = graph;
        analyze();
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
        ConnectivityInspector connectivityInspector = new ConnectivityInspector(graph);
        int numberOfConnectedTriples = 0;
        int numberOfTriangles = 0;
        for (ClassInformation x: graph.vertexSet()) {
            for (ClassInformation y: graph.vertexSet()) {
                for (ClassInformation z: graph.vertexSet()) {
                    if (x.equals(y) || y.equals(z) || z.equals(x)) {
                        continue;
                    } else {
                        int connectedElements = 0;
                        if (connectivityInspector.pathExists(x, y) ||
                                connectivityInspector.pathExists(y, x)) {
                            connectedElements++;
                        }
                        if (connectivityInspector.pathExists(z, y) ||
                                connectivityInspector.pathExists(y, z)) {
                            connectedElements++;
                        }
                        if (connectivityInspector.pathExists(x, z) ||
                                connectivityInspector.pathExists(z, x)) {
                            connectedElements++;
                        }

                        if (connectedElements == 3) {
                            numberOfConnectedTriples++;
                            numberOfTriangles++;
                        } else if (connectedElements == 2) {
                            numberOfConnectedTriples++;
                        }
                    }
                }
            }
        }
        int T = numberOfTriangles/numberOfConnectedTriples;
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
