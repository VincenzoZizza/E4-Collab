package com.example.e4_collab.repository;

import com.example.e4_collab.entity.Authority;
import com.example.e4_collab.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.Collection;

@RestResource(exported = false)
public interface AuthorityRepository extends CrudRepository<Authority, String> {
    Collection<Authority> findAllByUser(User user);
    void deleteAuthorityByUser(User user);
}
