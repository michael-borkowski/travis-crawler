package at.borkowski.traviscrawler.jobs;

import at.borkowski.traviscrawler.entities.RepoBuild;
import at.borkowski.traviscrawler.entities.TravisRepo;
import at.borkowski.traviscrawler.service.TravisRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static java.lang.System.currentTimeMillis;

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
        long done = 0;
        long zombies = 0;

        long reposWithBuildsNoInfo = 0, reposWithInfoNoBuilds = 0;

        long buildsWithRealStats = 0;
        long buildsWithFailedStats = 0;

        long reposWithBuildsNoStats = 0;
        long reposWithBuildsSomeStats = 0;
        long reposWithBuildsAllStats = 0;
        long reposWithNoBuilds = 0;

        long a = currentTimeMillis();
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
                    } else if (travisRepo.getBuildsStatus().getBuilds().size() > 0) reposWithSizeAndBuilds++;
                    else reposWithInfoNoBuilds++;
                } else if (travisRepo.getBuildsStatus().getBuilds().size() > 0) reposWithBuildsNoInfo++;

                if (travisRepo.getBuildsStatus().getBuilds().size() > 0) {
                    boolean someStats = false;
                    boolean allStats = travisRepo.getBuildsStatus().isFirstReached();

                    for (RepoBuild repoBuild : travisRepo.getBuildsStatus().getBuilds()) {
                        if (repoBuild.getCommit().getStats() != null) someStats = true;
                        else allStats = false;

                        if (!allStats && someStats) break;
                    }

                    if (someStats && !allStats) reposWithBuildsSomeStats++;
                    else if (!someStats) reposWithBuildsNoStats++;
                    else reposWithBuildsAllStats++;
                }
                else reposWithNoBuilds++;
            }
        }

        System.out.println("[stat] =========================================");
        System.out.println("[stat] = REPOS                                 =");
        System.out.println("[stat] =========================================");
        System.out.println("[stat] repos: " + repoCount);
        System.out.println("[stat] repo info known for " + reposWithSize + " (" + percent(reposWithSize, repoCount) + ")");
        System.out.println("[stat] repo size ok: " + okSizeRepos + " (" + percent(okSizeRepos, repoCount) + "); oversize repos: " + oversizeRepos + " (" + percent(oversizeRepos, repoCount) + ")");
        if (maxSizeRepo != null) System.out.println("[stat] biggest repo: " + maxSizeRepo + " (" + maxSize + ")");
        System.out.println("[stat] zombies: " + zombies + " (" + percent(zombies, repoCount) + ")");
        System.out.println("[stat] repos with info and builds: " + reposWithSizeAndBuilds);
        System.out.println("[stat] repos with builds but no info: " + reposWithBuildsNoInfo);
        System.out.println("[stat] repos with info but no builds: " + reposWithInfoNoBuilds);
        System.out.println("[stat] repos with end reached: " + done + " (" + percent(done, repoCount) + ")");
        System.out.println("[stat]");
        System.out.println("[stat] repos without builds:              " + reposWithNoBuilds + " (" + percent(reposWithNoBuilds, repoCount) + ")");
        System.out.println("[stat] repos with builds, no build stats: " + reposWithBuildsNoStats + " (" + percent(reposWithBuildsNoStats, repoCount) + ")");
        System.out.println("[stat] repos with some build stats:       " + reposWithBuildsSomeStats + " (" + percent(reposWithBuildsSomeStats, repoCount) + ")");
        System.out.println("[stat] repos with some all stats:         " + reposWithBuildsAllStats + " (" + percent(reposWithBuildsAllStats, repoCount) + ")");
        System.out.println("[stat]");
        System.out.println("[stat] =========================================");
        System.out.println("[stat] = BUILDS                                =");
        System.out.println("[stat] =========================================");
        System.out.println("[stat] total builds " + buildSum);
        System.out.println("[stat] builds with stats: " + buildsWithRealStats + " (" + percent(buildsWithRealStats, buildSum) + ")");
        System.out.println("[stat]      failed stats: " + buildsWithFailedStats + " (" + percent(buildsWithFailedStats, buildSum) + ")");
        System.out.println("[stat] average builds per repo: " + (int) ((double) buildSum / repoCount));
        System.out.println("[stat]");
        System.out.println("[stat] duration: " + (currentTimeMillis() - a) + " ms");
        System.out.println("[stat] =========================================");
    }

    private String percent(long a, long b) {
        double percent = ((double) a / b) * 100;
        return (double) Math.round(percent * 100) / 100 + " %";
    }
}
