package com.example.e4_collab.service;

import com.example.e4_collab.entity.Authority;
import com.example.e4_collab.entity.User;
import com.example.e4_collab.enums.UserRole;
import com.example.e4_collab.repository.AuthorityRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class AuthorityService {
    private final AuthorityRepository authorityRepository;

    @Autowired
    public AuthorityService(AuthorityRepository authorityRepository) {
        this.authorityRepository = authorityRepository;
    }

    public Collection<Authority> getAuthorities(User user) {
        return authorityRepository.findAllByUser(user);
    }

    @Transactional
    public void setUserAuthority(User user, UserRole userRole) {
        if(user.getRole() != userRole) {
            authorityRepository.deleteAuthorityByUser(user);
            authorityRepository.save(new Authority(user, userRole));
        }
    }
}
