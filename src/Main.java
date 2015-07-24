import java.util.Scanner;
import org.apache.logging.log4j.*;
import org.pf.tools.cda.base.model.Workset;
import org.pf.tools.cda.base.model.workset.ClasspathPartDefinition;
import org.pf.tools.cda.core.init.WorksetInitializer;

public class Main {
    static Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Welcome to Class Dependency Analyser by usage of networks.");
        logger.info("Write a path to needed classpath: ");
        Scanner sc = new Scanner(System.in);
        String classpath = sc.nextLine();
        logger.info("Thank you. {} is scanned...", classpath);

        logger.info("PHASE 1: WORKSET CREATION");
        Workset workset = new Workset("Testing");
        ClasspathPartDefinition partDefinition = new ClasspathPartDefinition(classpath);
        workset.addClasspathPartDefinition(partDefinition);

        logger.info("PHASE 2: WORKSET INITIALIZATION");
        WorksetInitializer wsInitializer = new WorksetInitializer(workset);
        wsInitializer.initializeWorksetAndWait(null);

        logger.info("PHASE 3: DEPENDENCY ANALYSIS");

    }
}
