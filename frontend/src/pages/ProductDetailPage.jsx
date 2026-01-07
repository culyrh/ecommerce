import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import apiService from '../services/api';

function ProductDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  
  const [product, setProduct] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [quantity, setQuantity] = useState(1);
  const [user, setUser] = useState(null);

  useEffect(() => {
    loadProduct();
    loadReviews();
    checkUser();
  }, [id]);

  const checkUser = async () => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      try {
        const userData = await apiService.getCurrentUser();
        setUser(userData);
      } catch (err) {
        console.error('사용자 정보 로딩 실패:', err);
      }
    }
  };

  const loadProduct = async () => {
    try {
      setLoading(true);
      const data = await apiService.getProduct(id);
      setProduct(data);
      setError('');
    } catch (err) {
      setError('상품을 불러오는데 실패했습니다: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const loadReviews = async () => {
    try {
      const data = await apiService.getProductReviews(id, 0, 5);
      setReviews(data.content);
    } catch (err) {
      console.error('리뷰 로딩 실패:', err);
    }
  };

  const handleOrder = () => {
    if (!user) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }

    if (product.stock < quantity) {
      alert('재고가 부족합니다.');
      return;
    }

    // 주문 페이지로 이동 (상품 정보 전달)
    navigate('/orders/create', {
      state: {
        items: [{
          productId: product.id,
          quantity: quantity
        }]
      }
    });
  };

  const handleRestockVote = async () => {
    if (!user) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }

    try {
      await apiService.voteRestock(product.id);
      alert('재입고 투표가 완료되었습니다!');
    } catch (err) {
      if (err.code === 'DUPLICATE_VOTE') {
        alert('이미 투표하셨습니다.');
      } else {
        alert('재입고 투표 실패: ' + err.message);
      }
    }
  };

  const handleRestockNotification = async () => {
    if (!user) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }

    try {
      await apiService.requestRestockNotification(product.id);
      alert('재입고 알림 신청이 완료되었습니다!');
    } catch (err) {
      alert('재입고 알림 신청 실패: ' + err.message);
    }
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat('ko-KR').format(price) + '원';
  };

  if (loading) {
    return <div style={styles.loading}>로딩 중...</div>;
  }

  if (error) {
    return (
      <div style={styles.container}>
        <div style={styles.error}>{error}</div>
        <button onClick={() => navigate('/products')} style={styles.backButton}>
          목록으로
        </button>
      </div>
    );
  }

  if (!product) {
    return <div style={styles.loading}>상품을 찾을 수 없습니다.</div>;
  }

  return (
    <div style={styles.container}>
      <button onClick={() => navigate('/products')} style={styles.backButton}>
        ← 목록으로
      </button>

      <div style={styles.productSection}>
        {/* 상품 이미지 */}
        <div style={styles.imageSection}>
          {product.imageUrl ? (
            <img
              src={product.imageUrl}
              alt={product.name}
              style={styles.productImage}
            />
          ) : (
            <div style={styles.noImage}>이미지 없음</div>
          )}
        </div>

        {/* 상품 정보 */}
        <div style={styles.infoSection}>
          <h1 style={styles.productName}>{product.name}</h1>
          
          {product.category && (
            <div style={styles.category}>{product.category.name}</div>
          )}

          <div style={styles.priceSection}>
            <span style={styles.price}>{formatPrice(product.price)}</span>
          </div>

          <div style={styles.metaInfo}>
            <div style={styles.metaItem}>
              <span style={styles.metaLabel}>상태:</span>
              <span style={{
                ...styles.badge,
                backgroundColor: product.status === 'ACTIVE' ? '#28a745' : '#dc3545'
              }}>
                {product.status === 'ACTIVE' ? '판매중' : '품절'}
              </span>
            </div>
            
            <div style={styles.metaItem}>
              <span style={styles.metaLabel}>재고:</span>
              <span style={styles.metaValue}>{product.stock}개</span>
            </div>
            
            {product.salesCount > 0 && (
              <div style={styles.metaItem}>
                <span style={styles.metaLabel}>판매량:</span>
                <span style={styles.metaValue}>{product.salesCount}개</span>
              </div>
            )}
          </div>

          {product.description && (
            <div style={styles.description}>
              <h3 style={styles.descTitle}>상품 설명</h3>
              <p style={styles.descText}>{product.description}</p>
            </div>
          )}

          {/* 주문 섹션 */}
          {product.status === 'ACTIVE' && product.stock > 0 ? (
            <div style={styles.orderSection}>
              <div style={styles.quantitySection}>
                <label style={styles.quantityLabel}>수량:</label>
                <input
                  type="number"
                  min="1"
                  max={product.stock}
                  value={quantity}
                  onChange={(e) => setQuantity(Math.max(1, parseInt(e.target.value) || 1))}
                  style={styles.quantityInput}
                />
              </div>
              
              <button onClick={handleOrder} style={styles.orderButton}>
                주문하기
              </button>
            </div>
          ) : (
            <div style={styles.outOfStock}>
              <p style={styles.outOfStockText}>현재 품절된 상품입니다</p>
              <div style={styles.restockButtons}>
                <button onClick={handleRestockVote} style={styles.restockButton}>
                  재입고 투표하기
                </button>
                <button onClick={handleRestockNotification} style={styles.restockButton}>
                  재입고 알림 받기
                </button>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* 리뷰 섹션 */}
      <div style={styles.reviewSection}>
        <h2 style={styles.reviewTitle}>상품 리뷰 ({reviews.length})</h2>
        
        {reviews.length === 0 ? (
          <div style={styles.noReviews}>아직 리뷰가 없습니다.</div>
        ) : (
          <div style={styles.reviewList}>
            {reviews.map(review => (
              <div key={review.id} style={styles.reviewCard}>
                <div style={styles.reviewHeader}>
                  <span style={styles.reviewUser}>{review.user.name}</span>
                  <span style={styles.reviewRating}>⭐ {review.rating}</span>
                </div>
                <p style={styles.reviewContent}>{review.content}</p>
                <span style={styles.reviewDate}>
                  {new Date(review.createdAt).toLocaleDateString()}
                </span>
              </div>
            ))}
          </div>
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
  productSection: {
    display: 'grid',
    gridTemplateColumns: '1fr 1fr',
    gap: '40px',
    marginBottom: '40px',
  },
  imageSection: {
    backgroundColor: '#f8f9fa',
    borderRadius: '8px',
    overflow: 'hidden',
  },
  productImage: {
    width: '100%',
    height: '500px',
    objectFit: 'cover',
  },
  noImage: {
    width: '100%',
    height: '500px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    color: '#999',
    fontSize: '18px',
  },
  infoSection: {
    display: 'flex',
    flexDirection: 'column',
    gap: '20px',
  },
  productName: {
    fontSize: '28px',
    fontWeight: 'bold',
    color: '#333',
    marginBottom: '10px',
  },
  category: {
    display: 'inline-block',
    padding: '6px 12px',
    backgroundColor: '#e9ecef',
    color: '#666',
    borderRadius: '4px',
    fontSize: '14px',
  },
  priceSection: {
    padding: '20px 0',
    borderTop: '2px solid #e9ecef',
    borderBottom: '2px solid #e9ecef',
  },
  price: {
    fontSize: '32px',
    fontWeight: 'bold',
    color: '#007bff',
  },
  metaInfo: {
    display: 'flex',
    flexDirection: 'column',
    gap: '10px',
  },
  metaItem: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
  },
  metaLabel: {
    fontSize: '14px',
    color: '#666',
    fontWeight: 'bold',
  },
  metaValue: {
    fontSize: '16px',
    color: '#333',
  },
  badge: {
    padding: '4px 12px',
    fontSize: '14px',
    fontWeight: 'bold',
    color: 'white',
    borderRadius: '4px',
  },
  description: {
    padding: '20px',
    backgroundColor: '#f8f9fa',
    borderRadius: '8px',
  },
  descTitle: {
    fontSize: '18px',
    fontWeight: 'bold',
    marginBottom: '10px',
    color: '#333',
  },
  descText: {
    fontSize: '16px',
    lineHeight: '1.6',
    color: '#666',
  },
  orderSection: {
    padding: '20px',
    backgroundColor: '#f8f9fa',
    borderRadius: '8px',
  },
  quantitySection: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
    marginBottom: '15px',
  },
  quantityLabel: {
    fontSize: '16px',
    fontWeight: 'bold',
    color: '#333',
  },
  quantityInput: {
    width: '80px',
    padding: '8px',
    fontSize: '16px',
    border: '1px solid #ddd',
    borderRadius: '4px',
    textAlign: 'center',
  },
  orderButton: {
    width: '100%',
    padding: '15px',
    fontSize: '18px',
    fontWeight: 'bold',
    color: 'white',
    backgroundColor: '#007bff',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
  },
  outOfStock: {
    padding: '20px',
    backgroundColor: '#f8f9fa',
    borderRadius: '8px',
    textAlign: 'center',
  },
  outOfStockText: {
    fontSize: '16px',
    color: '#dc3545',
    marginBottom: '15px',
    fontWeight: 'bold',
  },
  restockButtons: {
    display: 'flex',
    gap: '10px',
  },
  restockButton: {
    flex: 1,
    padding: '12px',
    fontSize: '14px',
    fontWeight: 'bold',
    color: 'white',
    backgroundColor: '#28a745',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
  },
  reviewSection: {
    marginTop: '60px',
  },
  reviewTitle: {
    fontSize: '24px',
    fontWeight: 'bold',
    marginBottom: '20px',
    color: '#333',
  },
  noReviews: {
    textAlign: 'center',
    padding: '40px',
    color: '#999',
    fontSize: '16px',
  },
  reviewList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '15px',
  },
  reviewCard: {
    padding: '20px',
    backgroundColor: 'white',
    border: '1px solid #e9ecef',
    borderRadius: '8px',
  },
  reviewHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '10px',
  },
  reviewUser: {
    fontSize: '16px',
    fontWeight: 'bold',
    color: '#333',
  },
  reviewRating: {
    fontSize: '14px',
    color: '#ffc107',
  },
  reviewContent: {
    fontSize: '14px',
    lineHeight: '1.6',
    color: '#666',
    marginBottom: '10px',
  },
  reviewDate: {
    fontSize: '12px',
    color: '#999',
  },
};

export default ProductDetailPage;