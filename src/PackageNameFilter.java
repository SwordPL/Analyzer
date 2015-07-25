import org.pf.tools.cda.base.model.ClassPackage;
import org.pf.tools.cda.base.model.processing.AClassPackageProcessor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PackageNameFilter extends AClassPackageProcessor {

    private final Pattern pattern;

    public PackageNameFilter(Pattern pattern) {
        super();
        this.pattern = pattern;
    }

    @Override
    public boolean matches(ClassPackage classPackage) {
        if (pattern == null)
            return false;
        Matcher matcher = pattern.matcher(classPackage.getName());
        return matcher.matches();
    }

    @Override
    public boolean process(ClassPackage classPackage) {
        return true;
    }
}
