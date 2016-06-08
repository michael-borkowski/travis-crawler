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

    @Scheduled(fixedDelay = 5000, initialDelay = 0)
    public void fetchBuilds() {
        List<TravisRepo> repos = travisRepoService.find50WithoutInfo();
        System.out.println("[repo info] checking " + repos.size() + " repos");
        int done = 0;

        for (TravisRepo repo : repos) {
            try {
                repo.setInfo(githubService.getInfo(repo.getSlug()));
                travisRepoService.save(repo);
                done++;
            } catch (Throwable t) {
                if (!(t instanceof HttpClientErrorException) || !((HttpClientErrorException) t).getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                    System.out.println("[repo info] exception while fetching repo info for " + repo.getSlug());
                }
            }
        }

        System.out.println("[repo info] done " + done);
    }
}
