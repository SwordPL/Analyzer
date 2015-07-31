package pl.edu.agh.depanalyzer.java;

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

import java.util.*;
import java.util.regex.Pattern;


public class JavaDependencyGrapher {
    private static final Logger LOGGER = LogManager.getLogger(JavaDependencyGrapher.class);
    private static String userPattern;

    public List<ClassInformation> getClasses() {
        LOGGER.info("Welcome to Class Dependency Analyser - Java aspect - by usage of networks.");
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
        return getClassesInfo(filteredPackages);
    }

    private static Workset createWorkset(String classpath) {
        LOGGER.info("PHASE 1: WORKSET CREATION");
        Workset workset = new Workset("Analyzer");
        ClasspathPartDefinition partDefinition = new ClasspathPartDefinition(classpath);
        workset.addClasspathPartDefinition(partDefinition);
        return workset;
    }

    private void initializeWorkset(Workset workset) {
        LOGGER.info("PHASE 2: WORKSET INITIALIZATION");
        WorksetInitializer wsInitializer = new WorksetInitializer(workset);
        wsInitializer.initializeWorksetAndWait(new ProgressMonitor());
    }

    private List<ClassPackage> filterPackages(Workset workset, String uPattern) {
        Pattern pattern = Pattern.compile(uPattern);
        PackageNameFilter filter = new PackageNameFilter(pattern);
        List<ClassPackage> result = new ArrayList<>();
        workset.processClassPackageObjects(result, filter);
        return result;
    }

    private List<ClassInformation> getClassesInfo(List<ClassPackage> filtered) {
        List<ClassInformation> classes = new ArrayList<>();
        for (ClassPackage cp : filtered) {
            Collections.addAll(classes, cp.getAllContainedClasses());
        }
        return classes;
    }

    public DirectedGraph<ClassInformation, DefaultEdge> createGraph(Collection<ClassInformation> classes) {
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
}
