package at.borkowski.traviscrawler;

import at.borkowski.traviscrawler.analysis.CSVLine;
import at.borkowski.traviscrawler.analysis.Scatterplot;
import at.borkowski.traviscrawler.jobs.StatisticJob;
import at.borkowski.traviscrawler.ml.MachineLearning;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

import static java.lang.Long.compare;
import static java.util.stream.Collectors.toList;

public class AnalysisApplication {
    private static final String FILE_PATH = StatisticJob.EXPORT_PATH + StatisticJob.EXPORT_FILE_BASE + StatisticJob.EXPORT_FILE_EXTENSION;
    private static final String PLOT_BASE = StatisticJob.EXPORT_PATH + "/plots/";

    private static final int TOP = 100;

    public static void main(String args[]) {
        List<CSVLine> lines = readFile(true);

        lines = lines.stream()
                .filter(line -> line.getFileCount() > 0)
                .filter(line -> line.getSize() > 0)
                .collect(toList());

        Map<String, Long> _repoBuildCount = new HashMap<>();
        Map<String, Long> _languageBuildCount = new HashMap<>();

        lines.stream()
                .forEach(line -> {
                    count(_repoBuildCount, line.getRepo());
                    count(_languageBuildCount, line.getLanguage());
                });

        Map<String, Long> repoBuildCount = sort(_repoBuildCount);
        Map<String, Long> languageBuildCount = sort(_languageBuildCount);

        System.out.println("");
        System.out.println("========================");
        System.out.println("TOP REPOSITORIES");
        System.out.println("========================");
        int count = 0;
        for (String repo : repoBuildCount.keySet()) {
            if (++count > TOP) break;
            System.out.println(repo + ": " + repoBuildCount.get(repo));

            Scatterplot scatterplotFileCount = new Scatterplot();
            Scatterplot scatterplotSize = new Scatterplot();

            List<CSVLine> pre = lines.stream()
                    .filter(line -> line.getRepo().equals(repo))
                    .collect(toList());

            Quantiles qFileCount = Quantiles.from(pre, CSVLine::getFileCount);
            Quantiles qSize = Quantiles.from(pre, CSVLine::getSize);
            Quantiles qDuration = Quantiles.from(pre, CSVLine::getDuration);

            pre.stream()
                    .filter(qFileCount::accepts)
                    .filter(qDuration::accepts)
                    .forEach(line -> scatterplotFileCount.addPoint(new Scatterplot.Point(line.getFileCount(), line.getDuration())));

            pre.stream()
                    .filter(qSize::accepts)
                    .filter(qDuration::accepts)
                    .forEach(line -> scatterplotSize.addPoint(new Scatterplot.Point(line.getSize(), line.getDuration())));

            MachineLearning.analyze(scatterplotFileCount);

            save(scatterplotFileCount, PLOT_BASE + "repo/" + nice(repo) + "-fileCount", repo, "file count");
            save(scatterplotSize, PLOT_BASE + "repo/" + nice(repo) + "-size", repo, "size");
        }

        System.out.println("");
        System.out.println("========================");
        System.out.println("TOP LANGUAGES");
        System.out.println("========================");
        count = 0;
        for (String language : languageBuildCount.keySet()) {
            if (++count > TOP) break;
            System.out.println(language + ": " + languageBuildCount.get(language));

            Scatterplot scatterplotFileCount = new Scatterplot();
            Scatterplot scatterplotSize = new Scatterplot();


            List<CSVLine> pre = lines.stream()
                    .filter(line -> line.getLanguage().equals(language))
                    .collect(toList());

            Quantiles qFileCount = Quantiles.from(pre, CSVLine::getFileCount);
            Quantiles qSize = Quantiles.from(pre, CSVLine::getSize);
            Quantiles qDuration = Quantiles.from(pre, CSVLine::getDuration);

            pre.stream()
                    .filter(qFileCount::accepts)
                    .filter(qDuration::accepts)
                    .forEach(line -> scatterplotFileCount.addPoint(new Scatterplot.Point(line.getFileCount(), line.getDuration())));

            pre.stream()
                    .filter(qSize::accepts)
                    .filter(qDuration::accepts)
                    .forEach(line -> scatterplotSize.addPoint(new Scatterplot.Point(line.getFileCount(), line.getDuration())));

            save(scatterplotFileCount, PLOT_BASE + "lang/" + nice(language) + "-fileCount", language, "file count");
            save(scatterplotSize, PLOT_BASE + "lang/" + nice(language) + "-size", language, "size");
        }
    }

    private static void save(Scatterplot scatterplot, String fileName, String title, String xLabel) {
        //noinspection ResultOfMethodCallIgnored
        new File(fileName).getParentFile().mkdirs();

        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series = new XYSeries("Series");
        for (Scatterplot.Point point : scatterplot.getPoints()) series.add(point.getX(), point.getY());
        dataset.addSeries(series);

        JFreeChart jFreeChart = ChartFactory.createScatterPlot(title, xLabel, "duration", dataset);
        BufferedImage image = jFreeChart.createBufferedImage(1000, 800);
        try {
            ImageIO.write(image, "png", new File(fileName + ".png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String nice(String name) {
        StringBuilder ret = new StringBuilder();
        for (char c : name.toCharArray())
            if (!(c >= 'a' && c <= 'z') && !(c >= 'A' && c <= 'Z') && !(c >= '0' && c <= '9') &&
                    !(c == '.' || c == '-' || c == '_')) ret.append('_');
            else ret.append(c);
        return ret.toString();
    }

    private static void scatter(Map<String, Scatterplot> map, String key, long x, long y) {
        if (!map.containsKey(key)) map.put(key, new Scatterplot());
        map.get(key).addPoint(new Scatterplot.Point(x, y));
    }

    private static Map<String, Long> sort(Map<String, Long> repoBuildCount) {
        LinkedHashMap<String, Long> res = new LinkedHashMap<>();
        repoBuildCount.entrySet().stream().sorted((a, b) -> -compare(a.getValue(), b.getValue()))
                .forEachOrdered(x -> res.put(x.getKey(), x.getValue()));
        return res;
    }

    private static void count(Map<String, Long> map, String key) {
        if (!map.containsKey(key)) map.put(key, 0L);
        map.put(key, map.get(key) + 1);
    }

    private static List<CSVLine> readFile(boolean skipFirst) {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            if (skipFirst) br.readLine();
            List<CSVLine> lines = new LinkedList<>();
            String line;
            int lineNumber = skipFirst ? 2 : 1;
            while ((line = br.readLine()) != null) {
                try {
                    String[] fields = line.split(";");
                    lines.add(new CSVLine(fields[1], fields[2], Long.valueOf(fields[5]), Long.valueOf(fields[6]), Long.valueOf(fields[7])));
                    lineNumber++;
                } catch (Exception ex) {
                    System.out.println("exception at line #" + lineNumber);
                    throw ex;
                }
            }

            return lines;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class Quantiles {
        public static final double DROP_Q = 0.05;
        public static final int MIN_ELEMENTS = 100;

        private final Function<CSVLine, Long> f;

        private final long q0, q1;
        private final int n;

        public Quantiles(int n, long q0, long q1, Function<CSVLine, Long> f) {
            this.n = n;
            this.q0 = q0;
            this.q1 = q1;
            this.f = f;
        }

        public static Quantiles from(List<CSVLine> lines, Function<CSVLine, Long> f) {
            Series series = new Series();
            lines.forEach(x -> series.numbers.add(f.apply(x)));

            long q0 = series.getQuantile(DROP_Q);
            long q1 = series.getQuantile(1 - DROP_Q);

            return new Quantiles(lines.size(), q0, q1, f);
        }

        public boolean accepts(CSVLine line) {
            if (n < MIN_ELEMENTS) return true;
            long v = f.apply(line);
            return v >= q0 && v <= q1;
        }
    }

    private static class Series {
        private final TreeSet<Long> numbers = new TreeSet<>((a, b) -> {
            int compare = Long.compare(a, b);
            return compare == 0 ? 1 : compare;
        });

        public long getQuantile(double q) {
            int element;
            Iterator<Long> iterator;
            if (q <= 0.5) {
                iterator = numbers.iterator();
                element = (int) (q * numbers.size());
            } else {
                iterator = numbers.descendingIterator();
                element = (int) ((1 - q) * numbers.size());
            }
            for (int i = 0; i < element; i++) iterator.next();
            return iterator.hasNext() ? iterator.next() : numbers.last();
        }
    }
}
