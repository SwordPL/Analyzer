import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.pf.tools.cda.base.model.ClassInformation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Analyzer {
    static final Logger logger = LogManager.getLogger(Analyzer.class);

    private final DirectedGraph<ClassInformation, DefaultEdge> graph;

    public Analyzer(DirectedGraph<ClassInformation, DefaultEdge> graph) {
        this.graph = graph;
    }

    public void analyze() {
        analyzeDistribution();
        analyzeEfficiency();
        analyzeTransitivity();
        analyzeAverageDistance();
    }

    private void analyzeAverageDistance () {
        //l
        double sum = 0;
        AsUndirectedGraph<ClassInformation, DefaultEdge> undirected = new AsUndirectedGraph<>(graph);
        FloydWarshallShortestPaths<ClassInformation, DefaultEdge> pathFinder
                = new FloydWarshallShortestPaths<>(undirected);
        ConnectivityInspector<ClassInformation, DefaultEdge> connectivityInspector
                = new ConnectivityInspector<>(undirected);
        for (ClassInformation x: undirected.vertexSet()) {
            for (ClassInformation y: undirected.vertexSet()) {
                if (x.equals(y) || !connectivityInspector.pathExists(x, y)) {
                    continue;
                }
                sum += pathFinder.shortestDistance(x, y);
            }
        }
        int numberOfVertices = undirected.vertexSet().size();
        double l = sum * 1/(numberOfVertices*(numberOfVertices-1));
        logger.info("Average distance: l = {}", l);
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
        double T = (double)numberOfTriangles/numberOfConnectedTriples;
        logger.info("Transitivity: C = {}", T);
    }

    private void analyzeEfficiency() {
        //E
        double sum = 0;

        FloydWarshallShortestPaths<ClassInformation, DefaultEdge> pathFinder = new FloydWarshallShortestPaths<>(graph);
        ConnectivityInspector<ClassInformation, DefaultEdge> connectivityInspector
                = new ConnectivityInspector<>(graph);
        for (ClassInformation x: graph.vertexSet()) {
            for (ClassInformation y: graph.vertexSet()) {
                if (x.equals(y) || !connectivityInspector.pathExists(x, y)) {
                    continue;
                }
                sum += 1/pathFinder.shortestDistance(x, y);
            }
        }
        int numberOfVertices = graph.vertexSet().size();
        double E = sum * 1/(numberOfVertices*(numberOfVertices-1));
        logger.info("Efficiency: E = {}", E);
    }

    private void analyzeDistribution() {
        Map<Integer, AtomicInteger> kSumMap = new HashMap<>();
        Map<Integer, AtomicInteger> kInMap = new HashMap<>();
        Map<Integer, AtomicInteger> kOutMap = new HashMap<>();
        for (ClassInformation x: graph.vertexSet()) {
            int inDeg = graph.inDegreeOf(x);
            int outDeg = graph.outDegreeOf(x);
            kSumMap.putIfAbsent(inDeg, new AtomicInteger(0));
            kSumMap.get(inDeg).incrementAndGet();
            kSumMap.putIfAbsent(outDeg, new AtomicInteger(0));
            kSumMap.get(outDeg).incrementAndGet();

            kInMap.putIfAbsent(inDeg, new AtomicInteger(0));
            kInMap.get(inDeg).incrementAndGet();

            kOutMap.putIfAbsent(outDeg, new AtomicInteger(0));
            kOutMap.get(outDeg).incrementAndGet();
        }
        for (Integer i : kSumMap.keySet()) {
            System.out.println(i + ";" + kSumMap.get(i).get());
        }
        logger.info("Distribution: TBD");
    }
}
