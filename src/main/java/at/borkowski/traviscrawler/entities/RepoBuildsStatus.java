package at.borkowski.traviscrawler.entities;

import java.util.LinkedList;
import java.util.List;

public class RepoBuildsStatus {
    private boolean firstReached;

    private List<RepoBuild> builds = new LinkedList<>();

    public boolean isFirstReached() {
        return firstReached;
    }

    public List<RepoBuild> getBuilds() {
        return builds;
    }

    public void setFirstReached() {
        this.firstReached = true;
    }
}
