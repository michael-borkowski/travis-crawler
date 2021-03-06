package at.borkowski.traviscrawler.service;

import at.borkowski.traviscrawler.entities.TravisRepo;
import at.borkowski.traviscrawler.repo.TravisRepoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static at.borkowski.traviscrawler.util.StaticRandom.nextLong;

@Service
public class TravisRepoService {
    @Autowired
    private TravisRepoRepository travisRepoRepository;

    public TravisRepo findByTravisId(long id) {
        return travisRepoRepository.findByTravisId(id);
    }

    public TravisRepo save(TravisRepo repo) {
        return travisRepoRepository.save(repo);
    }

    public List<TravisRepo> findSome(int count) {
        Set<Long> ids = new HashSet<>();
        List<TravisRepo> ret = new LinkedList<>();

        while (ret.size() < count) {
            long randomId = nextLong();
            TravisRepo some = travisRepoRepository.findFloor(randomId);
            if (some == null) some = travisRepoRepository.findCeiling(randomId);
            if (some == null) break;
            if (ids.contains(some.getTravisId())) continue;

            ret.add(some);
            ids.add(some.getTravisId());
        }

        return ret;
    }

    public Stream<TravisRepo> findAll() {
        return travisRepoRepository.findAllAndStream();
    }

    public List<TravisRepo> findSomeWithOldInfoNotZombie(int count) {
        return find(travisRepo -> travisRepo.getInfo().isOld() && !travisRepo.isZombie(), count);
    }

    public List<TravisRepo> findWithInfo(int count) {
        return find(travisRepo -> !travisRepo.getInfo().isOutdated() && !travisRepo.isZombie(), count);
    }

    private List<TravisRepo> find(Predicate<TravisRepo> predicate, int count) {
        List<TravisRepo> ret = new LinkedList<>();

        int retries = 0;
        while (ret.size() < count) {
            List<TravisRepo> some = findSome(count);
            int prev = ret.size();
            ret.addAll(some.stream().filter(predicate).collect(Collectors.toList()));
            if (ret.size() == prev && retries++ >= 20) break;
        }

        return ret;
    }
}
