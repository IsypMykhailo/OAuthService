package com.auth.springbackend.controller;


import com.auth.springbackend.payload.request.UpdateRequest;
import com.auth.springbackend.payload.request.user.*;
import com.auth.springbackend.payload.response.UpdateResponse;
import com.auth.springbackend.exception.BadRequestException;
import com.auth.springbackend.exception.ResourceNotFoundException;
import com.auth.springbackend.repository.UserRepository;
import com.auth.springbackend.security.CurrentUser;
import com.auth.springbackend.security.UserPrincipal;
import com.auth.springbackend.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserController(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public User getCurrentUser(@CurrentUser UserPrincipal userPrincipal) {

        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));
    }

    @GetMapping("/get-users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping("/get-specific-users")
    public List<User> getSpecificUsers(@Valid @RequestBody ListUserRequest listUserRequest) {
        List<User> list = new ArrayList<>();
        for (String id: (listUserRequest.getUserList())) {
            Optional<User> user = userRepository.findById( Long.parseLong(id.trim()));
            user.ifPresent(list::add);
        }
        return list;
    }

    @PostMapping("/get-specific-user")
    public Optional<User> getSpecificUser(@Valid @RequestBody IdRequest idRequest) {
        return userRepository.findById(Long.parseLong(idRequest.getId().trim()));
    }

    @PostMapping("/get-user-by-fname")
    public Optional<User> getByFullName(@Valid @RequestBody FullNameRequest fullNameRequest) {
        return userRepository.findByFullnameContaining(fullNameRequest.getFullname());
    }

    @PostMapping("/get-user-by-login")
    public Optional<User> getByLogin(@Valid @RequestBody LoginRequest loginRequest) {
        return userRepository.findByFullnameContaining(loginRequest.getLogin());
    }

    @PostMapping("/get-user-by-email")
    public Optional<User> getByEmail(@Valid @RequestBody EmailRequest emailRequest) {
        return userRepository.findByFullnameContaining(emailRequest.getEmail());
    }

    @PostMapping("/update-user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> RedactUser(@CurrentUser UserPrincipal userPrincipal,
                                        @RequestBody UpdateRequest updateRequest) {
        try {
            User curUser = userRepository.findById(userPrincipal.getId()).get();
            /*curUser.setPassword(passwordEncoder.encode(user.getPassword()));
            curUser.setImageUrl(user.getImageUrl());
            curUser.setName(user.getName());
            curUser.setDescription(user.getDescription());*/
            if(updateRequest.getPassword() != null) {
                    curUser.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
            }
            //curUser.setImageUrl(image);
           /* if(updateRequest.getName() != null) {
                if (updateRequest.getName().length() > 2 && updateRequest.getName().length() < 35) {
                    curUser.setName(updateRequest.getName());
                }
            }*/
            if(updateRequest.getFullname() != null) {
                if (updateRequest.getFullname().length() > 0 && updateRequest.getFullname().length() < 255) {
                    curUser.setFullname(updateRequest.getFullname());
                }
            }
            curUser.setUpdated_at(new Date());
            userRepository.save(curUser);
            return ResponseEntity.ok(new UpdateResponse(true, "User updated", curUser));
        } catch (Exception ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }

}