import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import apiService from '../services/api';

function NotificationPage() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const navigate = useNavigate();

  useEffect(() => {
    loadNotifications();
  }, [page]);

  const loadNotifications = async () => {
    try {
      setLoading(true);
      const data = await apiService.getMyNotifications(page, 20);
      setNotifications(data.content);
      setTotalPages(data.totalPages);
      setError('');
    } catch (err) {
      setError('알림을 불러오는데 실패했습니다: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const markAsRead = async (notificationId) => {
    try {
      await apiService.markNotificationAsRead(notificationId);
      // 목록 새로고침
      loadNotifications();
    } catch (err) {
      console.error('알림 읽음 처리 실패:', err);
    }
  };

  const handleNotificationClick = async (notification) => {
    // 읽지 않은 알림이면 읽음 처리 (백엔드 필드명: isRead)
    if (!notification.isRead) {
      await markAsRead(notification.id);
    }

    // 알림 타입에 따라 다른 페이지로 이동
    switch (notification.type) {
      case 'ORDER_CONFIRMED':
      case 'ORDER_SHIPPED':
      case 'ORDER_DELIVERED':
        // 주문 상세 페이지로 이동 (알림 내용에서 주문 ID 추출 필요)
        // 백엔드 필드명: content
        const orderIdMatch = notification.content.match(/#(\d+)/);
        if (orderIdMatch) {
          navigate(`/orders/${orderIdMatch[1]}`);
        }
        break;
      case 'RESTOCK':
        // 재입고 알림은 상품 상세로 이동
        navigate('/restock');
        break;
      case 'COUPON_ISSUED':
        // 쿠폰 페이지로 이동
        navigate('/coupons');
        break;
      default:
        break;
    }
  };

  const getNotificationIcon = (type) => {
    const icons = {
      ORDER_CONFIRMED: '📦',
      ORDER_SHIPPED: '🚚',
      ORDER_DELIVERED: '✅',
      RESTOCK: '🔄',
      COUPON_ISSUED: '🎟️',
    };
    return icons[type] || '📢';
  };

  const getNotificationTypeLabel = (type) => {
    const labels = {
      ORDER_CONFIRMED: '주문 확정',
      ORDER_SHIPPED: '배송 시작',
      ORDER_DELIVERED: '배송 완료',
      RESTOCK: '재입고 알림',
      COUPON_ISSUED: '쿠폰 발급',
    };
    return labels[type] || '알림';
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    const now = new Date();
    const diff = Math.floor((now - date) / 1000); // 초 단위

    if (diff < 60) return '방금 전';
    if (diff < 3600) return `${Math.floor(diff / 60)}분 전`;
    if (diff < 86400) return `${Math.floor(diff / 3600)}시간 전`;
    if (diff < 604800) return `${Math.floor(diff / 86400)}일 전`;
    
    return date.toLocaleDateString('ko-KR');
  };

  if (loading && page === 0) {
    return <div style={styles.loading}>로딩 중...</div>;
  }

  return (
    <div style={styles.container}>
      <h1 style={styles.title}>알림</h1>

      {error && <div style={styles.error}>{error}</div>}

      {notifications.length === 0 ? (
        <div style={styles.empty}>
          <p style={styles.emptyText}>새로운 알림이 없습니다.</p>
        </div>
      ) : (
        <>
          <div style={styles.notificationList}>
            {notifications.map(notification => (
              <div
                key={notification.id}
                style={{
                  ...styles.notificationCard,
                  ...(notification.isRead ? styles.notificationRead : styles.notificationUnread),
                }}
                onClick={() => handleNotificationClick(notification)}
              >
                <div style={styles.notificationIcon}>
                  {getNotificationIcon(notification.type)}
                </div>

                <div style={styles.notificationContent}>
                  <div style={styles.notificationHeader}>
                    <span style={styles.notificationTypeLabel}>
                      {notification.title}
                    </span>
                    <span style={styles.notificationTime}>
                      {formatDate(notification.createdAt)}
                    </span>
                  </div>

                  <p style={styles.notificationMessage}>
                    {notification.content}
                  </p>

                  {!notification.isRead && (
                    <span style={styles.unreadBadge}>NEW</span>
                  )}
                </div>
              </div>
            ))}
          </div>

          {/* 페이지네이션 */}
          {totalPages > 1 && (
            <div style={styles.pagination}>
              <button
                onClick={() => setPage(p => Math.max(0, p - 1))}
                disabled={page === 0}
                style={{
                  ...styles.pageButton,
                  ...(page === 0 ? styles.pageButtonDisabled : {}),
                }}
              >
                이전
              </button>

              <span style={styles.pageInfo}>
                {page + 1} / {totalPages}
              </span>

              <button
                onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                disabled={page === totalPages - 1}
                style={{
                  ...styles.pageButton,
                  ...(page === totalPages - 1 ? styles.pageButtonDisabled : {}),
                }}
              >
                다음
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}

const styles = {
  container: {
    maxWidth: '800px',
    margin: '0 auto',
    padding: '20px',
  },
  title: {
    fontSize: '28px',
    fontWeight: 'bold',
    marginBottom: '20px',
    color: '#333',
  },
  loading: {
    textAlign: 'center',
    padding: '40px',
    fontSize: '18px',
    color: '#666',
  },
  error: {
    padding: '15px',
    backgroundColor: '#fee',
    color: '#c33',
    borderRadius: '6px',
    marginBottom: '20px',
  },
  empty: {
    textAlign: 'center',
    padding: '60px 20px',
    backgroundColor: 'white',
    borderRadius: '8px',
  },
  emptyText: {
    fontSize: '18px',
    color: '#666',
  },
  notificationList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '10px',
    marginBottom: '20px',
  },
  notificationCard: {
    display: 'flex',
    gap: '15px',
    padding: '20px',
    backgroundColor: 'white',
    borderRadius: '8px',
    boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
    cursor: 'pointer',
    transition: 'box-shadow 0.2s',
  },
  notificationUnread: {
    borderLeft: '4px solid #007bff',
  },
  notificationRead: {
    opacity: 0.7,
  },
  notificationIcon: {
    fontSize: '32px',
    flexShrink: 0,
  },
  notificationContent: {
    flex: 1,
    position: 'relative',
  },
  notificationHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '8px',
  },
  notificationTypeLabel: {
    fontSize: '14px',
    fontWeight: 'bold',
    color: '#007bff',
  },
  notificationTime: {
    fontSize: '12px',
    color: '#999',
  },
  notificationMessage: {
    fontSize: '16px',
    color: '#333',
    lineHeight: '1.5',
    marginBottom: '0',
  },
  unreadBadge: {
    position: 'absolute',
    top: '0',
    right: '0',
    padding: '4px 8px',
    fontSize: '10px',
    fontWeight: 'bold',
    backgroundColor: '#dc3545',
    color: 'white',
    borderRadius: '4px',
  },
  pagination: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    gap: '20px',
    marginTop: '30px',
  },
  pageButton: {
    padding: '10px 20px',
    fontSize: '14px',
    fontWeight: 'bold',
    backgroundColor: '#007bff',
    color: 'white',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
  },
  pageButtonDisabled: {
    backgroundColor: '#ccc',
    cursor: 'not-allowed',
  },
  pageInfo: {
    fontSize: '16px',
    fontWeight: 'bold',
    color: '#333',
  },
};

export default NotificationPage;