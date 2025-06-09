package com.example.e4_collab.repository;

import com.example.e4_collab.entity.SensorSession;
import com.example.e4_collab.entity.Session;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.Collection;

@RestResource(exported = false)
public interface SensorSessionRepository extends CrudRepository<SensorSession, Long> {
    Collection<SensorSession> findAllBySession(Session session);
}
