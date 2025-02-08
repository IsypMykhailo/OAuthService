package com.auth.springbackend.service;

import com.auth.springbackend.model.AuthProvider;
import com.auth.springbackend.model.User;
import com.auth.springbackend.model.token.ConfirmationToken;
import com.auth.springbackend.payload.request.SignUpRequest;
import com.auth.springbackend.payload.response.ApiResponse;
import com.auth.springbackend.repository.ConfirmationTokenRepository;
import com.auth.springbackend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConfirmationTokenRepository confirmationTokenRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, ConfirmationTokenRepository confirmationTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.confirmationTokenRepository = confirmationTokenRepository;
    }

    public User save(User user) { return userRepository.save(user);}

    // Search
    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    public Optional<User> findById(Long Id) {
        return userRepository.findById(Id);
    }

    // Exist
    public Boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    public Boolean existsByLogin(String login) {
        return userRepository.existsByLogin(login);
    }


    public ResponseEntity<?> confirmEmail(String confirmationToken) {
        ConfirmationToken token = confirmationTokenRepository.findByConfirmationToken(confirmationToken);
        if(token != null)
        {
            Optional<User> user = userRepository.findByEmail(token.getUser().getEmail());
           /* if(user.get().getActive())
            {
                return ResponseEntity.ok(new ApiResponse(false, "User already confirmed his email"));
            }*/
            user.get().setActive(true);;
            userRepository.save(user.get());
            confirmationTokenRepository.deleteById(token.getId());
            return ResponseEntity.ok(new ApiResponse(true, "Email Confirmed"));
        }
        return ResponseEntity.badRequest().body(new ApiResponse(false, "Error: Couldn't verify email"));
    }


    public User createUser(SignUpRequest signUpRequest) {
        User user = new User();
        user.setFullname(signUpRequest.getLogin());
        user.setLogin(signUpRequest.getLogin());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(signUpRequest.getPassword());
        user.setProvider(AuthProvider.local);
        user.setActive(false);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return user;
    }

}
