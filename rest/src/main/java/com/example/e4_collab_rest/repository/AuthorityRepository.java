package com.example.e4_collab_rest.repository;

import com.example.e4_collab_rest.entity.Authority;
import com.example.e4_collab_rest.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, String> {
    Collection<Authority> findAllByUser(User user);
    void deleteAuthorityByUser(User user);
}
