package pl.edu.agh.depanalyzer.ruby;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class RubyDependencyGraphParser {
    List<String> graph;

    public RubyDependencyGraphParser(String path) {
        try {
            List<String> graph = Files.readAllLines(Paths.get(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    DirectedGraph<String, DefaultEdge> getGraph() {
        DirectedGraph<String, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        return g;
    }

}
