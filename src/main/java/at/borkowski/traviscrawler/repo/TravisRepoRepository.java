package at.borkowski.traviscrawler.repo;

import at.borkowski.traviscrawler.entities.TravisRepo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface TravisRepoRepository extends MongoRepository<TravisRepo, String> {
    TravisRepo findByTravisId(long id);

    @Query("{ 'randomId' : { $gt: ?0 } }")
    TravisRepo findFloor(long randomId);

    @Query("{ 'randomId' : { $lt: ?0 } }")
    TravisRepo findCeiling(long randomId);

    List<TravisRepo> findFirst50ByInfoIsNullAndZombieNot(boolean zombie);

    List<TravisRepo> findFirst200ByInfoIsNotNull();
}
