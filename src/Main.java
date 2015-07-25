import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.pf.tools.cda.base.model.ClassInformation;
import org.pf.tools.cda.base.model.ClassPackage;
import org.pf.tools.cda.base.model.Workset;
import org.pf.tools.cda.base.model.workset.ClasspathPartDefinition;
import org.pf.tools.cda.core.init.WorksetInitializer;
import org.pfsw.odem.TypeClassification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Main {
    static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Welcome to Class Dependency Analyser by usage of networks.");
        logger.info("Write a path to needed classpath (example: lib/*.jar):  ");
        Scanner sc = new Scanner(System.in);
        String classpath = sc.nextLine();
        logger.info("Thank you. {} is scanned...", classpath);

        Workset workset = createWorkset(classpath);

        initializeWorkset(workset);

        logger.info("PHASE 3: FILTERING PACKAGES");
        logger.info("Please input Regular Expression describing packages you are interested in.");
        logger.info(".* -- All packages");
        logger.info("java.(.*) - All packages starting with Java");

        String userPattern = sc.nextLine();
        List<ClassPackage> filtered = filterPackages(workset, userPattern);

        logger.info("PHASE 5: CREATING DEPENDENCY GRAPH");
        List<ClassInformation> classes = getClassesInfo(filtered);
        DirectedGraph<ClassInformation, DefaultEdge> graph = createGraph(classes);
    }

    private static Workset createWorkset(String classpath) {
        logger.info("PHASE 1: WORKSET CREATION");
        Workset workset = new Workset("Testing");
        ClasspathPartDefinition partDefinition = new ClasspathPartDefinition(classpath);
        workset.addClasspathPartDefinition(partDefinition);
        return workset;
    }

    private static void initializeWorkset(Workset workset) {
        logger.info("PHASE 2: WORKSET INITIALIZATION");
        WorksetInitializer wsInitializer = new WorksetInitializer(workset);
        wsInitializer.initializeWorksetAndWait(new ProgressMonitor());
    }

    private static List<ClassPackage> filterPackages(Workset workset, String userPattern) {
        Pattern pattern = Pattern.compile(userPattern);
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

    private static DirectedGraph<ClassInformation, DefaultEdge> createGraph(List<ClassInformation> classes) {
        DirectedGraph<ClassInformation, DefaultEdge> g = new DefaultDirectedGraph<>(DefaultEdge.class);
        classes.forEach(g::addVertex);
        classes.forEach(classInfo -> classInfo.getDirectReferredTypes()
                        .stream()
                        .filter(dependency -> dependency.getClassification() == TypeClassification.CLASS)
                        .forEach(dependency -> g.addEdge(classInfo, (ClassInformation) dependency))
        );

        return g;
    }
}