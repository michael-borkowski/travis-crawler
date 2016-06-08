package at.borkowski.traviscrawler.entities;

import at.borkowski.traviscrawler.dto.GithubRepoInfoDTO;

import java.util.Date;

import static java.lang.System.currentTimeMillis;

public class RepoInfo {
    private long size;

    private Date infoDate;

    public static long MAX_AGE = 3600 * 24 * 14; // 14 days

    public RepoInfo() {
    }

    public RepoInfo(GithubRepoInfoDTO infoDTO) {
        this.size = infoDTO.getSize();
        this.infoDate = new Date();
    }

    public Date getInfoDate() {
        return infoDate;
    }

    public long getSize() {
        return size;
    }

    public boolean isOutdated() {
        return currentTimeMillis() - infoDate.getTime() > MAX_AGE;
    }
}