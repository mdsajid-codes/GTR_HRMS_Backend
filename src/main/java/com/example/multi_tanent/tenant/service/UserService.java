package com.example.multi_tanent.tenant.service;

import org.springframework.stereotype.Service;

import com.example.multi_tanent.tenant.entity.User;
import com.example.multi_tanent.tenant.repository.UserRepository;

import java.util.List;

@Service
public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public User saveUser(User user) {
        return repo.save(user);
    }

    public List<User> getAllUsers() {
        return repo.findAll();
    }
}
