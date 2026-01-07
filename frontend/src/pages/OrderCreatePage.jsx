import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import apiService from '../services/api';

function OrderCreatePage() {
  const location = useLocation();
  const navigate = useNavigate();
  
  const [items, setItems] = useState([]);
  const [products, setProducts] = useState({});
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  
  const [formData, setFormData] = useState({
    recipientName: '',
    recipientPhone: '',
    address: ''
  });

  useEffect(() => {
    loadOrderData();
  }, []);

  const loadOrderData = async () => {
    try {
      setLoading(true);
      
      const orderItems = location.state?.items || [];
      if (orderItems.length === 0) {
        alert('주문할 상품이 없습니다.');
        navigate('/products');
        return;
      }

      setItems(orderItems);

      const productPromises = orderItems.map(item => 
        apiService.getProduct(item.productId)
      );
      const productList = await Promise.all(productPromises);
      
      const productMap = {};
      productList.forEach(p => {
        productMap[p.id] = p;
      });
      setProducts(productMap);

      const user = await apiService.getCurrentUser();
      setFormData(prev => ({
        ...prev,
        recipientName: user.name,
        recipientPhone: user.phone || ''
      }));

    } catch (err) {
      setError('주문 정보를 불러오는데 실패했습니다: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const calculateTotal = () => {
    return items.reduce((sum, item) => {
      const product = products[item.productId];
      if (!product) return sum;
      return sum + (product.price * item.quantity);
    }, 0);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!formData.recipientName.trim()) {
      setError('받는 사람 이름을 입력해주세요.');
      return;
    }
    if (!formData.recipientPhone.trim()) {
      setError('받는 사람 연락처를 입력해주세요.');
      return;
    }
    if (!formData.address.trim()) {
      setError('배송 주소를 입력해주세요.');
      return;
    }

    try {
      setSubmitting(true);
      setError('');

      const orderData = {
        ...formData,
        items: items.map(item => ({
          productId: item.productId,
          quantity: item.quantity
        }))
      };

      const result = await apiService.createOrder(orderData);
      
      alert('주문이 완료되었습니다!');
      navigate(`/orders/${result.id}`);

    } catch (err) {
      if (err.code === 'INSUFFICIENT_STOCK') {
        setError('재고가 부족합니다. 수량을 확인해주세요.');
      } else {
        setError('주문 실패: ' + err.message);
      }
    } finally {
      setSubmitting(false);
    }
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat('ko-KR').format(price) + '원';
  };

  if (loading) {
    return <div style={styles.loading}>로딩 중...</div>;
  }

  return (
    <div style={styles.container}>
      <h1 style={styles.title}>주문하기</h1>

      {error && (
        <div style={styles.error}>{error}</div>
      )}

      <div style={styles.content}>
        <div style={styles.section}>
          <h2 style={styles.sectionTitle}>주문 상품</h2>
          <div style={styles.itemList}>
            {items.map((item, index) => {
              const product = products[item.productId];
              if (!product) return null;

              return (
                <div key={index} style={styles.itemCard}>
                  {product.imageUrl && (
                    <img
                      src={product.imageUrl}
                      alt={product.name}
                      style={styles.itemImage}
                    />
                  )}
                  <div style={styles.itemInfo}>
                    <div style={styles.itemName}>{product.name}</div>
                    <div style={styles.itemMeta}>
                      {formatPrice(product.price)} × {item.quantity}개
                    </div>
                  </div>
                  <div style={styles.itemTotal}>
                    {formatPrice(product.price * item.quantity)}
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        <div style={styles.section}>
          <h2 style={styles.sectionTitle}>배송 정보</h2>
          <form onSubmit={handleSubmit} style={styles.form}>
            <div style={styles.formGroup}>
              <label style={styles.label}>받는 사람</label>
              <input
                type="text"
                name="recipientName"
                value={formData.recipientName}
                onChange={handleChange}
                placeholder="이름을 입력하세요"
                style={styles.input}
                required
              />
            </div>

            <div style={styles.formGroup}>
              <label style={styles.label}>연락처</label>
              <input
                type="tel"
                name="recipientPhone"
                value={formData.recipientPhone}
                onChange={handleChange}
                placeholder="010-0000-0000"
                style={styles.input}
                required
              />
            </div>

            <div style={styles.formGroup}>
              <label style={styles.label}>배송 주소</label>
              <textarea
                name="address"
                value={formData.address}
                onChange={handleChange}
                placeholder="배송 받을 주소를 입력하세요"
                style={styles.textarea}
                rows="3"
                required
              />
            </div>
          </form>
        </div>

        <div style={styles.section}>
          <h2 style={styles.sectionTitle}>결제 정보</h2>
          <div style={styles.paymentInfo}>
            <div style={styles.paymentRow}>
              <span>상품 금액</span>
              <span style={styles.paymentValue}>{formatPrice(calculateTotal())}</span>
            </div>
            <div style={styles.paymentRow}>
              <span>배송비</span>
              <span style={styles.paymentValue}>무료</span>
            </div>
            <div style={styles.paymentRowTotal}>
              <span style={styles.totalLabel}>총 결제 금액</span>
              <span style={styles.totalValue}>{formatPrice(calculateTotal())}</span>
            </div>
          </div>
        </div>

        <div style={styles.buttonGroup}>
          <button
            type="button"
            onClick={() => navigate('/products')}
            style={styles.cancelButton}
            disabled={submitting}
          >
            취소
          </button>
          <button
            onClick={handleSubmit}
            style={styles.submitButton}
            disabled={submitting}
          >
            {submitting ? '주문 중...' : `${formatPrice(calculateTotal())} 결제하기`}
          </button>
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
  content: {
    display: 'flex',
    flexDirection: 'column',
    gap: '30px',
  },
  section: {
    backgroundColor: 'white',
    padding: '25px',
    borderRadius: '8px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
  },
  sectionTitle: {
    fontSize: '20px',
    fontWeight: 'bold',
    marginBottom: '20px',
    color: '#333',
  },
  itemList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '15px',
  },
  itemCard: {
    display: 'flex',
    alignItems: 'center',
    gap: '15px',
    padding: '15px',
    backgroundColor: '#f8f9fa',
    borderRadius: '6px',
  },
  itemImage: {
    width: '80px',
    height: '80px',
    objectFit: 'cover',
    borderRadius: '4px',
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
  itemTotal: {
    fontSize: '18px',
    fontWeight: 'bold',
    color: '#007bff',
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: '20px',
  },
  formGroup: {
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
  },
  label: {
    fontSize: '14px',
    fontWeight: 'bold',
    color: '#333',
  },
  input: {
    padding: '12px',
    fontSize: '16px',
    border: '1px solid #ddd',
    borderRadius: '6px',
    outline: 'none',
  },
  textarea: {
    padding: '12px',
    fontSize: '16px',
    border: '1px solid #ddd',
    borderRadius: '6px',
    outline: 'none',
    resize: 'vertical',
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
  buttonGroup: {
    display: 'flex',
    gap: '10px',
    marginTop: '10px',
  },
  cancelButton: {
    flex: 1,
    padding: '16px',
    fontSize: '16px',
    fontWeight: 'bold',
    color: '#666',
    backgroundColor: 'white',
    border: '1px solid #ddd',
    borderRadius: '6px',
    cursor: 'pointer',
  },
  submitButton: {
    flex: 2,
    padding: '16px',
    fontSize: '16px',
    fontWeight: 'bold',
    color: 'white',
    backgroundColor: '#007bff',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
  },
};

export default OrderCreatePage;