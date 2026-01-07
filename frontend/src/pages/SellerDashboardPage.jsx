import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import apiService from '../services/api';

function SellerDashboardPage() {
  const navigate = useNavigate();
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadDashboard();
  }, []);

  const loadDashboard = async () => {
    try {
      setLoading(true);
      const data = await apiService.getDashboard();
      setDashboard(data);
      setError('');
    } catch (err) {
      if (err.status === 403 || err.status === 404) {
        alert('판매자 등록이 필요합니다.');
        navigate('/seller/register');
      } else {
        setError('대시보드 로딩 실패: ' + err.message);
      }
    } finally {
      setLoading(false);
    }
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat('ko-KR').format(price) + '원';
  };

  const formatNumber = (num) => {
    return new Intl.NumberFormat('ko-KR').format(num);
  };

  if (loading) {
    return <div style={styles.loading}>로딩 중...</div>;
  }

  if (error) {
    return (
      <div style={styles.container}>
        <div style={styles.error}>{error}</div>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <h1 style={styles.title}>판매자 대시보드</h1>
        <button
          onClick={() => navigate('/seller/products')}
          style={styles.manageButton}
        >
          상품 관리
        </button>
      </div>

      {/* 오늘의 통계 */}
      <div style={styles.statsGrid}>
        <div style={styles.statCard}>
          <div style={styles.statIcon}>📦</div>
          <div style={styles.statInfo}>
            <div style={styles.statLabel}>오늘 주문</div>
            <div style={styles.statValue}>
              {formatNumber(dashboard.todayOrders)}건
            </div>
          </div>
        </div>

        <div style={styles.statCard}>
          <div style={styles.statIcon}>💰</div>
          <div style={styles.statInfo}>
            <div style={styles.statLabel}>오늘 매출</div>
            <div style={styles.statValue}>
              {formatPrice(dashboard.todayRevenue)}
            </div>
          </div>
        </div>

        <div style={styles.statCard}>
          <div style={styles.statIcon}>⭐</div>
          <div style={styles.statInfo}>
            <div style={styles.statLabel}>평균 평점</div>
            <div style={styles.statValue}>
              {dashboard.averageRating.toFixed(1)}
            </div>
          </div>
        </div>

        <div style={styles.statCard}>
          <div style={styles.statIcon}>📊</div>
          <div style={styles.statInfo}>
            <div style={styles.statLabel}>총 상품</div>
            <div style={styles.statValue}>
              {formatNumber(dashboard.totalProducts)}개
            </div>
          </div>
        </div>
      </div>

      {/* 베스트셀러 */}
      <div style={styles.section}>
        <h2 style={styles.sectionTitle}>베스트셀러 Top 5</h2>
        {dashboard.bestSellers && dashboard.bestSellers.length > 0 ? (
          <div style={styles.bestSellerList}>
            {dashboard.bestSellers.map((product, index) => (
              <div key={product.id} style={styles.bestSellerCard}>
                <div style={styles.rank}>#{index + 1}</div>
                <div style={styles.productInfo}>
                  <div style={styles.productName}>{product.name}</div>
                  <div style={styles.productMeta}>
                    판매량: {formatNumber(product.salesCount)}개 | 
                    재고: {formatNumber(product.stock)}개
                  </div>
                </div>
                <div style={styles.productPrice}>
                  {formatPrice(product.price)}
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div style={styles.emptyMessage}>
            판매 데이터가 없습니다.
          </div>
        )}
      </div>

      {/* 재고 부족 상품 */}
      {dashboard.lowStockProducts && dashboard.lowStockProducts.length > 0 && (
        <div style={styles.section}>
          <h2 style={styles.sectionTitle}>
            ⚠️ 재고 부족 상품 ({dashboard.lowStockProducts.length})
          </h2>
          <div style={styles.lowStockList}>
            {dashboard.lowStockProducts.map(product => (
              <div key={product.id} style={styles.lowStockCard}>
                <div style={styles.productInfo}>
                  <div style={styles.productName}>{product.name}</div>
                  <div style={styles.productMeta}>
                    현재 재고: <span style={styles.dangerText}>
                      {product.stock}개
                    </span>
                  </div>
                </div>
                <button
                  onClick={() => navigate(`/seller/products/${product.id}/edit`)}
                  style={styles.actionButton}
                >
                  재고 추가
                </button>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* 최근 30일 매출 */}
      {dashboard.last30DaysRevenue && dashboard.last30DaysRevenue.length > 0 && (
        <div style={styles.section}>
          <h2 style={styles.sectionTitle}>최근 30일 매출</h2>
          <div style={styles.chartContainer}>
            {dashboard.last30DaysRevenue.map((item, index) => {
              const maxRevenue = Math.max(
                ...dashboard.last30DaysRevenue.map(d => d.revenue)
              );
              const height = maxRevenue > 0 
                ? (item.revenue / maxRevenue) * 200 
                : 0;
              
              return (
                <div key={index} style={styles.barContainer}>
                  <div
                    style={{
                      ...styles.bar,
                      height: `${height}px`,
                    }}
                    title={`${item.date}: ${formatPrice(item.revenue)}`}
                  />
                  <div style={styles.barLabel}>
                    {new Date(item.date).getDate()}일
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
}

const styles = {
  container: {
    maxWidth: '1200px',
    margin: '0 auto',
    padding: '20px',
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
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '30px',
  },
  title: {
    fontSize: '28px',
    fontWeight: 'bold',
    color: '#333',
  },
  manageButton: {
    padding: '12px 24px',
    fontSize: '16px',
    fontWeight: 'bold',
    color: 'white',
    backgroundColor: '#007bff',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
  },
  statsGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))',
    gap: '20px',
    marginBottom: '40px',
  },
  statCard: {
    display: 'flex',
    alignItems: 'center',
    gap: '15px',
    padding: '20px',
    backgroundColor: 'white',
    borderRadius: '8px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
  },
  statIcon: {
    fontSize: '40px',
  },
  statInfo: {
    flex: 1,
  },
  statLabel: {
    fontSize: '14px',
    color: '#666',
    marginBottom: '5px',
  },
  statValue: {
    fontSize: '24px',
    fontWeight: 'bold',
    color: '#333',
  },
  section: {
    marginBottom: '40px',
  },
  sectionTitle: {
    fontSize: '20px',
    fontWeight: 'bold',
    marginBottom: '20px',
    color: '#333',
  },
  bestSellerList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '10px',
  },
  bestSellerCard: {
    display: 'flex',
    alignItems: 'center',
    gap: '15px',
    padding: '15px',
    backgroundColor: 'white',
    borderRadius: '8px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
  },
  rank: {
    fontSize: '24px',
    fontWeight: 'bold',
    color: '#007bff',
    width: '50px',
    textAlign: 'center',
  },
  productInfo: {
    flex: 1,
  },
  productName: {
    fontSize: '16px',
    fontWeight: 'bold',
    color: '#333',
    marginBottom: '5px',
  },
  productMeta: {
    fontSize: '14px',
    color: '#666',
  },
  productPrice: {
    fontSize: '18px',
    fontWeight: 'bold',
    color: '#007bff',
  },
  emptyMessage: {
    textAlign: 'center',
    padding: '40px',
    color: '#999',
    fontSize: '16px',
  },
  lowStockList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '10px',
  },
  lowStockCard: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: '15px',
    padding: '15px',
    backgroundColor: '#fff3cd',
    borderRadius: '8px',
    border: '1px solid #ffc107',
  },
  dangerText: {
    color: '#dc3545',
    fontWeight: 'bold',
  },
  actionButton: {
    padding: '8px 16px',
    fontSize: '14px',
    fontWeight: 'bold',
    color: 'white',
    backgroundColor: '#28a745',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
  },
  chartContainer: {
    display: 'flex',
    alignItems: 'flex-end',
    gap: '5px',
    height: '250px',
    padding: '20px',
    backgroundColor: 'white',
    borderRadius: '8px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
  },
  barContainer: {
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    gap: '5px',
  },
  bar: {
    width: '100%',
    backgroundColor: '#007bff',
    borderRadius: '4px 4px 0 0',
    minHeight: '2px',
    transition: 'height 0.3s ease',
  },
  barLabel: {
    fontSize: '10px',
    color: '#666',
  },
};

export default SellerDashboardPage;