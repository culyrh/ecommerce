import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import apiService from '../services/api';

function RestockPage() {
  const [activeTab, setActiveTab] = useState('notifications'); // notifications, votes
  const [notifications, setNotifications] = useState([]);
  const [votes, setVotes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    loadData();
  }, [activeTab]);

  const loadData = async () => {
    try {
      setLoading(true);
      if (activeTab === 'notifications') {
        const data = await apiService.getMyRestockNotifications(0, 100);
        setNotifications(data.content);
      } else {
        const data = await apiService.getMyRestockVotes(0, 100);
        setVotes(data.content);
      }
      setError('');
    } catch (err) {
      setError('데이터를 불러오는데 실패했습니다: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('ko-KR');
  };

  const renderNotifications = () => {
    if (notifications.length === 0) {
      return (
        <div style={styles.empty}>
          <p style={styles.emptyText}>신청한 재입고 알림이 없습니다.</p>
          <button onClick={() => navigate('/products')} style={styles.goButton}>
            상품 둘러보기
          </button>
        </div>
      );
    }

    return (
      <div style={styles.list}>
        {notifications.map(notification => (
          <div key={notification.id} style={styles.card}>
            <div style={styles.cardContent}>
              <h3 
                style={styles.productName}
                onClick={() => navigate(`/products/${notification.productId}`)}
              >
                {notification.productName}
              </h3>
              
              <div style={styles.info}>
                <span style={styles.infoLabel}>신청일:</span>
                <span style={styles.infoValue}>
                  {formatDate(notification.createdAt)}
                </span>
              </div>

              {notification.notified ? (
                <div style={styles.statusBadge}>
                  <span style={styles.statusNotified}>✅ 알림 발송됨</span>
                  {notification.notifiedAt && (
                    <span style={styles.statusDate}>
                      ({formatDate(notification.notifiedAt)})
                    </span>
                  )}
                </div>
              ) : (
                <div style={styles.statusBadge}>
                  <span style={styles.statusPending}>⏳ 대기 중</span>
                </div>
              )}
            </div>

            <button
              onClick={() => navigate(`/products/${notification.productId}`)}
              style={styles.viewButton}
            >
              상품 보기
            </button>
          </div>
        ))}
      </div>
    );
  };

  const renderVotes = () => {
    if (votes.length === 0) {
      return (
        <div style={styles.empty}>
          <p style={styles.emptyText}>투표한 재입고 요청이 없습니다.</p>
          <button onClick={() => navigate('/products')} style={styles.goButton}>
            상품 둘러보기
          </button>
        </div>
      );
    }

    return (
      <div style={styles.list}>
        {votes.map(vote => (
          <div key={vote.id} style={styles.card}>
            <div style={styles.cardContent}>
              <div style={styles.cardHeader}>
                <h3 
                  style={styles.productName}
                  onClick={() => navigate(`/products/${vote.productId}`)}
                >
                  {vote.productName}
                </h3>
                <span style={styles.voteCount}>
                  🗳️ {vote.voteCount || 0}표
                </span>
              </div>
              
              <div style={styles.info}>
                <span style={styles.infoLabel}>투표일:</span>
                <span style={styles.infoValue}>
                  {formatDate(vote.createdAt)}
                </span>
              </div>

              <p style={styles.voteDescription}>
                이 상품의 재입고를 원하는 사용자가 {vote.voteCount || 0}명 입니다.
              </p>
            </div>

            <button
              onClick={() => navigate(`/products/${vote.productId}`)}
              style={styles.viewButton}
            >
              상품 보기
            </button>
          </div>
        ))}
      </div>
    );
  };

  return (
    <div style={styles.container}>
      <h1 style={styles.title}>재입고 관리</h1>

      {error && <div style={styles.error}>{error}</div>}

      {/* 탭 */}
      <div style={styles.tabs}>
        <button
          onClick={() => setActiveTab('notifications')}
          style={{
            ...styles.tab,
            ...(activeTab === 'notifications' ? styles.tabActive : {}),
          }}
        >
          재입고 알림
        </button>
        <button
          onClick={() => setActiveTab('votes')}
          style={{
            ...styles.tab,
            ...(activeTab === 'votes' ? styles.tabActive : {}),
          }}
        >
          재입고 투표
        </button>
      </div>

      {/* 설명 */}
      <div style={styles.description}>
        {activeTab === 'notifications' ? (
          <p>
            품절된 상품의 재입고 알림을 신청하면, 상품이 재입고될 때 알림을 받을 수 있습니다.
          </p>
        ) : (
          <p>
            재입고를 원하는 상품에 투표하면, 판매자가 재입고 우선순위를 결정하는데 도움이 됩니다.
          </p>
        )}
      </div>

      {/* 내용 */}
      {loading ? (
        <div style={styles.loading}>로딩 중...</div>
      ) : (
        activeTab === 'notifications' ? renderNotifications() : renderVotes()
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
  tabs: {
    display: 'flex',
    gap: '10px',
    marginBottom: '20px',
  },
  tab: {
    flex: 1,
    padding: '12px 24px',
    fontSize: '16px',
    fontWeight: '500',
    color: '#666',
    backgroundColor: 'white',
    border: '1px solid #ddd',
    borderRadius: '6px',
    cursor: 'pointer',
  },
  tabActive: {
    color: '#007bff',
    backgroundColor: '#e7f3ff',
    borderColor: '#007bff',
    fontWeight: 'bold',
  },
  description: {
    padding: '15px',
    backgroundColor: '#f8f9fa',
    borderRadius: '6px',
    marginBottom: '20px',
    fontSize: '14px',
    color: '#666',
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
    marginBottom: '20px',
  },
  goButton: {
    padding: '12px 30px',
    fontSize: '16px',
    fontWeight: 'bold',
    color: 'white',
    backgroundColor: '#007bff',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
  },
  list: {
    display: 'flex',
    flexDirection: 'column',
    gap: '15px',
  },
  card: {
    backgroundColor: 'white',
    borderRadius: '8px',
    padding: '20px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: '20px',
  },
  cardContent: {
    flex: 1,
  },
  cardHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '10px',
  },
  productName: {
    fontSize: '18px',
    fontWeight: 'bold',
    color: '#007bff',
    cursor: 'pointer',
    margin: 0,
  },
  voteCount: {
    fontSize: '16px',
    fontWeight: 'bold',
    color: '#28a745',
  },
  info: {
    display: 'flex',
    gap: '10px',
    marginBottom: '10px',
    fontSize: '14px',
  },
  infoLabel: {
    color: '#666',
  },
  infoValue: {
    color: '#333',
    fontWeight: '500',
  },
  statusBadge: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
    marginTop: '10px',
  },
  statusNotified: {
    fontSize: '14px',
    fontWeight: 'bold',
    color: '#28a745',
  },
  statusPending: {
    fontSize: '14px',
    fontWeight: 'bold',
    color: '#ffc107',
  },
  statusDate: {
    fontSize: '12px',
    color: '#999',
  },
  voteDescription: {
    fontSize: '14px',
    color: '#666',
    margin: '10px 0 0 0',
  },
  viewButton: {
    padding: '10px 20px',
    fontSize: '14px',
    fontWeight: 'bold',
    color: 'white',
    backgroundColor: '#007bff',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
    whiteSpace: 'nowrap',
  },
};

export default RestockPage;