package in.hajeera.FeastHaven.repository;

import in.hajeera.FeastHaven.entity.OrderEntity;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<OrderEntity,String> {

    List<OrderEntity> findByUserId(String userId);
    Optional<OrderEntity> findByRazorpayOrderId(String razorpayOrderId);

}
