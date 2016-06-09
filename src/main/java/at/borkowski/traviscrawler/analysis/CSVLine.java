package at.borkowski.traviscrawler.analysis;

public class CSVLine {
    private final String repo;
    private final String language;
    private final long fileCount;
    private final long size;
    private final long duration;

    public CSVLine(String repo, String language, long fileCount, long size, long duration) {
        this.repo = repo;
        this.language = language;
        this.fileCount = fileCount;
        this.size = size;
        this.duration = duration;
    }

    public String getRepo() {
        return repo;
    }

    public String getLanguage() {
        return language;
    }

    public long getFileCount() {
        return fileCount;
    }

    public long getSize() {
        return size;
    }

    public long getDuration() {
        return duration;
    }
}
