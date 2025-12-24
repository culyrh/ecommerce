package ecommerce.domain.notification.repository;

import ecommerce.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUserId(Long userId, Pageable pageable);

    long countByUserIdAndIsReadFalse(Long userId);

    // 읽지 않은 알림 조회 (페이징)
    Page<Notification> findByUserIdAndIsReadFalse(Long userId, Pageable pageable);

    // 읽지 않은 알림 목록 (모두 읽음 처리용)
    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.isRead = false")
    List<Notification> findUnreadByUserId(@Param("userId") Long userId);
}