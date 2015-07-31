package pl.edu.agh.depanalyzer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.ext.GraphMLExporter;
import org.jgrapht.ext.IntegerEdgeNameProvider;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.StringNameProvider;
import org.jgrapht.graph.DefaultEdge;
import org.pf.tools.cda.base.model.ClassInformation;
import org.xml.sax.SAXException;
import pl.edu.agh.depanalyzer.java.JavaDependencyGrapher;

import javax.xml.transform.TransformerConfigurationException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public final class Main {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    private Main() {

    }

    public static void main(String[] args) {
        JavaDependencyGrapher jdg = new JavaDependencyGrapher();
        DirectedGraph<ClassInformation, DefaultEdge> graph = jdg.createGraph(jdg.getClasses());

        LOGGER.info("PHASE 6: CHOOSING Largest Connected Component");
        Set<ClassInformation> lccSet = getLCCSet(graph);
        graph = jdg.createGraph(lccSet);
        saveGraphToFile(graph);

        LOGGER.info("PHASE 7: ANALYSIS");
        Analyzer<ClassInformation, DefaultEdge> analyzer = new Analyzer<>(graph);
        analyzer.analyze();
    }

    private static Set<ClassInformation> getLCCSet(DirectedGraph<ClassInformation, DefaultEdge> graph) {
        ConnectivityInspector<ClassInformation, DefaultEdge> inspector
                = new ConnectivityInspector<>(graph);
        List<Set<ClassInformation>> sets = inspector.connectedSets();
        Set<ClassInformation> lccVertices = null;
        for (Set<ClassInformation> set : sets) {
            if (lccVertices == null || lccVertices.size() < set.size())
                lccVertices = set;
        }
        return lccVertices;
    }

    private static void saveGraphToFile(DirectedGraph<ClassInformation, DefaultEdge> graph) {
        GraphMLExporter<ClassInformation, DefaultEdge> exporter =
                new GraphMLExporter<>(new IntegerNameProvider<>(),
                        new StringNameProvider<>(), new IntegerEdgeNameProvider<>(), null);
        try {
            exporter.export(new FileWriter("file.graphml"), graph);
        } catch (IOException | SAXException | TransformerConfigurationException e) {
            LOGGER.error("Something has gone wrong during saving graph to file...");
        }
    }

}