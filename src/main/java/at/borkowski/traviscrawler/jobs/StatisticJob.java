package at.borkowski.traviscrawler.jobs;

import at.borkowski.traviscrawler.entities.RepoBuild;
import at.borkowski.traviscrawler.entities.RepoCommitStatistic;
import at.borkowski.traviscrawler.entities.TravisRepo;
import at.borkowski.traviscrawler.service.TravisRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Stream;

import static java.lang.System.currentTimeMillis;

@Service
public class StatisticJob {
    @Autowired
    private TravisRepoService travisRepoService;

    private static final String EXPORT_PATH = "/home/michael/research/ml/travis-export/";
    private static final String EXPORT_FILE_BASE = "export";
    private static final String EXPORT_PROGRESS_SUFFIX = "-progress";
    private static final String EXPORT_FILE_EXTENSION = ".csv";

    @SuppressWarnings("ConstantConditions")
    @Scheduled(fixedDelay = 30000, initialDelay = 0)
    public void printStatistics() {
        final long[] buildSum = {0};
        final long[] repoCount = {0};
        final long[] reposWithSize = {0};
        final long[] oversizeRepos = {0};
        final long[] okSizeRepos = {0};
        final String[] maxSizeRepo = {null};
        final long[] maxSize = {0};
        final long[] done = {0};
        final long[] zombies = {0};

        final long[] buildsWithRealStats = {0};
        final long[] buildsWithFailedStats = {0};

        final long[] reposWithBuildsNoStats = {0};
        final long[] reposWithBuildsNoStatsInfo = {0};
        final long[] reposWithBuildsSomeStats = {0};
        final long[] reposWithBuildsSomeStatsInfo = {0};
        final long[] reposWithBuildsAllStats = {0};
        final long[] reposWithNoBuilds = {0};
        long a = currentTimeMillis();

        //noinspection ResultOfMethodCallIgnored
        new File(EXPORT_PATH).mkdirs();

        try (PrintWriter out = new PrintWriter(EXPORT_PATH + EXPORT_FILE_BASE + EXPORT_PROGRESS_SUFFIX + EXPORT_FILE_EXTENSION);
             Stream<TravisRepo> repos = travisRepoService.findAll()) {
            out.println(HEADER);
            repos.forEach(travisRepo -> {
                export(out, travisRepo);
                buildSum[0] += travisRepo.getBuildsStatus().getBuilds().size();
                repoCount[0]++;

                if (travisRepo.isZombie()) zombies[0]++;
                else {
                    done[0] += travisRepo.getBuildsStatus().isFirstReached() ? 1 : 0;

                    travisRepo.getBuildsStatus().getBuilds().stream().filter(repoBuild -> repoBuild.getCommit().getStats() != null).forEach(repoBuild -> {
                        if (repoBuild.getCommit().getStats().getFileCount() >= 0) buildsWithRealStats[0]++;
                        else buildsWithFailedStats[0]++;
                    });

                    if (!travisRepo.getInfo().isOutdated()) {
                        reposWithSize[0]++;
                        if (travisRepo.getInfo().getSize() > CommitStatisticsJob.SIZE_THRESHOLD) oversizeRepos[0]++;
                        else okSizeRepos[0]++;

                        if (travisRepo.getInfo().getSize() > maxSize[0]) {
                            maxSize[0] = travisRepo.getInfo().getSize();
                            maxSizeRepo[0] = travisRepo.getSlug();
                        }
                    }

                    if (travisRepo.getBuildsStatus().getBuilds().size() > 0) {
                        boolean someStats = false;
                        boolean allStats = travisRepo.getBuildsStatus().isFirstReached();

                        for (RepoBuild repoBuild : travisRepo.getBuildsStatus().getBuilds()) {
                            if (repoBuild.getCommit().getStats() != null) someStats = true;
                            else allStats = false;

                            if (!allStats && someStats) break;
                        }

                        if (someStats && !allStats) {
                            reposWithBuildsSomeStats[0]++;
                            if (!travisRepo.getInfo().isOutdated()) reposWithBuildsSomeStatsInfo[0]++;
                        } else if (!someStats) {
                            reposWithBuildsNoStats[0]++;
                            if (!travisRepo.getInfo().isOutdated()) reposWithBuildsNoStatsInfo[0]++;
                        } else reposWithBuildsAllStats[0]++;
                    } else reposWithNoBuilds[0]++;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean renameSuccessful = new File(EXPORT_PATH + EXPORT_FILE_BASE + EXPORT_PROGRESS_SUFFIX + EXPORT_FILE_EXTENSION)
                .renameTo(new File(EXPORT_PATH + EXPORT_FILE_BASE + EXPORT_FILE_EXTENSION));

        if (!renameSuccessful) System.err.println("[output] rename failed");

        System.out.println("[stat] ==========================================================================");
        System.out.println("[stat] = REPOS                                                                  =");
        System.out.println("[stat] ==========================================================================");
        System.out.println("[stat] total repos: " + repoCount[0]);
        System.out.println("[stat] repo info known for " + reposWithSize[0] + " (" + percent(reposWithSize[0], repoCount[0]) + ")");
        System.out.println("[stat] repo size ok: " + okSizeRepos[0] + " (" + percent(okSizeRepos[0], repoCount[0]) + "); oversize repos: " + oversizeRepos[0] + " (" + percent(oversizeRepos[0], repoCount[0]) + ")");
        if (maxSizeRepo[0] != null)
            System.out.println("[stat] biggest repo: " + maxSizeRepo[0] + " (" + maxSize[0] + ")");
        System.out.println("[stat] zombies: " + zombies[0] + " (" + percent(zombies[0], repoCount[0]) + ")");
        System.out.println("[stat] repos with end reached: " + done[0] + " (" + percent(done[0], repoCount[0]) + ")");
        System.out.println("[stat]");
        System.out.println("[stat] repos without builds:                     " + reposWithNoBuilds[0] + " (" + percent(reposWithNoBuilds[0], repoCount[0]) + ")");
        System.out.println("[stat] repos with builds, no build stats:        " + reposWithBuildsNoStats[0] + " (" + percent(reposWithBuildsNoStats[0], repoCount[0]) + ")");
        System.out.println("[stat] repos with builds, no build stats (info): " + reposWithBuildsNoStatsInfo[0] + " (" + percent(reposWithBuildsNoStatsInfo[0], repoCount[0]) + ")");
        System.out.println("[stat] repos with some build stats:              " + reposWithBuildsSomeStats[0] + " (" + percent(reposWithBuildsSomeStats[0], repoCount[0]) + ")");
        System.out.println("[stat] repos with some build stats (info):       " + reposWithBuildsSomeStatsInfo[0] + " (" + percent(reposWithBuildsSomeStatsInfo[0], repoCount[0]) + ")");
        System.out.println("[stat] repos with some all stats:                " + reposWithBuildsAllStats[0] + " (" + percent(reposWithBuildsAllStats[0], repoCount[0]) + ")");
        System.out.println("[stat]");
        System.out.println("[stat] ==========================================================================");
        System.out.println("[stat] = BUILDS                                                                 =");
        System.out.println("[stat] ==========================================================================");
        System.out.println("[stat] total builds: " + buildSum[0]);
        System.out.println("[stat] builds with stats: " + buildsWithRealStats[0] + " (" + percent(buildsWithRealStats[0], buildSum[0]) + ")");
        System.out.println("[stat]      failed stats: " + buildsWithFailedStats[0] + " (" + percent(buildsWithFailedStats[0], buildSum[0]) + ")");
        System.out.println("[stat] average builds per repo: " + (int) ((double) buildSum[0] / repoCount[0]));
        System.out.println("[stat]");
        System.out.println("[stat] duration: " + (currentTimeMillis() - a) + " ms");
        System.out.println("[stat] ==========================================================================");
    }

    private static final String HEADER = "repo-id;slug;language;buildnumber;sha;files;bytes;duration";

    private void export(PrintWriter out, TravisRepo travisRepo) {
        if (travisRepo.isZombie()) return;
        travisRepo.getBuildsStatus().getBuilds().forEach(build -> {
            if (build.getCommit().getStats() == null) return;
            RepoBuild.Commit commit = build.getCommit();
            RepoCommitStatistic stats = commit.getStats();
            out.printf("%d;%s;%s;%s;%s;%d;%d;%d", travisRepo.getTravisId(), travisRepo.getSlug(), build.getLanguage(), build.getNumber(), commit.getSha(), stats.getFileCount(), stats.getTotalSize(), build.getDuration());
            out.printf("\n");
        });
    }

    private String percent(long a, long b) {
        double percent = ((double) a / b) * 100;
        return (double) Math.round(percent * 100) / 100 + " %";
    }
}
