package com.example.e4_collab_rest.repository;

import com.example.e4_collab_rest.entity.SensorSession;
import com.example.e4_collab_rest.entity.Session;
import com.example.e4_collab_rest.entity.id.SensorSessionId;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface SensorSessionRepository extends JpaRepository<SensorSession, SensorSessionId> {
    Collection<SensorSession> findAllBySession(Session session);
}
