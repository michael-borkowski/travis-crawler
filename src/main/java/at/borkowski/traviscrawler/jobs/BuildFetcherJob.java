package at.borkowski.traviscrawler.jobs;

import at.borkowski.traviscrawler.dto.TravisBuildDTO;
import at.borkowski.traviscrawler.dto.TravisBuildsDTO;
import at.borkowski.traviscrawler.dto.TravisCommitDTO;
import at.borkowski.traviscrawler.entities.RepoBuild;
import at.borkowski.traviscrawler.entities.TravisRepo;
import at.borkowski.traviscrawler.service.TravisRepoService;
import at.borkowski.traviscrawler.service.TravisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
public class BuildFetcherJob {
    @Autowired
    private TravisRepoService travisRepoService;

    @Autowired
    private TravisService travisService;

    @Scheduled(fixedDelay = 5000, initialDelay = 0)
    public void fetchBuilds() {
        List<TravisRepo> repos = travisRepoService.findSome(50);

        repos.sort((a, b) -> -Integer.compare(a.getBuildsStatus().getBuilds().size(), b.getBuildsStatus().getBuilds().size()));

        int buildsAdded = 0;
        System.out.println("[builds] crawling over " + repos.size() + " repos");

        for (TravisRepo repo : repos) {
            if (repo.isZombie()) continue;

            List<RepoBuild> builds = repo.getBuildsStatus().getBuilds();
            if (repo.getBuildsStatus().isFirstReached()) {
                String latestBuild = builds.size() == 0 ? null : builds.get(0).getNumber();

                try {
                    List<RepoBuild> catchingUpBuilds = new LinkedList<>();
                    boolean caughtUp = false;
                    boolean paged = false;

                    TravisBuildsDTO buildsDto = travisService.get("/repos/" + repo.getSlug() + "/builds", TravisBuildsDTO.class);

                    do {
                        Map<Long, TravisCommitDTO> commits = build(buildsDto.commits);
                        String lastNumber = null;
                        if (buildsDto.builds.size() == 0) {
                            if (paged) System.out.println("[builds] WEIRD, didn't reach end ... " + repo.getSlug());
                            break;
                        }

                        for (TravisBuildDTO build : buildsDto.builds) {
                            if (latestBuild == null || build.getNumber().equals(latestBuild) || isIn(repo.getBuildsStatus().getBuilds(), build.getNumber())) {
                                caughtUp = true;
                                break;
                            }
                            if (build.getFinishedAt() != null)
                                catchingUpBuilds.add(new RepoBuild(build, commits));
                            lastNumber = build.getNumber();
                        }

                        if (!caughtUp)
                            buildsDto = travisService.get("/repos/" + repo.getSlug() + "/builds?after_number=" + lastNumber, TravisBuildsDTO.class);
                        paged = true;
                    }
                    while (!caughtUp);

                    for (int i = catchingUpBuilds.size() - 1; i >= 0; i--) builds.add(0, catchingUpBuilds.get(i));
                    travisRepoService.save(repo);
                } catch (Exception ex) {
                    System.out.println("[builds] exception while fetching newest " + repo.getSlug());
                }
            } else {
                TravisBuildsDTO buildsDto;
                String urlSuffix = builds.size() > 0 ? "?after_number=" + builds.get(builds.size() - 1).getNumber() : "";
                try {
                    buildsDto = travisService.get("/repos/" + repo.getSlug() + "/builds" + urlSuffix, TravisBuildsDTO.class);
                } catch (Exception ex) {
                    System.out.println("[builds] exception while fetching " + repo.getSlug());
                    ex.printStackTrace();
                    continue;
                }

                Map<Long, TravisCommitDTO> commits = build(buildsDto.commits);

                if (buildsDto.builds.size() == 0) repo.getBuildsStatus().setFirstReached();
                else buildsDto.builds.stream()
                        .filter(build -> build.getFinishedAt() != null)
                        .forEach(build -> builds.add(new RepoBuild(build, commits)));

                try {
                    travisRepoService.save(repo);
                } catch (Throwable t) {
                    System.out.println("current repo: " + repo.getSlug());
                    throw t;
                }

                buildsAdded += buildsDto.builds.size();
            }
        }

        System.out.println("[builds] added " + buildsAdded + " builds");
    }

    private boolean isIn(List<RepoBuild> builds, String number) {
        return builds.stream().filter(x -> x.getNumber().equals(number)).findAny().isPresent();
    }

    private Map<Long, TravisCommitDTO> build(List<TravisCommitDTO> commits) {
        Map<Long, TravisCommitDTO> ret = new HashMap<>();
        commits.forEach(x -> ret.put(x.getId(), x));
        return ret;
    }
}
