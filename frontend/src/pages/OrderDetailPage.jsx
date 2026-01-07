import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import apiService from '../services/api';

function OrderDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadOrder();
  }, [id]);

  const loadOrder = async () => {
    try {
      setLoading(true);
      const data = await apiService.getOrder(id);
      setOrder(data);
      setError('');
    } catch (err) {
      setError('주문 정보를 불러오는데 실패했습니다: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat('ko-KR').format(price) + '원';
  };

  const getStatusText = (status) => {
    const statusMap = {
      PENDING: '주문 접수',
      CONFIRMED: '주문 확인',
      PREPARING: '배송 준비',
      SHIPPED: '배송 중',
      DELIVERED: '배송 완료',
      CANCELLED: '주문 취소'
    };
    return statusMap[status] || status;
  };

  const getStatusColor = (status) => {
    const colorMap = {
      PENDING: '#ffc107',
      CONFIRMED: '#17a2b8',
      PREPARING: '#007bff',
      SHIPPED: '#6f42c1',
      DELIVERED: '#28a745',
      CANCELLED: '#dc3545'
    };
    return colorMap[status] || '#6c757d';
  };

  if (loading) {
    return <div style={styles.loading}>로딩 중...</div>;
  }

  if (error || !order) {
    return (
      <div style={styles.container}>
        <div style={styles.error}>{error || '주문을 찾을 수 없습니다.'}</div>
        <button onClick={() => navigate('/orders')} style={styles.backButton}>
          ← 주문 목록
        </button>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <button onClick={() => navigate('/orders')} style={styles.backButton}>
        ← 주문 목록
      </button>

      <h1 style={styles.title}>주문 상세</h1>

      <div style={styles.section}>
        <div style={styles.sectionHeader}>
          <h2 style={styles.sectionTitle}>주문 정보</h2>
          <div
            style={{
              ...styles.statusBadge,
              backgroundColor: getStatusColor(order.status)
            }}
          >
            {getStatusText(order.status)}
          </div>
        </div>
        <div style={styles.infoGrid}>
          <div style={styles.infoRow}>
            <span style={styles.infoLabel}>주문번호</span>
            <span style={styles.infoValue}>{order.orderNumber}</span>
          </div>
          <div style={styles.infoRow}>
            <span style={styles.infoLabel}>주문일시</span>
            <span style={styles.infoValue}>
              {new Date(order.createdAt).toLocaleString()}
            </span>
          </div>
        </div>
      </div>

      <div style={styles.section}>
        <h2 style={styles.sectionTitle}>배송 정보</h2>
        <div style={styles.infoGrid}>
          <div style={styles.infoRow}>
            <span style={styles.infoLabel}>받는 사람</span>
            <span style={styles.infoValue}>{order.recipientName}</span>
          </div>
          <div style={styles.infoRow}>
            <span style={styles.infoLabel}>연락처</span>
            <span style={styles.infoValue}>{order.recipientPhone}</span>
          </div>
          <div style={styles.infoRow}>
            <span style={styles.infoLabel}>배송 주소</span>
            <span style={styles.infoValue}>{order.address}</span>
          </div>
        </div>
      </div>

      <div style={styles.section}>
        <h2 style={styles.sectionTitle}>주문 상품 ({order.orderItems.length})</h2>
        <div style={styles.itemList}>
          {order.orderItems.map((item, idx) => (
            <div key={idx} style={styles.itemCard}>
              <div style={styles.itemInfo}>
                <div style={styles.itemName}>{item.productName}</div>
                <div style={styles.itemMeta}>
                  {formatPrice(item.price)} × {item.quantity}개
                </div>
              </div>
              <div style={styles.itemSubtotal}>
                {formatPrice(item.subtotal)}
              </div>
            </div>
          ))}
        </div>
      </div>

      <div style={styles.section}>
        <h2 style={styles.sectionTitle}>결제 정보</h2>
        <div style={styles.paymentInfo}>
          <div style={styles.paymentRow}>
            <span>상품 금액</span>
            <span style={styles.paymentValue}>{formatPrice(order.totalAmount)}</span>
          </div>
          <div style={styles.paymentRow}>
            <span>배송비</span>
            <span style={styles.paymentValue}>무료</span>
          </div>
          {order.couponDiscount > 0 && (
            <div style={styles.paymentRow}>
              <span>쿠폰 할인</span>
              <span style={styles.discountValue}>-{formatPrice(order.couponDiscount)}</span>
            </div>
          )}
          <div style={styles.paymentRowTotal}>
            <span style={styles.totalLabel}>총 결제 금액</span>
            <span style={styles.totalValue}>{formatPrice(order.finalAmount)}</span>
          </div>
        </div>
      </div>
    </div>
  );
}

const styles = {
  container: {
    maxWidth: '800px',
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
  backButton: {
    padding: '10px 20px',
    fontSize: '14px',
    fontWeight: 'bold',
    color: '#007bff',
    backgroundColor: 'white',
    border: '1px solid #007bff',
    borderRadius: '6px',
    cursor: 'pointer',
    marginBottom: '20px',
  },
  title: {
    fontSize: '28px',
    fontWeight: 'bold',
    marginBottom: '30px',
    color: '#333',
  },
  section: {
    backgroundColor: 'white',
    padding: '25px',
    borderRadius: '8px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
    marginBottom: '20px',
  },
  sectionHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '20px',
  },
  sectionTitle: {
    fontSize: '20px',
    fontWeight: 'bold',
    marginBottom: '20px',
    color: '#333',
  },
  statusBadge: {
    padding: '8px 16px',
    fontSize: '14px',
    fontWeight: 'bold',
    color: 'white',
    borderRadius: '4px',
  },
  infoGrid: {
    display: 'flex',
    flexDirection: 'column',
    gap: '15px',
  },
  infoRow: {
    display: 'flex',
    paddingBottom: '15px',
    borderBottom: '1px solid #e9ecef',
  },
  infoLabel: {
    width: '150px',
    fontWeight: 'bold',
    color: '#666',
  },
  infoValue: {
    flex: 1,
    color: '#333',
  },
  itemList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '15px',
  },
  itemCard: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: '15px',
    backgroundColor: '#f8f9fa',
    borderRadius: '6px',
  },
  itemInfo: {
    flex: 1,
  },
  itemName: {
    fontSize: '16px',
    fontWeight: 'bold',
    marginBottom: '5px',
    color: '#333',
  },
  itemMeta: {
    fontSize: '14px',
    color: '#666',
  },
  itemSubtotal: {
    fontSize: '18px',
    fontWeight: 'bold',
    color: '#007bff',
  },
  paymentInfo: {
    display: 'flex',
    flexDirection: 'column',
    gap: '15px',
  },
  paymentRow: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    fontSize: '16px',
    color: '#666',
  },
  paymentValue: {
    fontWeight: 'bold',
    color: '#333',
  },
  discountValue: {
    fontWeight: 'bold',
    color: '#dc3545',
  },
  paymentRowTotal: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingTop: '15px',
    borderTop: '2px solid #e9ecef',
    marginTop: '10px',
  },
  totalLabel: {
    fontSize: '18px',
    fontWeight: 'bold',
    color: '#333',
  },
  totalValue: {
    fontSize: '24px',
    fontWeight: 'bold',
    color: '#007bff',
  },
};

export default OrderDetailPage;