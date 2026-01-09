import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import apiService from '../services/api';

function ProductDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  
  const [product, setProduct] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [restockVoteCount, setRestockVoteCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [quantity, setQuantity] = useState(1);
  const [user, setUser] = useState(null);

  useEffect(() => {
    loadProduct();
    loadReviews();
    loadRestockVoteCount();
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
      setReviews(data.content || []);
    } catch (err) {
      console.error('리뷰 로딩 실패:', err);
    }
  };

  const loadRestockVoteCount = async () => {
    try {
      // 백엔드에 재입고 투표 수를 가져오는 API가 있다면 사용
      // 현재는 투표 목록의 totalElements를 사용
      const response = await fetch(`http://54.206.243.31:8080/api/restock-votes/products/${id}?page=0&size=1`);
      if (response.ok) {
        const data = await response.json();
        setRestockVoteCount(data.totalElements || 0);
      }
    } catch (err) {
      console.error('재입고 투표 수 로딩 실패:', err);
    }
  };

  const handleAddToCart = async () => {
    if (!user) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }

    if (product.stock < quantity) {
      alert('재고가 부족합니다.');
      return;
    }

    try {
      await apiService.addToCart(product.id, quantity);
      
      if (window.confirm('장바구니에 추가되었습니다. 장바구니로 이동하시겠습니까?')) {
        navigate('/cart');
      }
    } catch (err) {
      alert('장바구니 추가 실패: ' + err.message);
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
      loadRestockVoteCount(); // 투표 수 새로고침
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

  const getStatusBadge = (status) => {
    if (status === 'ACTIVE') {
      return <span style={styles.statusBadgeActive}>판매중</span>;
    } else if (status === 'OUT_OF_STOCK') {
      return <span style={styles.statusBadgeOutOfStock}>품절</span>;
    } else {
      return <span style={styles.statusBadgeInactive}>판매중지</span>;
    }
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

  if (!product) {
    return (
      <div style={styles.container}>
        <div style={styles.error}>상품을 찾을 수 없습니다.</div>
      </div>
    );
  }

  return (
    <div style={styles.container}>
      <div style={styles.productSection}>
        <div style={styles.imageSection}>
          {product.imageUrl ? (
            <img src={product.imageUrl} alt={product.name} style={styles.productImage} />
          ) : (
            <div style={styles.noImage}>이미지 없음</div>
          )}
        </div>

        <div style={styles.infoSection}>
          <div style={styles.header}>
            <h1 style={styles.productName}>{product.name}</h1>
            {getStatusBadge(product.status)}
          </div>

          {/* 판매자 정보 */}
          {product.seller && (
            <div style={styles.sellerInfo}>
              <span style={styles.sellerLabel}>판매자:</span>
              <span style={styles.sellerName}>{product.seller.businessName}</span>
            </div>
          )}

          {/* 카테고리 */}
          {product.category && (
            <div style={styles.categoryInfo}>
              <span style={styles.categoryLabel}>카테고리:</span>
              <span style={styles.categoryName}>{product.category.name}</span>
            </div>
          )}

          <div style={styles.priceSection}>
            <span style={styles.price}>{formatPrice(product.price)}</span>
          </div>

          <div style={styles.stockInfo}>
            <span style={styles.stockLabel}>재고:</span>
            <span style={styles.stockValue}>
              {product.stock > 0 ? `${product.stock}개` : '품절'}
            </span>
          </div>

          {/* 재입고 투표 정보 */}
          {product.status === 'OUT_OF_STOCK' && (
            <div style={styles.restockSection}>
              <div style={styles.restockInfo}>
                <span style={styles.restockLabel}>재입고 요청:</span>
                <span style={styles.restockCount}>{restockVoteCount}명이 원해요</span>
              </div>
              <div style={styles.restockButtons}>
                <button onClick={handleRestockVote} style={styles.restockVoteButton}>
                  재입고 투표하기
                </button>
                <button onClick={handleRestockNotification} style={styles.restockNotifyButton}>
                  재입고 알림 신청
                </button>
              </div>
            </div>
          )}

          {product.stock > 0 && (
            <>
              <div style={styles.quantitySection}>
                <span style={styles.quantityLabel}>수량:</span>
                <div style={styles.quantityControl}>
                  <button
                    onClick={() => setQuantity(Math.max(1, quantity - 1))}
                    style={styles.quantityButton}
                  >
                    -
                  </button>
                  <span style={styles.quantityValue}>{quantity}</span>
                  <button
                    onClick={() => setQuantity(Math.min(product.stock, quantity + 1))}
                    style={styles.quantityButton}
                  >
                    +
                  </button>
                </div>
              </div>

              <div style={styles.totalSection}>
                <span style={styles.totalLabel}>총 금액:</span>
                <span style={styles.totalPrice}>
                  {formatPrice(product.price * quantity)}
                </span>
              </div>

              <div style={styles.buttonGroup}>
                <button onClick={handleAddToCart} style={styles.cartButton}>
                  🛒 장바구니
                </button>
                <button onClick={handleOrder} style={styles.buyButton}>
                  바로 구매
                </button>
              </div>
            </>
          )}

          <div style={styles.description}>
            <h3 style={styles.descriptionTitle}>상품 설명</h3>
            <p style={styles.descriptionText}>{product.description}</p>
          </div>
        </div>
      </div>

      {/* 리뷰 섹션 */}
      <div style={styles.reviewSection}>
        <h2 style={styles.reviewTitle}>상품 리뷰</h2>
        {reviews.length === 0 ? (
          <div style={styles.noReviews}>아직 리뷰가 없습니다.</div>
        ) : (
          <div style={styles.reviewList}>
            {reviews.map((review) => (
              <div key={review.id} style={styles.reviewCard}>
                <div style={styles.reviewHeader}>
                  <div style={styles.reviewRating}>
                    {'⭐'.repeat(review.rating)}
                  </div>
                  <div style={styles.reviewDate}>
                    {new Date(review.createdAt).toLocaleDateString()}
                  </div>
                </div>
                <div style={styles.reviewAuthor}>{review.userName}</div>
                <div style={styles.reviewComment}>{review.comment}</div>
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
  productSection: {
    display: 'grid',
    gridTemplateColumns: '1fr 1fr',
    gap: '40px',
    marginBottom: '40px',
    backgroundColor: 'white',
    padding: '30px',
    borderRadius: '8px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
  },
  imageSection: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
  },
  productImage: {
    width: '100%',
    maxWidth: '500px',
    height: 'auto',
    borderRadius: '8px',
    objectFit: 'cover',
  },
  noImage: {
    width: '100%',
    height: '400px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#f0f0f0',
    borderRadius: '8px',
    color: '#999',
    fontSize: '18px',
  },
  infoSection: {
    display: 'flex',
    flexDirection: 'column',
    gap: '20px',
  },
  header: {
    display: 'flex',
    alignItems: 'center',
    gap: '15px',
  },
  productName: {
    fontSize: '28px',
    fontWeight: 'bold',
    color: '#333',
    margin: 0,
  },
  statusBadgeActive: {
    padding: '6px 12px',
    backgroundColor: '#28a745',
    color: 'white',
    borderRadius: '4px',
    fontSize: '14px',
    fontWeight: 'bold',
  },
  statusBadgeOutOfStock: {
    padding: '6px 12px',
    backgroundColor: '#dc3545',
    color: 'white',
    borderRadius: '4px',
    fontSize: '14px',
    fontWeight: 'bold',
  },
  statusBadgeInactive: {
    padding: '6px 12px',
    backgroundColor: '#6c757d',
    color: 'white',
    borderRadius: '4px',
    fontSize: '14px',
    fontWeight: 'bold',
  },
  sellerInfo: {
    display: 'flex',
    gap: '10px',
    padding: '10px 0',
    borderBottom: '1px solid #e9ecef',
  },
  sellerLabel: {
    fontWeight: 'bold',
    color: '#666',
  },
  sellerName: {
    color: '#333',
  },
  categoryInfo: {
    display: 'flex',
    gap: '10px',
    padding: '10px 0',
    borderBottom: '1px solid #e9ecef',
  },
  categoryLabel: {
    fontWeight: 'bold',
    color: '#666',
  },
  categoryName: {
    color: '#333',
  },
  priceSection: {
    padding: '15px 0',
    borderBottom: '1px solid #e9ecef',
  },
  price: {
    fontSize: '32px',
    fontWeight: 'bold',
    color: '#007bff',
  },
  stockInfo: {
    display: 'flex',
    gap: '10px',
    alignItems: 'center',
  },
  stockLabel: {
    fontWeight: 'bold',
    color: '#666',
  },
  stockValue: {
    color: '#333',
    fontSize: '16px',
  },
  restockSection: {
    backgroundColor: '#fff3cd',
    padding: '15px',
    borderRadius: '6px',
    border: '1px solid #ffc107',
  },
  restockInfo: {
    display: 'flex',
    gap: '10px',
    marginBottom: '10px',
  },
  restockLabel: {
    fontWeight: 'bold',
    color: '#856404',
  },
  restockCount: {
    color: '#856404',
  },
  restockButtons: {
    display: 'flex',
    gap: '10px',
  },
  restockVoteButton: {
    flex: 1,
    padding: '10px',
    fontSize: '14px',
    fontWeight: 'bold',
    color: '#856404',
    backgroundColor: 'white',
    border: '1px solid #ffc107',
    borderRadius: '6px',
    cursor: 'pointer',
  },
  restockNotifyButton: {
    flex: 1,
    padding: '10px',
    fontSize: '14px',
    fontWeight: 'bold',
    color: 'white',
    backgroundColor: '#ffc107',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
  },
  quantitySection: {
    display: 'flex',
    alignItems: 'center',
    gap: '15px',
  },
  quantityLabel: {
    fontWeight: 'bold',
    color: '#666',
  },
  quantityControl: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
  },
  quantityButton: {
    width: '40px',
    height: '40px',
    fontSize: '20px',
    fontWeight: 'bold',
    backgroundColor: 'white',
    border: '1px solid #ddd',
    borderRadius: '6px',
    cursor: 'pointer',
  },
  quantityValue: {
    fontSize: '18px',
    fontWeight: 'bold',
    minWidth: '40px',
    textAlign: 'center',
  },
  totalSection: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: '15px 0',
    borderTop: '2px solid #e9ecef',
  },
  totalLabel: {
    fontSize: '18px',
    fontWeight: 'bold',
    color: '#333',
  },
  totalPrice: {
    fontSize: '24px',
    fontWeight: 'bold',
    color: '#007bff',
  },
  buttonGroup: {
    display: 'grid',
    gridTemplateColumns: '1fr 2fr',
    gap: '10px',
  },
  cartButton: {
    padding: '15px',
    fontSize: '16px',
    fontWeight: 'bold',
    color: '#007bff',
    backgroundColor: 'white',
    border: '2px solid #007bff',
    borderRadius: '6px',
    cursor: 'pointer',
  },
  buyButton: {
    padding: '15px',
    fontSize: '16px',
    fontWeight: 'bold',
    color: 'white',
    backgroundColor: '#007bff',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
  },
  description: {
    marginTop: '20px',
    padding: '20px',
    backgroundColor: '#f8f9fa',
    borderRadius: '6px',
  },
  descriptionTitle: {
    fontSize: '18px',
    fontWeight: 'bold',
    marginBottom: '10px',
    color: '#333',
  },
  descriptionText: {
    fontSize: '16px',
    lineHeight: '1.6',
    color: '#666',
  },
  reviewSection: {
    backgroundColor: 'white',
    padding: '30px',
    borderRadius: '8px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
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
    backgroundColor: '#f8f9fa',
    borderRadius: '6px',
  },
  reviewHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '10px',
  },
  reviewRating: {
    fontSize: '18px',
  },
  reviewDate: {
    fontSize: '14px',
    color: '#999',
  },
  reviewAuthor: {
    fontSize: '14px',
    fontWeight: 'bold',
    color: '#666',
    marginBottom: '8px',
  },
  reviewComment: {
    fontSize: '16px',
    color: '#333',
    lineHeight: '1.5',
  },
};

export default ProductDetailPage;