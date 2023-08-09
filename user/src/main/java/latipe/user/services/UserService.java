package latipe.user.services;

import latipe.user.repositories.IUserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService  {
    private final IUserRepository userRepository;

    public UserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

}
