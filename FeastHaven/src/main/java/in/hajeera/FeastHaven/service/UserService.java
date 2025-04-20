package in.hajeera.FeastHaven.service;

import in.hajeera.FeastHaven.io.UserRequest;
import in.hajeera.FeastHaven.io.UserResponse;

public interface UserService {

    UserResponse registerUser(UserRequest request);

    String findByUserId();
}
