package in.hajeera.FeastHaven.repository;

import in.hajeera.FeastHaven.entity.CartEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.s3.endpoints.internal.Value.Str;

import java.util.Optional;

@Repository
public interface CartRepository extends MongoRepository<CartEntity,String> {

    Optional<CartEntity> findByUserId(String userid);

    void deleteByUserId(String userId);


}
