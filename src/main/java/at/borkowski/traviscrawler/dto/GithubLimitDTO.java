package at.borkowski.traviscrawler.dto;

public class GithubLimitDTO {

    public GithubLimitResourcesDTO resources;

    public static class GithubLimitResourcesDTO {
        public LimitDTO core, search;
    }

    public static class LimitDTO {
        public long limit, remaining, reset;
    }
}
