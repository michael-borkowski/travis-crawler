package at.borkowski.traviscrawler.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

public class TravisBuildDTO {
    private long id;
    @JsonProperty("repository_iD")
    private long repositoryId;
    @JsonProperty("commit_id")
    private long commitId;
    private String number;
    @JsonProperty("event_type")
    private String eventType;
    @JsonProperty("pull_request")
    private boolean pullRequest;
    @JsonProperty("pull_request_title")
    private String pullRequestTitle;
    @JsonProperty("pull_request_number")
    private Long pullRequestNumber;
    private TravisBuildConfigDTO config;
    private String state;
    @JsonProperty("started_at")
    private Date startedAt;
    @JsonProperty("finished_at")
    private Date finishedAt;
    private Long duration;
    @JsonProperty("job_ids")
    private List<Long> jobIds;

    public TravisBuildDTO() {
    }

    public long getRepositoryId() {
        return repositoryId;
    }

    public long getId() {
        return id;
    }

    public long getCommitId() {
        return commitId;
    }

    public String getNumber() {
        return number;
    }

    public String getEventType() {
        return eventType;
    }

    public boolean isPullRequest() {
        return pullRequest;
    }

    public String getPullRequestTitle() {
        return pullRequestTitle;
    }

    public Long getPullRequestNumber() {
        return pullRequestNumber;
    }

    public TravisBuildConfigDTO getConfig() {
        return config;
    }

    public String getState() {
        return state;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public Date getFinishedAt() {
        return finishedAt;
    }

    public Long getDuration() {
        return duration;
    }

    public List<Long> getJobIds() {
        return jobIds;
    }
}
