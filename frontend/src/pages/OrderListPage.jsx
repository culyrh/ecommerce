import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import apiService from '../services/api';

function OrderListPage() {
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    loadOrders();
  }, [page]);

  const loadOrders = async () => {
    try {
      setLoading(true);
      const data = await apiService.getMyOrders(page, 10);
      setOrders(data.content);
      setTotalPages(data.totalPages);
      setError('');
    } catch (err) {
      setError('주문 목록을 불러오는데 실패했습니다: ' + err.message);
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

  if (loading && page === 0) {
    return <div style={styles.loading}>로딩 중...</div>;
  }

  return (
    <div style={styles.container}>
      <h1 style={styles.title}>주문 내역</h1>

      {error && (
        <div style={styles.error}>{error}</div>
      )}

      {orders.length === 0 ? (
        <div style={styles.empty}>
          <p>주문 내역이 없습니다.</p>
          <button
            onClick={() => navigate('/products')}
            style={styles.shopButton}
          >
            쇼핑하러 가기
          </button>
        </div>
      ) : (
        <>
          <div style={styles.orderList}>
            {orders.map(order => (
              <div
                key={order.id}
                style={styles.orderCard}
                onClick={() => navigate(`/orders/${order.id}`)}
              >
                <div style={styles.orderHeader}>
                  <div>
                    <div style={styles.orderNumber}>주문번호: {order.orderNumber}</div>
                    <div style={styles.orderDate}>
                      {new Date(order.createdAt).toLocaleString()}
                    </div>
                  </div>
                  <div
                    style={{
                      ...styles.statusBadge,
                      backgroundColor: getStatusColor(order.status)
                    }}
                  >
                    {getStatusText(order.status)}
                  </div>
                </div>

                <div style={styles.orderItems}>
                  {order.orderItems.slice(0, 2).map((item, idx) => (
                    <div key={idx} style={styles.orderItem}>
                      <span>{item.productName}</span>
                      <span style={styles.orderItemQty}>
                        {item.quantity}개
                      </span>
                    </div>
                  ))}
                  {order.orderItems.length > 2 && (
                    <div style={styles.moreItems}>
                      외 {order.orderItems.length - 2}개
                    </div>
                  )}
                </div>

                <div style={styles.orderFooter}>
                  <span style={styles.totalLabel}>총 결제금액</span>
                  <span style={styles.totalAmount}>
                    {formatPrice(order.totalAmount)}
                  </span>
                </div>
              </div>
            ))}
          </div>

          {totalPages > 1 && (
            <div style={styles.pagination}>
              <button
                onClick={() => setPage(p => Math.max(0, p - 1))}
                disabled={page === 0}
                style={{
                  ...styles.pageButton,
                  ...(page === 0 ? styles.pageButtonDisabled : {})
                }}
              >
                이전
              </button>
              <span style={styles.pageInfo}>{page + 1} / {totalPages}</span>
              <button
                onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                disabled={page === totalPages - 1}
                style={{
                  ...styles.pageButton,
                  ...(page === totalPages - 1 ? styles.pageButtonDisabled : {})
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
  loading: {
    textAlign: 'center',
    padding: '40px',
    fontSize: '18px',
    color: '#666',
  },
  title: {
    fontSize: '28px',
    fontWeight: 'bold',
    marginBottom: '30px',
    color: '#333',
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
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
  },
  shopButton: {
    marginTop: '20px',
    padding: '12px 30px',
    fontSize: '16px',
    fontWeight: 'bold',
    color: 'white',
    backgroundColor: '#007bff',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
  },
  orderList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '20px',
  },
  orderCard: {
    backgroundColor: 'white',
    padding: '20px',
    borderRadius: '8px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
    cursor: 'pointer',
    transition: 'transform 0.2s, box-shadow 0.2s',
  },
  orderHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    marginBottom: '15px',
    paddingBottom: '15px',
    borderBottom: '1px solid #e9ecef',
  },
  orderNumber: {
    fontSize: '16px',
    fontWeight: 'bold',
    color: '#333',
    marginBottom: '5px',
  },
  orderDate: {
    fontSize: '14px',
    color: '#666',
  },
  statusBadge: {
    padding: '6px 12px',
    fontSize: '14px',
    fontWeight: 'bold',
    color: 'white',
    borderRadius: '4px',
  },
  orderItems: {
    marginBottom: '15px',
  },
  orderItem: {
    display: 'flex',
    justifyContent: 'space-between',
    padding: '8px 0',
    fontSize: '14px',
    color: '#666',
  },
  orderItemQty: {
    fontWeight: 'bold',
    color: '#333',
  },
  moreItems: {
    fontSize: '14px',
    color: '#999',
    marginTop: '5px',
  },
  orderFooter: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingTop: '15px',
    borderTop: '1px solid #e9ecef',
  },
  totalLabel: {
    fontSize: '16px',
    fontWeight: 'bold',
    color: '#666',
  },
  totalAmount: {
    fontSize: '20px',
    fontWeight: 'bold',
    color: '#007bff',
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

export default OrderListPage;