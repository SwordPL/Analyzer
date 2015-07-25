import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pf.tools.cda.base.model.ClassInformation;
import org.pf.tools.cda.base.model.ClassPackage;
import org.pf.tools.cda.base.model.Workset;
import org.pf.tools.cda.base.model.workset.ClasspathPartDefinition;
import org.pf.tools.cda.core.init.WorksetInitializer;

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

        logger.info("PHASE 5: GATHERING CLASSES");
        List<ClassInformation> classes = getClassesInfo(filtered);

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
}