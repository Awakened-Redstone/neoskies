package skylands.util;

public class UnitConvertions {

    public static String formatTimings(double time) {
        String metric = "µs";
        if (time > 1000) {
            time /= 1000;
            metric = "ms";
        }
        if (time > 1000) {
            time /= 1000;
            metric = "s";
        }
        return String.format("%.2f", time) + metric;
    }
}
