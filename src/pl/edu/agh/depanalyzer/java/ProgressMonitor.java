package pl.edu.agh.depanalyzer.java;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pf.tools.cda.core.processing.IProgressMonitor;
import org.pf.util.StopWatch;

public class ProgressMonitor implements IProgressMonitor {
    static final Logger LOGGER = LogManager.getLogger(ProgressMonitor.class);
    private final StopWatch stopWatch = new StopWatch();

    public ProgressMonitor() {
        super();
    }

    @Override
    public void startProgressMonitor() {
        stopWatch.start();
        LOGGER.info("=== Start of workset processing ===");
    }

    @Override
    public void terminateProgressMonitor() {
        stopWatch.stop();
        LOGGER.info("=== End of workset processing. Duration: " + stopWatch.getDuration() + " ms ===");
    }

    @Override
    public boolean showProgress(int value, Object[] info) {
        for (Object object : info) {
            LOGGER.info("Processing: {}", object.toString());
        }
        return true;
    }
}