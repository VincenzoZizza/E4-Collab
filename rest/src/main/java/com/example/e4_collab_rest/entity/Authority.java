package com.example.e4_collab_rest.entity;

import com.example.e4_collab_rest.enums.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.security.core.GrantedAuthority;

@Entity
@Table(name = "AUTHORITIES")
public class Authority implements GrantedAuthority {

    @MapsId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "username", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private User user;

    @Id
    private String username;

    @JsonProperty("username")
    public String getUsername() {
        return username;
    }

    @Column(nullable = false)
    private UserRole userRole = UserRole.ROLE_USER;

    public Authority() {}

    public Authority(User user, UserRole userRole) {
        this.user = user;
        this.userRole = userRole;
    }

    public User getUser() {
        return user;
    }

    public UserRole getUserRole() {
        return userRole;
    }

    @JsonIgnore
    @Override
    public String getAuthority() {
        return userRole.name();
    }
}
