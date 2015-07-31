package pl.edu.agh.depanalyzer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.FloydWarshallShortestPaths;
import org.jgrapht.graph.AsUndirectedGraph;

import java.util.HashMap;
import java.util.Map;

public class Analyzer<V, E> {
    private static final Logger LOGGER = LogManager.getLogger(Analyzer.class);

    private final DirectedGraph<V, E> graph;

    public Analyzer(DirectedGraph<V, E> graph) {
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
        double sum = 0.0;
        AsUndirectedGraph<V, E> undirected = new AsUndirectedGraph<>(graph);
        FloydWarshallShortestPaths<V, E> pathFinder
                = new FloydWarshallShortestPaths<>(undirected);
        ConnectivityInspector<V, E> connectivityInspector
                = new ConnectivityInspector<>(undirected);
        for (V x : undirected.vertexSet()) {
            for (V y : undirected.vertexSet()) {
                if (x.equals(y) || !connectivityInspector.pathExists(x, y)) {
                    continue;
                }
                sum += pathFinder.shortestDistance(x, y);
            }
        }
        int numberOfVertices = undirected.vertexSet().size();
        double l = sum * 1.0 / (numberOfVertices * (numberOfVertices - 1));
        LOGGER.info("Average distance: l = {}", l);
    }

    private void analyzeTransitivity () {
        //parameter C
        int numberOfConnectedTriples = 0;
        int numberOfTriangles = 0;
        for (V x : graph.vertexSet()) {
            for (V y : graph.vertexSet()) {
                for (V z : graph.vertexSet()) {
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
        LOGGER.info("Transitivity: C = {}", T);
    }

    private void analyzeEfficiency() {
        //E
        double sum = 0;

        FloydWarshallShortestPaths<V, E> pathFinder = new FloydWarshallShortestPaths<>(graph);
        ConnectivityInspector<V, E> connectivityInspector
                = new ConnectivityInspector<>(graph);
        for (V x : graph.vertexSet()) {
            for (V y : graph.vertexSet()) {
                if (x.equals(y) || !connectivityInspector.pathExists(x, y)) {
                    continue;
                }
                sum += 1/pathFinder.shortestDistance(x, y);
            }
        }
        int numberOfVertices = graph.vertexSet().size();
        double E = sum * 1/(numberOfVertices*(numberOfVertices-1));
        LOGGER.info("Efficiency: E = {}", E);
    }

    private void analyzeDistribution() {
        Map<Integer, Integer> kMap = new HashMap<>();
        Map<Integer, Integer> kInMap = new HashMap<>();
        Map<Integer, Integer> kOutMap = new HashMap<>();

        for (V x : graph.vertexSet()) {
            int inDeg = graph.inDegreeOf(x);
            int outDeg = graph.outDegreeOf(x);
            int deg = inDeg + outDeg;

            insertOrIncrement(kMap, deg);
            insertOrIncrement(kInMap, inDeg);
            insertOrIncrement(kOutMap, outDeg);
        }

        Map<Integer, Double> normalizedKMap = getProbabilityDistributionMap(kMap);
        Map<Integer, Double> normalizedKInMap = getProbabilityDistributionMap(kInMap);
        Map<Integer, Double> normalizedKOutMap = getProbabilityDistributionMap(kOutMap);

        LOGGER.info("Vertex degree distribution");
        normalizedKMap.forEach((k, v) -> System.out.println(k + ";" + v));
        LOGGER.info("In-vertex degree distribution");
        normalizedKInMap.forEach((k, v) -> System.out.println(k + ";" + v));
        LOGGER.info("Out-vertex degree distribution");
        normalizedKOutMap.forEach((k, v) -> System.out.println(k + ";" + v));

        LOGGER.info("Distribution: TBD");
    }

    private void insertOrIncrement(Map<Integer, Integer> kMap, int deg) {
        if (!kMap.containsKey(deg)) kMap.put(deg, 1);
        else kMap.put(deg, kMap.get(deg) + 1);
    }

    private Map<Integer, Double> getProbabilityDistributionMap(Map<Integer, Integer> kMap) {
        Map<Integer, Double> normalizedKMap = new HashMap<>();
        int sum = kMap.values().stream().mapToInt(k -> k).sum();
        kMap.forEach((k, v) -> normalizedKMap.put(k, (double) v / sum));
        return normalizedKMap;
    }
}
