import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.ext.GraphMLExporter;
import org.jgrapht.ext.IntegerEdgeNameProvider;
import org.jgrapht.ext.IntegerNameProvider;
import org.jgrapht.ext.StringNameProvider;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.pf.tools.cda.base.model.ClassInformation;
import org.pf.tools.cda.base.model.ClassPackage;
import org.pf.tools.cda.base.model.Workset;
import org.pf.tools.cda.base.model.workset.ClasspathPartDefinition;
import org.pf.tools.cda.core.init.WorksetInitializer;
import org.pfsw.odem.TypeClassification;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerConfigurationException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public final class Main {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);
    private static String userPattern;

    private Main() {

    }

    public static void main(String[] args) {
        LOGGER.info("Welcome to Class Dependency Analyser by usage of networks.");
        LOGGER.info("Write a path to needed classpath (example: lib/*.jar):  ");
        Scanner sc = new Scanner(System.in);
        String classpath = sc.nextLine();
        LOGGER.info("Thank you. {} is scanned...", classpath);

        Workset workset = createWorkset(classpath);

        initializeWorkset(workset);

        LOGGER.info("PHASE 3: FILTERING PACKAGES");
        LOGGER.info("Please input Regular Expression describing packages you are interested in.");
        LOGGER.info(".* -- All packages");
        LOGGER.info("java.(.*) - All packages starting with Java");

        userPattern = sc.nextLine();
        List<ClassPackage> filteredPackages = filterPackages(workset, userPattern);

        LOGGER.info("PHASE 5: CREATING DEPENDENCY GRAPH");
        List<ClassInformation> classes = getClassesInfo(filteredPackages);
        DirectedGraph<ClassInformation, DefaultEdge> graph = createGraph(classes);

        LOGGER.info("PHASE 6: CHOOSING Largest Connected Component");
        Set<ClassInformation> lccSet = getLCCSet(graph);
        graph = createGraph(lccSet);
        saveGraphToFile(graph);

        LOGGER.info("PHASE 7: ANALYSIS");
        Analyzer analyzer = new Analyzer(graph);
        analyzer.analyze();
    }

    private static Workset createWorkset(String classpath) {
        LOGGER.info("PHASE 1: WORKSET CREATION");
        Workset workset = new Workset("Analyzer");
        ClasspathPartDefinition partDefinition = new ClasspathPartDefinition(classpath);
        workset.addClasspathPartDefinition(partDefinition);
        return workset;
    }

    private static void initializeWorkset(Workset workset) {
        LOGGER.info("PHASE 2: WORKSET INITIALIZATION");
        WorksetInitializer wsInitializer = new WorksetInitializer(workset);
        wsInitializer.initializeWorksetAndWait(new ProgressMonitor());
    }

    private static List<ClassPackage> filterPackages(Workset workset, String uPattern) {
        Pattern pattern = Pattern.compile(uPattern);
        PackageNameFilter filter = new PackageNameFilter(pattern);
        List<ClassPackage> result = new ArrayList<>();
        workset.processClassPackageObjects(result, filter);
        return result;
    }

    private static List<ClassInformation> getClassesInfo(List<ClassPackage> filtered) {
        List<ClassInformation> classes = new ArrayList<>();
        for (ClassPackage cp : filtered) {
            Collections.addAll(classes, cp.getAllContainedClasses());
        }
        return classes;
    }

    private static DirectedGraph<ClassInformation, DefaultEdge> createGraph(Collection<ClassInformation> classes) {
        DirectedGraph<ClassInformation, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        if (classes == null) return g;

        Pattern pattern = Pattern.compile(userPattern);
        classes.forEach(g::addVertex);
        classes.forEach(classInfo -> classInfo.getDirectReferredTypes()
                        .stream()
                        .filter(dependency -> dependency.getClassification() != TypeClassification.UNKNOWN)
                        .map(dependency -> (ClassInformation) dependency)
                        .filter(dependency -> pattern.matcher(dependency.getClassName()).matches())
                        .forEach(dependency -> g.addEdge(classInfo, dependency))
        );
        return g;
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