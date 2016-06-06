package at.borkowski.traviscrawler.jobs;

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
        int buildsAdded = 0;
        System.out.println("[builds] crawling over " + repos.size() + " repos");

        for (TravisRepo repo : repos) {
            if (repo.getBuildsStatus().isFirstReached()) {
                // todo
            } else {
                List<RepoBuild> builds = repo.getBuildsStatus().getBuilds();
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
                        .forEach(build -> repo.getBuildsStatus().getBuilds().add(new RepoBuild(build, commits)));
                travisRepoService.save(repo);

                buildsAdded += buildsDto.builds.size();
            }
        }

        System.out.println("[builds] added " + buildsAdded + " builds");
    }

    private Map<Long, TravisCommitDTO> build(List<TravisCommitDTO> commits) {
        Map<Long, TravisCommitDTO> ret = new HashMap<>();
        commits.forEach(x -> ret.put(x.getId(), x));
        return ret;
    }
}
