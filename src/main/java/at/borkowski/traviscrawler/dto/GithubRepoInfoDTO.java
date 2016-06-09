package at.borkowski.traviscrawler.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GithubRepoInfoDTO {
    private long id;
    private Owner owner;
    private String name;
    @JsonProperty("full_name")
    private String fullName;
    private String description;
    private String url;
    private String language;
    private long size;
    @JsonProperty("default_branch")
    private String defaultBranch;

    public long getSize() {
        return size;
    }

    public String getLanguage() {
        return language;
    }

    public static class Owner {

    }
}
