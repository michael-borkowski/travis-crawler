package at.borkowski.traviscrawler.jobs;

import at.borkowski.traviscrawler.entities.RepoBuild;
import at.borkowski.traviscrawler.entities.RepoCommitStatistic;
import at.borkowski.traviscrawler.entities.TravisRepo;
import at.borkowski.traviscrawler.git.GitRepo;
import at.borkowski.traviscrawler.git.GitRepoHandle;
import at.borkowski.traviscrawler.service.TravisRepoService;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Integer.compare;

@Service
public class CommitStatisticsJob {
    public static final int REPOS_GRABBED = 50;
    public static final int REPOS_PROCESSED = 3;

    @Autowired
    private TravisRepoService travisRepoService;

    @Scheduled(fixedDelay = 10_000, initialDelay = 0)
    public void countCommitSizes() {
        List<TravisRepo> someRepos = travisRepoService.findSome(REPOS_GRABBED);

        someRepos = filterReposWithAllCommits(someRepos);

        Map<TravisRepo, Set<RepoBuild.Commit>> commits = new HashMap<>();
        for (TravisRepo someRepo : someRepos) {
            Set<RepoBuild.Commit> repoCommits = someRepo.getBuildsStatus().getBuilds()
                    .stream().map(RepoBuild::getCommit).collect(Collectors.toSet());
            commits.put(someRepo, repoCommits);
        }

        Collections.sort(someRepos, (a, b) -> -compare(countShasWithoutStats(commits.get(a)), countShasWithoutStats(commits.get(b))));

        for (int i = 0; i < REPOS_PROCESSED; i++) {
            TravisRepo repo = someRepos.get(i);

            System.out.println("[commit stat] inspecting repo " + repo.getSlug());
            try {
                try (GitRepoHandle git = GitRepo.grab(repo.getSlug())) {
                    Map<String, RepoCommitStatistic> statCache = new HashMap<>();

                    for (RepoBuild.Commit commit : commits.get(repo)) {
                        if (commit.getStats() == null) {
                            String sha = commit.getSha();

                            RepoCommitStatistic stats = statCache.get(sha);
                            if (stats != null) {
                                System.out.println("[commit stat] reusing commit " + sha);
                            } else {
                                System.out.println("[commit stat] inspecting commit " + sha);
                                try {
                                    stats = stat(git, sha);
                                } catch (Throwable t) {
                                    System.out.println("[commit stat] exception while inspecting " + repo.getSlug() + " sha " + sha);
                                    stats = new RepoCommitStatistic(-1, -1);
                                }
                                statCache.put(sha, stats);
                            }
                            commit.setStats(stats);
                            travisRepoService.save(repo);
                        }
                    }
                }

            } catch (Throwable t) {
                System.out.println("[commit stat] exception while inspecting " + repo.getSlug());
                t.printStackTrace();
            }
        }
    }

    private int countShasWithoutStats(Set<RepoBuild.Commit> commits) {
        return (int) commits.stream()
                .filter(commit -> commit.getStats() == null)
                .map(RepoBuild.Commit::getSha).count();
    }

    private static List<TravisRepo> filterReposWithAllCommits(List<TravisRepo> someRepos) {
        List<TravisRepo> ret = new LinkedList<>();
        for (TravisRepo someRepo : someRepos) {
            boolean ok = false;

            for (RepoBuild repoBuild : someRepo.getBuildsStatus().getBuilds()) {
                if (repoBuild.getCommit().getStats() == null) {
                    ok = true;
                    break;
                }
            }

            if (ok) ret.add(someRepo);
        }
        return ret;
    }

    private RepoCommitStatistic stat(GitRepoHandle git, String commit) throws GitAPIException {
        git.checkout(commit);

        Walker walker = new Walker();
        walker.walk(git.getWorkTree());

        return new RepoCommitStatistic(walker.fileCount, walker.totalSize);
    }

    private class Walker {
        private long fileCount, totalSize;

        void walk(File file) {
            File[] subFiles;
            if (file.isDirectory() && (subFiles = file.listFiles()) != null)
                for (File subFile : subFiles) walk(subFile);
            else {
                fileCount += 1;
                totalSize += file.length();
            }
        }
    }
}
