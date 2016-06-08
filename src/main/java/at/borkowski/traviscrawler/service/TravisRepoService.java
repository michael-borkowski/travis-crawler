package at.borkowski.traviscrawler.service;

import at.borkowski.traviscrawler.entities.TravisRepo;
import at.borkowski.traviscrawler.repo.TravisRepoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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

    public List<TravisRepo> findAll() {
        return travisRepoRepository.findAll();
    }

    public List<TravisRepo> find50WithoutInfo() {
        return travisRepoRepository.findFirst50ByInfoIsNull();
    }

    public List<TravisRepo> find50WithInfo() {
        return travisRepoRepository.findFirst50ByInfoIsNotNull();
    }
}
