import React, { useState, useEffect } from 'react';
import apiService from '../services/api';

function CouponPage() {
  const [coupons, setCoupons] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('AVAILABLE'); // AVAILABLE, USED, EXPIRED

  useEffect(() => {
    loadCoupons();
  }, []);

  const loadCoupons = async () => {
    try {
      setLoading(true);
      const data = await apiService.getMyCoupons(0, 100);
      setCoupons(data.content);
      setError('');
    } catch (err) {
      setError('쿠폰을 불러오는데 실패했습니다: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat('ko-KR').format(price) + '원';
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('ko-KR');
  };

  const getCouponTypeLabel = (type) => {
    const labels = {
      WELCOME: '웰컴 쿠폰',
      BIRTHDAY: '생일 축하 쿠폰',
      VIP: 'VIP 쿠폰',
    };
    return labels[type] || type;
  };

  const getDiscountText = (coupon) => {
    if (coupon.discountType === 'PERCENTAGE') {
      return `${coupon.discountValue}% 할인`;
    } else {
      return `${formatPrice(coupon.discountValue)} 할인`;
    }
  };

  const isExpired = (userCoupon) => {
    return new Date(userCoupon.expiresAt) < new Date();
  };

  const filteredCoupons = coupons.filter(userCoupon => {
    if (filter === 'AVAILABLE') {
      return !userCoupon.isUsed && !isExpired(userCoupon);
    } else if (filter === 'USED') {
      return userCoupon.isUsed;
    } else if (filter === 'EXPIRED') {
      return !userCoupon.isUsed && isExpired(userCoupon);
    }
    return true;
  });

  if (loading) {
    return <div style={styles.loading}>로딩 중...</div>;
  }

  return (
    <div style={styles.container}>
      <h1 style={styles.title}>내 쿠폰</h1>

      {error && <div style={styles.error}>{error}</div>}

      {/* 필터 탭 */}
      <div style={styles.filterTabs}>
        <button
          onClick={() => setFilter('AVAILABLE')}
          style={{
            ...styles.filterTab,
            ...(filter === 'AVAILABLE' ? styles.filterTabActive : {}),
          }}
        >
          사용 가능 ({coupons.filter(c => !c.isUsed && !isExpired(c)).length})
        </button>
        <button
          onClick={() => setFilter('USED')}
          style={{
            ...styles.filterTab,
            ...(filter === 'USED' ? styles.filterTabActive : {}),
          }}
        >
          사용 완료 ({coupons.filter(c => c.isUsed).length})
        </button>
        <button
          onClick={() => setFilter('EXPIRED')}
          style={{
            ...styles.filterTab,
            ...(filter === 'EXPIRED' ? styles.filterTabActive : {}),
          }}
        >
          기간 만료 ({coupons.filter(c => !c.isUsed && isExpired(c)).length})
        </button>
      </div>

      {/* 쿠폰 목록 */}
      <div style={styles.couponList}>
        {filteredCoupons.length === 0 ? (
          <div style={styles.empty}>
            {filter === 'AVAILABLE' && '사용 가능한 쿠폰이 없습니다.'}
            {filter === 'USED' && '사용한 쿠폰이 없습니다.'}
            {filter === 'EXPIRED' && '만료된 쿠폰이 없습니다.'}
          </div>
        ) : (
          filteredCoupons.map(userCoupon => (
            <div
              key={userCoupon.id}
              style={{
                ...styles.couponCard,
                ...(userCoupon.isUsed || isExpired(userCoupon) ? styles.couponCardDisabled : {}),
              }}
            >
              <div style={styles.couponLeft}>
                <div style={styles.discountBadge}>
                  {getDiscountText(userCoupon.coupon)}
                </div>
              </div>

              <div style={styles.couponRight}>
                <div style={styles.couponHeader}>
                  <h3 style={styles.couponName}>{userCoupon.coupon.name}</h3>
                  <span style={styles.couponType}>
                    {getCouponTypeLabel(userCoupon.coupon.type)}
                  </span>
                </div>

                <p style={styles.couponCode}>코드: {userCoupon.coupon.code}</p>

                <div style={styles.couponInfo}>
                  <div style={styles.infoRow}>
                    <span style={styles.infoLabel}>최소 주문금액:</span>
                    <span style={styles.infoValue}>
                      {formatPrice(userCoupon.coupon.minOrderAmount)}
                    </span>
                  </div>
                  <div style={styles.infoRow}>
                    <span style={styles.infoLabel}>유효기간:</span>
                    <span style={styles.infoValue}>
                      {formatDate(userCoupon.coupon.validFrom)} ~ {formatDate(userCoupon.coupon.validUntil)}
                    </span>
                  </div>
                  <div style={styles.infoRow}>
                    <span style={styles.infoLabel}>발급일:</span>
                    <span style={styles.infoValue}>
                      {formatDate(userCoupon.issuedAt)}
                    </span>
                  </div>
                </div>

                {userCoupon.isUsed && (
                  <div style={styles.usedBadge}>사용 완료</div>
                )}
                {!userCoupon.isUsed && isExpired(userCoupon) && (
                  <div style={styles.expiredBadge}>기간 만료</div>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

const styles = {
  container: {
    maxWidth: '1200px',
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
  filterTabs: {
    display: 'flex',
    gap: '10px',
    marginBottom: '20px',
  },
  filterTab: {
    padding: '12px 24px',
    fontSize: '16px',
    fontWeight: '500',
    color: '#666',
    backgroundColor: 'white',
    border: '1px solid #ddd',
    borderRadius: '6px',
    cursor: 'pointer',
  },
  filterTabActive: {
    color: '#007bff',
    backgroundColor: '#e7f3ff',
    borderColor: '#007bff',
    fontWeight: 'bold',
  },
  couponList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '15px',
  },
  empty: {
    textAlign: 'center',
    padding: '60px 20px',
    backgroundColor: 'white',
    borderRadius: '8px',
    color: '#666',
    fontSize: '16px',
  },
  couponCard: {
    display: 'flex',
    backgroundColor: 'white',
    borderRadius: '12px',
    overflow: 'hidden',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
    border: '2px solid #007bff',
  },
  couponCardDisabled: {
    opacity: 0.5,
    borderColor: '#ccc',
  },
  couponLeft: {
    width: '180px',
    backgroundColor: '#007bff',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    padding: '20px',
  },
  discountBadge: {
    fontSize: '24px',
    fontWeight: 'bold',
    color: 'white',
    textAlign: 'center',
  },
  couponRight: {
    flex: 1,
    padding: '20px',
    position: 'relative',
  },
  couponHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: '10px',
  },
  couponName: {
    fontSize: '20px',
    fontWeight: 'bold',
    color: '#333',
  },
  couponType: {
    padding: '4px 12px',
    fontSize: '12px',
    fontWeight: 'bold',
    backgroundColor: '#ffc107',
    color: 'white',
    borderRadius: '12px',
  },
  couponCode: {
    fontSize: '14px',
    color: '#999',
    marginBottom: '15px',
    fontFamily: 'monospace',
  },
  couponInfo: {
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
  },
  infoRow: {
    display: 'flex',
    justifyContent: 'space-between',
    fontSize: '14px',
  },
  infoLabel: {
    color: '#666',
  },
  infoValue: {
    fontWeight: 'bold',
    color: '#333',
  },
  usedBadge: {
    position: 'absolute',
    top: '20px',
    right: '20px',
    padding: '6px 12px',
    fontSize: '14px',
    fontWeight: 'bold',
    backgroundColor: '#6c757d',
    color: 'white',
    borderRadius: '6px',
  },
  expiredBadge: {
    position: 'absolute',
    top: '20px',
    right: '20px',
    padding: '6px 12px',
    fontSize: '14px',
    fontWeight: 'bold',
    backgroundColor: '#dc3545',
    color: 'white',
    borderRadius: '6px',
  },
};

export default CouponPage;