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
        long reposWithSizeAndBuilds = 0;
        long oversizeRepos = 0;
        long okSizeRepos = 0;
        String maxSizeRepo = null;
        long maxSize = 0;
        int done = 0;
        int zombies = 0;

        long reposWithBuildsNoInfo = 0, reposWithInfoNoBuilds = 0;

        long buildsWithRealStats = 0;
        long buildsWithFailedStats = 0;

        for (TravisRepo travisRepo : travisRepoService.findAll()) {
            buildSum += travisRepo.getBuildsStatus().getBuilds().size();
            repoCount++;

            if (travisRepo.isZombie()) zombies++;
            else {
                done += travisRepo.getBuildsStatus().isFirstReached() ? 1 : 0;

                for (RepoBuild repoBuild : travisRepo.getBuildsStatus().getBuilds())
                    if (repoBuild.getCommit().getStats() != null)
                        if (repoBuild.getCommit().getStats().getFileCount() >= 0) buildsWithRealStats++;
                        else buildsWithFailedStats++;

                if (!travisRepo.getInfo().isOutdated()) {
                    reposWithSize++;
                    if (travisRepo.getInfo().getSize() > CommitStatisticsJob.SIZE_THRESHOLD) oversizeRepos++;
                    else okSizeRepos++;

                    if (travisRepo.getInfo().getSize() > maxSize) {
                        maxSize = travisRepo.getInfo().getSize();
                        maxSizeRepo = travisRepo.getSlug();
                    }
                    else if(travisRepo.getBuildsStatus().getBuilds().size() > 0) reposWithSizeAndBuilds++;
                    else reposWithInfoNoBuilds++;
                }
                else if(travisRepo.getBuildsStatus().getBuilds().size() > 0) reposWithBuildsNoInfo++;
            }
        }

        System.out.println("=========================================");
        System.out.println("[stat] repos: " + repoCount + ", builds: " + buildSum + " (" + (int) ((double) buildSum / repoCount) + " avg builds) done " + done);
        System.out.println("[stat] total builds " + buildSum + ", with real stats " + buildsWithRealStats + " (failed " + buildsWithFailedStats + ")");
        System.out.println("[stat] repo info known for " + reposWithSize + " out of " + repoCount);
        System.out.println("[stat] repo size ok: " + okSizeRepos + "; oversize repos: " + oversizeRepos);
        if (maxSizeRepo != null) System.out.println("[stat] biggest repo: " + maxSizeRepo + " (" + maxSize + ")");
        System.out.println("[stat] zombies: " + zombies);
        System.out.println("[stat] repos with info and builds: " + reposWithSizeAndBuilds);
        System.out.println("[stat] repos with builds but no info: " + reposWithBuildsNoInfo);
        System.out.println("[stat] repos with info but no builds: " + reposWithInfoNoBuilds);
        System.out.println("=========================================");
    }
}
