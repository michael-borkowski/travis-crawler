package at.borkowski.traviscrawler.entities;

import at.borkowski.traviscrawler.dto.TravisBuildDTO;
import at.borkowski.traviscrawler.dto.TravisCommitDTO;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class RepoBuild {
    private long travisId;
    private long repositoryId;
    private String number;
    private String eventType;
    private boolean pullRequest;
    private String pullRequestTitle;
    private Long pullRequestNumber;
    private String language;
    private List<String> script;
    private List<String> os;
    private String group;
    private String dist;
    private Date startedAt;
    private Date finishedAt;
    private Long duration;
    private List<Long> jobIds;

    private Commit commit;

    public RepoBuild() {
    }

    public RepoBuild(TravisBuildDTO build, Map<Long, TravisCommitDTO> commits) {
        this.travisId = build.getId();
        this.repositoryId = build.getRepositoryId();
        this.number = build.getNumber();
        this.eventType = build.getEventType();
        this.pullRequest = build.isPullRequest();
        this.pullRequestTitle = build.getPullRequestTitle();
        this.pullRequestNumber = build.getPullRequestNumber();
        this.language = build.getConfig().getLanguage();
        this.script = build.getConfig().getScript();
        this.os = build.getConfig().getOs();
        this.group = build.getConfig().getGroup();
        this.dist = build.getConfig().getDist();
        this.startedAt = build.getStartedAt();
        this.finishedAt = build.getFinishedAt();
        this.duration = build.getDuration();
        this.jobIds = build.getJobIds();

        this.commit = new Commit(commits.get(build.getCommitId()));
    }

    public String getNumber() {
        return number;
    }

    public static class Commit {
        private long id;
        private String sha;
        private String branch;
        private String message;
        private Date commitedAt;
        private String authorName;
        private String authorEmail;
        private String committerName;
        private String committerEmail;
        private String compareUrl;
        private Long pullRequestNumber;

        public Commit() {
        }

        public Commit(TravisCommitDTO travisCommitDTO) {
            this.id = travisCommitDTO.getId();
            this.sha = travisCommitDTO.getSha();
            this.branch = travisCommitDTO.getBranch();
            this.message = travisCommitDTO.getMessage();
            this.commitedAt = travisCommitDTO.getCommitedAt();
            this.authorName = travisCommitDTO.getAuthorName();
            this.authorEmail = travisCommitDTO.getAuthorEmail();
            this.committerName = travisCommitDTO.getCommitterName();
            this.committerEmail = travisCommitDTO.getCommitterEmail();
            this.compareUrl = travisCommitDTO.getCompareUrl();
            this.pullRequestNumber = travisCommitDTO.getPullRequestNumber();
        }
    }
}
