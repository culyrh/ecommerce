import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import apiService from '../services/api';

function CartPage() {
  const [cartItems, setCartItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    loadCartItems();
  }, []);

  const loadCartItems = async () => {
    try {
      setLoading(true);
      const data = await apiService.getMyCart();
      setCartItems(data || []);
      setError('');
    } catch (err) {
      console.error('장바구니 로딩 실패:', err);
      if (err.status === 401) {
        navigate('/login');
      } else {
        setError('장바구니를 불러올 수 없습니다.');
        setCartItems([]);
      }
    } finally {
      setLoading(false);
    }
  };

  const updateQuantity = async (cartItemId, newQuantity) => {
    if (newQuantity < 1) return;
    
    try {
      await apiService.updateCartItem(cartItemId, newQuantity);
      loadCartItems(); // 새로고침
    } catch (err) {
      alert('수량 변경 실패: ' + err.message);
    }
  };

  const removeItem = async (cartItemId) => {
    try {
      await apiService.removeCartItem(cartItemId);
      loadCartItems(); // 새로고침
    } catch (err) {
      alert('삭제 실패: ' + err.message);
    }
  };

  const clearCart = async () => {
    if (!window.confirm('장바구니를 비우시겠습니까?')) {
      return;
    }

    try {
      await apiService.clearCart();
      setCartItems([]);
    } catch (err) {
      alert('장바구니 비우기 실패: ' + err.message);
    }
  };

  const handleCheckout = () => {
    if (cartItems.length === 0) {
      alert('장바구니가 비어있습니다.');
      return;
    }
    
    // 주문 페이지로 이동 (장바구니 정보 포함)
    navigate('/orders/create', { 
      state: { 
        items: cartItems.map(item => ({
          productId: item.productId,
          quantity: item.quantity,
        }))
      }
    });
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat('ko-KR').format(price) + '원';
  };

  const getTotalPrice = () => {
    return cartItems.reduce((sum, item) => {
      return sum + Number(item.subtotal || 0);
    }, 0);
  };

  if (loading) {
    return <div style={styles.loading}>로딩 중...</div>;
  }

  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <h1 style={styles.title}>장바구니</h1>
        {cartItems.length > 0 && (
          <button onClick={clearCart} style={styles.clearButton}>
            전체 삭제
          </button>
        )}
      </div>

      {error && <div style={styles.error}>{error}</div>}

      {cartItems.length === 0 ? (
        <div style={styles.empty}>
          <p style={styles.emptyText}>장바구니가 비어있습니다.</p>
          <button onClick={() => navigate('/products')} style={styles.shopButton}>
            쇼핑 계속하기
          </button>
        </div>
      ) : (
        <>
          <div style={styles.itemsList}>
            {cartItems.map(item => (
              <div key={item.id} style={styles.cartItem}>
                <img
                  src={item.productImageUrl || '/placeholder.png'}
                  alt={item.productName}
                  style={styles.itemImage}
                  onClick={() => navigate(`/products/${item.productId}`)}
                />
                
                <div style={styles.itemInfo}>
                  <h3 
                    style={styles.itemName}
                    onClick={() => navigate(`/products/${item.productId}`)}
                  >
                    {item.productName}
                  </h3>
                  <p style={styles.itemPrice}>
                    {formatPrice(item.productPrice)}
                  </p>
                  
                  {item.productStock < item.quantity && (
                    <p style={styles.stockWarning}>
                      ⚠️ 재고가 부족합니다 (현재 재고: {item.productStock}개)
                    </p>
                  )}

                  {item.productStatus !== 'ACTIVE' && (
                    <p style={styles.stockWarning}>
                      ⚠️ 품절된 상품입니다
                    </p>
                  )}
                </div>

                <div style={styles.quantityControl}>
                  <button
                    onClick={() => updateQuantity(item.id, item.quantity - 1)}
                    style={styles.quantityButton}
                    disabled={item.quantity <= 1}
                  >
                    -
                  </button>
                  <span style={styles.quantity}>{item.quantity}</span>
                  <button
                    onClick={() => updateQuantity(item.id, item.quantity + 1)}
                    style={styles.quantityButton}
                    disabled={item.quantity >= item.productStock}
                  >
                    +
                  </button>
                </div>

                <div style={styles.itemTotal}>
                  <p style={styles.totalPrice}>
                    {formatPrice(item.subtotal)}
                  </p>
                  <button
                    onClick={() => removeItem(item.id)}
                    style={styles.removeButton}
                  >
                    삭제
                  </button>
                </div>
              </div>
            ))}
          </div>

          <div style={styles.summary}>
            <div style={styles.summaryRow}>
              <span style={styles.summaryLabel}>총 상품 금액:</span>
              <span style={styles.summaryValue}>{formatPrice(getTotalPrice())}</span>
            </div>
            <div style={styles.summaryRow}>
              <span style={styles.summaryLabel}>배송비:</span>
              <span style={styles.summaryValue}>무료</span>
            </div>
            <div style={styles.summaryTotal}>
              <span style={styles.totalLabel}>총 결제 금액:</span>
              <span style={styles.totalValue}>{formatPrice(getTotalPrice())}</span>
            </div>
            
            <button onClick={handleCheckout} style={styles.checkoutButton}>
              주문하기
            </button>
          </div>
        </>
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
  clearButton: {
    padding: '10px 20px',
    fontSize: '14px',
    color: '#dc3545',
    backgroundColor: 'white',
    border: '1px solid #dc3545',
    borderRadius: '6px',
    cursor: 'pointer',
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
    marginBottom: '20px',
  },
  shopButton: {
    padding: '12px 30px',
    fontSize: '16px',
    fontWeight: 'bold',
    color: 'white',
    backgroundColor: '#007bff',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
  },
  itemsList: {
    backgroundColor: 'white',
    borderRadius: '8px',
    padding: '20px',
    marginBottom: '20px',
  },
  cartItem: {
    display: 'flex',
    gap: '20px',
    padding: '20px 0',
    borderBottom: '1px solid #e9ecef',
  },
  itemImage: {
    width: '120px',
    height: '120px',
    objectFit: 'cover',
    borderRadius: '8px',
    cursor: 'pointer',
  },
  itemInfo: {
    flex: 1,
  },
  itemName: {
    fontSize: '18px',
    fontWeight: 'bold',
    color: '#333',
    marginBottom: '8px',
    cursor: 'pointer',
  },
  itemPrice: {
    fontSize: '16px',
    color: '#666',
    marginBottom: '8px',
  },
  stockWarning: {
    fontSize: '14px',
    color: '#dc3545',
    marginTop: '8px',
  },
  quantityControl: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
  },
  quantityButton: {
    width: '32px',
    height: '32px',
    fontSize: '18px',
    fontWeight: 'bold',
    color: '#333',
    backgroundColor: 'white',
    border: '1px solid #ddd',
    borderRadius: '4px',
    cursor: 'pointer',
  },
  quantity: {
    fontSize: '16px',
    fontWeight: 'bold',
    minWidth: '40px',
    textAlign: 'center',
  },
  itemTotal: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'flex-end',
    gap: '10px',
  },
  totalPrice: {
    fontSize: '20px',
    fontWeight: 'bold',
    color: '#007bff',
  },
  removeButton: {
    padding: '8px 16px',
    fontSize: '14px',
    color: '#dc3545',
    backgroundColor: 'white',
    border: '1px solid #dc3545',
    borderRadius: '6px',
    cursor: 'pointer',
  },
  summary: {
    backgroundColor: 'white',
    borderRadius: '8px',
    padding: '30px',
  },
  summaryRow: {
    display: 'flex',
    justifyContent: 'space-between',
    padding: '10px 0',
    fontSize: '16px',
  },
  summaryLabel: {
    color: '#666',
  },
  summaryValue: {
    fontWeight: 'bold',
    color: '#333',
  },
  summaryTotal: {
    display: 'flex',
    justifyContent: 'space-between',
    padding: '20px 0',
    marginTop: '10px',
    borderTop: '2px solid #e9ecef',
  },
  totalLabel: {
    fontSize: '20px',
    fontWeight: 'bold',
    color: '#333',
  },
  totalValue: {
    fontSize: '24px',
    fontWeight: 'bold',
    color: '#007bff',
  },
  checkoutButton: {
    width: '100%',
    padding: '15px',
    fontSize: '18px',
    fontWeight: 'bold',
    color: 'white',
    backgroundColor: '#007bff',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
    marginTop: '20px',
  },
};

export default CartPage;