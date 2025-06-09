package com.example.e4_collab.repository;

import com.example.e4_collab.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource(path = "users", exported = false)
public interface UserRepository extends CrudRepository<User, String> {
    Optional<User> findByAccessToken(String accessToken);
}
