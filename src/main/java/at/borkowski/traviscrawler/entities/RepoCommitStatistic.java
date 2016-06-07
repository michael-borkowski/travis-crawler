package at.borkowski.traviscrawler.entities;

public class RepoCommitStatistic {
    private final long fileCount, totalSize;

    public RepoCommitStatistic(long fileCount, long totalSize) {
        this.fileCount = fileCount;
        this.totalSize = totalSize;
    }

    public long getFileCount() {
        return fileCount;
    }

    public long getTotalSize() {
        return totalSize;
    }
}
