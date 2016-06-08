package at.borkowski.traviscrawler.jobs;

import at.borkowski.traviscrawler.entities.TravisRepo;
import at.borkowski.traviscrawler.service.GithubService;
import at.borkowski.traviscrawler.service.TravisRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@Service
public class RepoInfoJob {
    @Autowired
    private TravisRepoService travisRepoService;

    @Autowired
    private GithubService githubService;

    @Scheduled(fixedDelay = 30_000, initialDelay = 0)
    public void fetchBuilds() {
        List<TravisRepo> repos = travisRepoService.findSomeWithoutInfoNotZombie(50);

        repos.sort((a, b) -> (a.getBuildsStatus() != null && b.getBuildsStatus() != null ?
                -Integer.compare(a.getBuildsStatus().getBuilds().size(), b.getBuildsStatus().getBuilds().size()) :
                (a.getBuildsStatus() != null ? 1 :
                        (b.getBuildsStatus() != null ? -1 : 0))));

        int done = 0;
        int notFound = 0;

        for (TravisRepo repo : repos) {
            if (repo.isZombie()) continue; // should not happen, query excludes zombies

            try {
                repo.setInfo(githubService.getInfo(repo.getSlug()));
                travisRepoService.save(repo);
                done++;
            } catch (Throwable t) {
                if (!(t instanceof HttpClientErrorException) || !((HttpClientErrorException) t).getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                    System.out.println("[repo info] exception while fetching repo info for " + repo.getSlug());
                } else {
                    repo.setZombie(true);
                    travisRepoService.save(repo);
                    notFound++;
                }
            }
        }

        System.out.println("[repo info] grabbed " + repos.size() + ", done " + done + " (marked " + notFound + " new zombies)");
    }
}
