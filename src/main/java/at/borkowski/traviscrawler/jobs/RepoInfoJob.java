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

import static java.lang.System.currentTimeMillis;

@Service
public class RepoInfoJob {
    @Autowired
    private TravisRepoService travisRepoService;

    @Autowired
    private GithubService githubService;

    @Scheduled(fixedDelay = 10_000, initialDelay = 0)
    public void fetchBuilds() {
        long aa = currentTimeMillis();
        List<TravisRepo> repos = travisRepoService.findSomeWithOldInfoNotZombie(50);
        long bb = currentTimeMillis();

        repos.sort((a, b) -> (a.getInfo().getInfoDate() == null ? -1 :
                (b.getInfo().getInfoDate() == null ? 1 :
                        (a.getInfo().getInfoDate().compareTo(b.getInfo().getInfoDate())))));

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

        System.out.println("[repo info] grabbed " + repos.size() + ", done " + done + " (marked " + notFound + " new zombies) -- took " + (currentTimeMillis() - aa) / 1000 + " s (first part: " + (bb - aa) / 1000 + " s)");
    }
}
