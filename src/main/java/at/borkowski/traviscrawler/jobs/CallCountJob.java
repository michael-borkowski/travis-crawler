package at.borkowski.traviscrawler.jobs;

import at.borkowski.traviscrawler.service.TravisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CallCountJob {
    @Autowired
    private TravisService travisService;

    private long previousCallCount1;
    private long previousCallCount5;
    private long previousCallCount10;

    private Long callCount1, callCount5, callCount10;

    @Scheduled(fixedRate = 60_000, initialDelay = 10_000)
    public void printStatistics() {
        if (callCount1 != null)
            System.out.println("[api] calls in last  1 min: " + callCount1 + " (max 100)");
        if (callCount5 != null)
            System.out.println("[api] calls in last  5 min: " + callCount5 + " (max 300)");
        if (callCount10 != null)
            System.out.println("[api] calls in last 10 min: " + callCount10 + " (max 1000)");
    }

    @Scheduled(fixedRate = 60_000)
    public void check1() {
        callCount1 = travisService.getCallCount() - previousCallCount1;
        previousCallCount1 = travisService.getCallCount();
    }

    @Scheduled(fixedRate = 5 * 60_000)
    public void check5() {
        callCount5 = travisService.getCallCount() - previousCallCount5;
        previousCallCount5 = travisService.getCallCount();
    }

    @Scheduled(fixedRate = 10 * 60_000)
    public void check10() {
        callCount10 = travisService.getCallCount() - previousCallCount10;
        previousCallCount10 = travisService.getCallCount();
    }
}
