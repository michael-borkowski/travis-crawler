package at.borkowski.traviscrawler.jobs;

import at.borkowski.traviscrawler.entities.RepoBuild;
import at.borkowski.traviscrawler.entities.RepoCommitStatistic;
import at.borkowski.traviscrawler.entities.TravisRepo;
import at.borkowski.traviscrawler.git.GitRepo;
import at.borkowski.traviscrawler.git.GitRepoHandle;
import at.borkowski.traviscrawler.service.TravisRepoService;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Integer.compare;
import static java.util.stream.Collectors.toCollection;

@Service
public class CommitStatisticsJob {
    public static final int REPOS_PROCESSED = 5;
    public static final long SIZE_THRESHOLD = 1024 * 200; // 200 MB in KB

    @Autowired
    private TravisRepoService travisRepoService;

    private static final boolean print = false;

    @Scheduled(fixedDelay = 10_000, initialDelay = 0)
    public void countCommitSizes() {
        List<TravisRepo> someRepos = travisRepoService.find200WithInfo();
        System.out.println("[commit stat] initial finding: " + someRepos.size());

        someRepos = filterNonZombies(someRepos);
        someRepos = filterReposWithUnanalyzedBuilds(someRepos);
        someRepos = filterReposBelowSizeThreshold(someRepos);

        Map<TravisRepo, Set<RepoBuild.Commit>> commits = new HashMap<>();
        for (TravisRepo someRepo : someRepos) {
            Set<RepoBuild.Commit> repoCommits = someRepo.getBuildsStatus().getBuilds()
                    .stream().map(RepoBuild::getCommit).collect(Collectors.toSet());
            commits.put(someRepo, repoCommits);
        }

        Collections.sort(someRepos, (a, b) -> -compare(countShasWithoutStats(commits.get(a)), countShasWithoutStats(commits.get(b))));

        if (someRepos.size() == 0) {
            System.out.println("[commit stat] no repos found to inspect");
            return;
        }

        System.out.println("[commit stat] inspecting (up to) " + REPOS_PROCESSED + " out of " + someRepos.size() + " grabbed repos with info");

        for (int i = 0; i < REPOS_PROCESSED; i++) {
            if (i >= someRepos.size()) break;
            TravisRepo repo = someRepos.get(i);

            System.out.println("[commit stat] inspecting repo " + repo.getSlug() + " (size " + repo.getInfo().getSize() + ")");
            try {
                try (GitRepoHandle git = GitRepo.grab(repo.getSlug())) {
                    Map<String, RepoCommitStatistic> statCache = new HashMap<>();

                    for (RepoBuild.Commit commit : commits.get(repo)) {
                        if (commit.getStats() == null) {
                            String sha = commit.getSha();

                            RepoCommitStatistic stats = statCache.get(sha);
                            if (stats != null) {
                                if (print) System.out.println("[commit stat] reusing commit " + sha);
                            } else {
                                if (print) System.out.println("[commit stat] inspecting commit " + sha);
                                try {
                                    stats = stat(git, sha);
                                } catch (Throwable t) {
                                    if (!(t instanceof JGitInternalException) || !("" + t.getMessage()).contains("Missing unknown ")) {
                                        if (print) {
                                            System.out.println("[commit stat] exception while inspecting " + repo.getSlug() + " sha " + sha);
                                            t.printStackTrace();
                                        }
                                        stats = new RepoCommitStatistic(-1, -1);
                                    }
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

    private List<TravisRepo> filterReposBelowSizeThreshold(List<TravisRepo> someRepos) {
        return someRepos.stream()
                .filter(someRepo -> someRepo.getInfo() != null && someRepo.getInfo().getSize() < SIZE_THRESHOLD)
                .collect(toCollection(LinkedList::new));
    }

    private List<TravisRepo> filterNonZombies(List<TravisRepo> someRepos) {
        return someRepos.stream()
                .filter(someRepo -> !someRepo.isZombie())
                .collect(toCollection(LinkedList::new));
    }

    private int countShasWithoutStats(Set<RepoBuild.Commit> commits) {
        return (int) commits.stream()
                .filter(commit -> commit.getStats() == null)
                .map(RepoBuild.Commit::getSha).count();
    }

    private static List<TravisRepo> filterReposWithUnanalyzedBuilds(List<TravisRepo> someRepos) {
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
