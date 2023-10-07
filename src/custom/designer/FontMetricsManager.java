package custom.designer;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class FontMetricsManager {
    public static FontMetrics metrics;

    public static Set<FontMetricsWaiter> fontMetricsWaiters = new HashSet<>();
}
