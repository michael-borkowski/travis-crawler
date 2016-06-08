package at.borkowski.traviscrawler.entities;

import at.borkowski.traviscrawler.util.StaticRandom;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

import static java.lang.System.currentTimeMillis;

@Document
public class TravisRepo {
    @Id
    private String id;

    @Indexed(unique = true)
    private long travisId;

    @Indexed
    private long randomId;

    private String slug, language;
    private boolean active;

    private Date dateFound;
    private long seenByMiner;

    private RepoBuildsStatus buildsStatus = new RepoBuildsStatus();
    private RepoInfo info = null;

    public TravisRepo() {
    }

    public TravisRepo(long travisId, String slug, String language, boolean active, Date dateFound) {
        this.travisId = travisId;
        this.slug = slug;
        this.language = language;
        this.active = active;
        this.dateFound = dateFound;

        this.randomId = StaticRandom.nextLong();
    }

    public long getSeenByMiner() {
        return seenByMiner;
    }

    public void setSeenByMiner(long seenByMiner) {
        this.seenByMiner = seenByMiner;
    }

    public long getTravisId() {
        return travisId;
    }

    public RepoBuildsStatus getBuildsStatus() {
        return buildsStatus;
    }

    public String getSlug() {
        return slug;
    }

    public RepoInfo getInfo() {
        return info;
    }

    public void setInfo(RepoInfo info) {
        this.info = info;
    }
}

