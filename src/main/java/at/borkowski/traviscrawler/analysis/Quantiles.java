package at.borkowski.traviscrawler.analysis;

import at.borkowski.traviscrawler.AnalysisApplication;

import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Function;

public class Quantiles {
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
