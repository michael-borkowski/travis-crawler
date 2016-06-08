package at.borkowski.traviscrawler.jobs;

import at.borkowski.traviscrawler.entities.RepoBuild;
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
        long reposWithSize = 0;
        int done = 0;

        long buildsWithRealStats = 0;
        long buildsWithFailedStats = 0;

        for (TravisRepo travisRepo : travisRepoService.findAll()) {
            buildSum += travisRepo.getBuildsStatus().getBuilds().size();
            repoCount++;

            done += travisRepo.getBuildsStatus().isFirstReached() ? 1 : 0;

            for (RepoBuild repoBuild : travisRepo.getBuildsStatus().getBuilds())
                if (repoBuild.getCommit().getStats() != null)
                    if (repoBuild.getCommit().getStats().getFileCount() >= 0) buildsWithRealStats++;
                    else buildsWithFailedStats++;

            reposWithSize += (travisRepo.getInfo() == null || travisRepo.getInfo().isOutdated()) ? 0 : 1;
        }

        System.out.println("[stat] repos: " + repoCount + ", builds: " + buildSum + " (" + (int) ((double) buildSum / repoCount) + " avg builds) done " + done);
        System.out.println("[stat] total builds " + buildSum + ", with real stats " + buildsWithRealStats + " (failed " + buildsWithFailedStats + ")");
        System.out.println("[stat] repo info known for " + reposWithSize + " out of " + repoCount);
    }
}
