package at.borkowski.traviscrawler.jobs;

import at.borkowski.traviscrawler.entities.TravisRepo;
import at.borkowski.traviscrawler.service.TravisRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class StatisticJob {
    @Autowired
    private TravisRepoService travisRepoService;

    @Scheduled(fixedDelay = 30000, initialDelay = 0)
    public void printStatistics() {
        long buildSum = 0;
        long repoCount = 0;
        int done = 0;

        for (TravisRepo travisRepo : travisRepoService.findAll()) {
            buildSum += travisRepo.getBuildsStatus().getBuilds().size();
            repoCount++;

            done += travisRepo.getBuildsStatus().isFirstReached() ? 1 : 0;
        }

        System.out.println("[stat] repos: " + repoCount + ", builds: " + buildSum + " (" + (int) ((double) buildSum / repoCount) + " avg builds) done " + done);
    }
}
