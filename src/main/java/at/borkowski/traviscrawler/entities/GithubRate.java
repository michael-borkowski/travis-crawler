package at.borkowski.traviscrawler.entities;

public class GithubRate {
    private final long limit, remaining, reset;

    public GithubRate(long limit, long remaining, long reset) {
        this.limit = limit;
        this.remaining = remaining;
        this.reset = reset;
    }

    public long getLimit() {
        return limit;
    }

    public long getRemaining() {
        return remaining;
    }

    public long getReset() {
        return reset;
    }

    public long getSecondsUntilReset() {
        return reset - (System.currentTimeMillis() / 1000);
    }

    public long getIntervalMilliseconds() {
        return getSecondsUntilReset() * 1000 / remaining;
    }

    @Override
    public String toString() {
        return "remaining: " + remaining + " / " + limit + ", reset at " + reset + " (in " + getSecondsUntilReset() + " s)";
    }
}
