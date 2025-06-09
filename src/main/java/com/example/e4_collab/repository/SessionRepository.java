package com.example.e4_collab.repository;

import com.example.e4_collab.entity.Session;
import com.example.e4_collab.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@RestResource(exported = false)
public interface SessionRepository extends CrudRepository<Session, Long> {
    Collection<Session> findAllByUser(User user);
    Collection<Session> findAllByUserAndStartTimestampIsBetween(User user, Long startTimestampAfter, Long startTimestampBefore);

    Long countSessionByUser(User user);

    @Query("SELECT MIN(endTimestamp-startTimestamp) FROM Session WHERE user = :user")
    Optional<Long> getMinSessionDurationByUser(@Param("user") User user);
    @Query("SELECT MAX(endTimestamp-startTimestamp) FROM Session WHERE user = :user")
    Optional<Long> getMaxSessionDurationByUser(@Param("user") User user);
    @Query("SELECT AVG(endTimestamp-startTimestamp) FROM Session WHERE user = :user")
    Optional<Long> getAverageSessionDurationByUser(@Param("user") User user);

    @Query(value = """
        SELECT DATE_FORMAT(FROM_UNIXTIME(s.START_TIMESTAMP / 1000), '%Y-%m-01') AS MONTH_DATE, COUNT(*) AS SESSIONS_COUNT
        FROM sessions s
        WHERE s.username = :username
        GROUP BY MONTH_DATE
        ORDER BY MONTH_DATE
    """, nativeQuery = true)
    ArrayList<Map<String, Object>> countMonthlySessionsByUser(@Param("username") String username);

    @Query(value = """
        SELECT
            CASE
               WHEN HOUR(FROM_UNIXTIME(s.START_TIMESTAMP / 1000)) BETWEEN 6 AND 11 THEN 'MORNING'
               WHEN HOUR(FROM_UNIXTIME(s.START_TIMESTAMP / 1000)) BETWEEN 12 AND 17 THEN 'AFTERNOON'
               ELSE 'EVENING'
            END AS TIME_SLOT,
            COUNT(*) AS SESSIONS_COUNT
        FROM sessions s
        WHERE s.username = :username
        GROUP BY TIME_SLOT
    """, nativeQuery = true)
    ArrayList<Map<String, Object>> countHourlySessionsByUser(@Param("username") String username);
}
