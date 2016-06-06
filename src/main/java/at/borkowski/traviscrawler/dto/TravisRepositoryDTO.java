package at.borkowski.traviscrawler.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class TravisRepositoryDTO {
    private long id;
    private String slug;
    private boolean active;
    private String description;
    @JsonProperty("last_build_id")
    private long lastBuildId;
    @JsonProperty("last_build_number")
    private String lastBuildNumber;
    @JsonProperty("last_build_state")
    private String lastBuildState;
    @JsonProperty("last_build_duration")
    private long lastBuildDuration;
    @JsonProperty("last_build_language")
    private String lastBuildLanguage;
    @JsonProperty("last_build_started_at")
    private Date lastBuildStartedAt;
    @JsonProperty("last_build_finished_at")
    private Date lastBuildFinishedAt;
    @JsonProperty("github_language")
    private String githubLanguage;

    public TravisRepositoryDTO() {
    }

    public long getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getDescription() {
        return description;
    }

    public long getLastBuildId() {
        return lastBuildId;
    }

    public String getLastBuildNumber() {
        return lastBuildNumber;
    }

    public String getLastBuildState() {
        return lastBuildState;
    }

    public String getLastBuildLanguage() {
        return lastBuildLanguage;
    }

    public Date getLastBuildStartedAt() {
        return lastBuildStartedAt;
    }

    public Date getLastBuildFinishedAt() {
        return lastBuildFinishedAt;
    }

    public boolean isActive() {
        return active;
    }

    public String getGithubLanguage() {
        return githubLanguage;
    }
}
