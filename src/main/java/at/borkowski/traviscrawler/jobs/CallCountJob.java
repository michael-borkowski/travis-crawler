package at.borkowski.traviscrawler.jobs;

import at.borkowski.traviscrawler.service.GithubService;
import at.borkowski.traviscrawler.service.TravisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CallCountJob {
    @Autowired
    private TravisService travisService;

    @Autowired
    private GithubService githubService;

    private long previousTravisCallCount1;
    private long previousTravisCallCount5;
    private long previousTravisCallCount10;
    private long previousGithubCallCount1;
    private long previousGithubCallCount5;
    private long previousGithubCallCount10;

    private Long travisCallCount1, travisCallCount5, travisCallCount10;
    private Long githubCallCount1, githubCallCount5, githubCallCount10;

    @Scheduled(fixedRate = 60_000, initialDelay = 10_000)
    public void printStatistics() {
        if (travisCallCount1 != null)
            System.out.println("[api travis] calls in last  1 min: " + travisCallCount1 + " (max 100)");
        if (travisCallCount5 != null)
            System.out.println("[api travis] calls in last  5 min: " + travisCallCount5 + " (max 300)");
        if (travisCallCount10 != null)
            System.out.println("[api travis] calls in last 10 min: " + travisCallCount10 + " (max 1000)");

        if (githubCallCount1 != null)
            System.out.println("[api github] calls in last  1 min: " + githubCallCount1);
        if (githubCallCount5 != null)
            System.out.println("[api github] calls in last  5 min: " + githubCallCount5);
        if (githubCallCount10 != null)
            System.out.println("[api github] calls in last 10 min: " + githubCallCount10);

        System.out.println("[api github] status: " + githubService.getRateStatus());
    }

    @Scheduled(fixedRate = 60_000)
    public void check1() {
        travisCallCount1 = travisService.getCallCount() - previousTravisCallCount1;
        previousTravisCallCount1 = travisService.getCallCount();

        githubCallCount1 = githubService.getCallCount() - previousGithubCallCount1;
        previousGithubCallCount1 = githubService.getCallCount();
    }

    @Scheduled(fixedRate = 5 * 60_000)
    public void check5() {
        travisCallCount5 = travisService.getCallCount() - previousTravisCallCount5;
        previousTravisCallCount5 = travisService.getCallCount();

        githubCallCount5 = githubService.getCallCount() - previousGithubCallCount5;
        previousGithubCallCount5 = githubService.getCallCount();
    }

    @Scheduled(fixedRate = 10 * 60_000)
    public void check10() {
        travisCallCount10 = travisService.getCallCount() - previousTravisCallCount10;
        previousTravisCallCount10 = travisService.getCallCount();

        githubCallCount10 = githubService.getCallCount() - previousGithubCallCount10;
        previousGithubCallCount10 = githubService.getCallCount();
    }
}
