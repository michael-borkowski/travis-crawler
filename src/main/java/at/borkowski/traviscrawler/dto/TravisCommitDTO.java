package at.borkowski.traviscrawler.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;

public class TravisCommitDTO {
    private long id;
    private String sha;
    private String branch;
    private String message;
    @JsonProperty("committed_at")
    private Date commitedAt;
    @JsonProperty("author_name")
    private String authorName;
    @JsonProperty("author_email")
    private String authorEmail;
    @JsonProperty("committer_name")
    private String committerName;
    @JsonProperty("committer_email")
    private String committerEmail;
    @JsonProperty("compare_url")
    private String compareUrl;
    @JsonProperty("pull_request_number")
    private Long pullRequestNumber;

    public TravisCommitDTO() {
    }

    public long getId() {
        return id;
    }

    public String getSha() {
        return sha;
    }

    public String getBranch() {
        return branch;
    }

    public String getMessage() {
        return message;
    }

    public Date getCommitedAt() {
        return commitedAt;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getAuthorEmail() {
        return authorEmail;
    }

    public String getCommitterName() {
        return committerName;
    }

    public String getCommitterEmail() {
        return committerEmail;
    }

    public String getCompareUrl() {
        return compareUrl;
    }

    public Long getPullRequestNumber() {
        return pullRequestNumber;
    }
}
