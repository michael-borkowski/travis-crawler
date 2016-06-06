package at.borkowski.traviscrawler.jobs;

import at.borkowski.traviscrawler.dto.TravisReposDTO;
import at.borkowski.traviscrawler.dto.TravisRepositoryDTO;
import at.borkowski.traviscrawler.entities.TravisRepo;
import at.borkowski.traviscrawler.service.TravisRepoService;
import at.borkowski.traviscrawler.service.TravisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class MinerJob {
    @Autowired
    private TravisRepoService travisRepoService;

    @Autowired
    private TravisService travisService;

    @Scheduled(fixedDelay = 5000, initialDelay = 0)
    public void mine() {
        TravisReposDTO travisReposDTO = travisService.get("/repos/", TravisReposDTO.class);

        int newRepos = 0;

        for (TravisRepositoryDTO repo : travisReposDTO.repos) {
            TravisRepo travisRepo = travisRepoService.findByTravisId(repo.getId());
            if (travisRepo == null) {
                newRepos++;
                travisRepo = travisRepoService.save(new TravisRepo(repo.getId(), repo.getSlug(), repo.getGithubLanguage(), repo.isActive(), new Date()));
            }

            travisRepo.setSeenByMiner(travisRepo.getSeenByMiner() + 1);
        }

        System.out.println("[mine] got " + travisReposDTO.repos.size() + " repos, " + newRepos + " were new");
    }
}
