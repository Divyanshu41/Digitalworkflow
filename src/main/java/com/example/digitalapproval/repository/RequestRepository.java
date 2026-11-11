
package com.example.digitalapproval.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.digitalapproval.entity.Request;
import com.example.digitalapproval.entity.User;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByUser(User user);

    List<Request> findByUser_IdOrderBySubmittedAtDesc(Long userId);

    List<Request> findByStatus(String status);

    List<Request> findByStatusOrderBySubmittedAtDesc(String status);

    List<Request> findAllByOrderBySubmittedAtDesc();

    // Total requests
    @Query("""
        SELECT COUNT(r)
        FROM Request r
        WHERE (:userId IS NULL OR r.user.id = :userId)
    """)
    long countTotalRequests(@Param("userId") Long userId);

    // Count requests by status
    @Query("""
        SELECT COUNT(r)
        FROM Request r
        WHERE r.status = :status
        AND (:userId IS NULL OR r.user.id = :userId)
    """)
    long countRequestsByStatus(
            @Param("status") String status,
            @Param("userId") Long userId
    );

    // Monthly statistics from MySQL
    @Query(value = """
        SELECT
            YEAR(submitted_at) AS year,
            MONTH(submitted_at) AS month,
            COUNT(*) AS totalRequests,
            SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) AS pendingRequests,
            SUM(CASE WHEN status = 'APPROVED' THEN 1 ELSE 0 END) AS approvedRequests,
            SUM(CASE WHEN status = 'REJECTED' THEN 1 ELSE 0 END) AS rejectedRequests
        FROM requests
        WHERE (:userId IS NULL OR user_id = :userId)
        GROUP BY YEAR(submitted_at), MONTH(submitted_at)
        ORDER BY YEAR(submitted_at), MONTH(submitted_at)
        """, nativeQuery = true)
    List<MonthlyStatsView> findMonthlyRequestStats(
            @Param("userId") Long userId
    );

    interface MonthlyStatsView {
        Integer getYear();
        Integer getMonth();
        Long getTotalRequests();
        Long getPendingRequests();
        Long getApprovedRequests();
        Long getRejectedRequests();
    }
}
