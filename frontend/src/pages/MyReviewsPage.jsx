import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import apiService from '../services/api';

function MyReviewsPage() {
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [editingReview, setEditingReview] = useState(null);
  const [editForm, setEditForm] = useState({ rating: 5, comment: '' });
  const navigate = useNavigate();

  useEffect(() => {
    loadReviews();
  }, []);

  const loadReviews = async () => {
    try {
      setLoading(true);
      // 현재 사용자 정보 먼저 가져오기
      const currentUser = await apiService.getCurrentUser();
      
      // 내 주문 목록을 가져와서 리뷰가 있는 상품들을 찾음
      const orders = await apiService.getMyOrders(0, 100);
      
      // 각 주문의 상품들에 대한 리뷰를 가져옴
      const reviewPromises = [];
      const productIds = new Set();
      
      orders.content.forEach(order => {
        if (order.items) {
          order.items.forEach(item => {
            // OrderItemDto 구조: productId, productName 직접 제공
            if (item.productId && !productIds.has(item.productId)) {
              productIds.add(item.productId);
              reviewPromises.push(
                apiService.getProductReviews(item.productId, 0, 100)
                  .then(data => ({
                    productId: item.productId,
                    productName: item.productName,
                    reviews: data.content
                  }))
                  .catch(err => {
                    console.error(`상품 ${item.productId} 리뷰 로딩 실패:`, err);
                    return {
                      productId: item.productId,
                      productName: item.productName,
                      reviews: []
                    };
                  })
              );
            }
          });
        }
      });
      
      const allReviews = await Promise.all(reviewPromises);
      
      // 현재 사용자의 리뷰만 필터링
      const myReviews = [];
      allReviews.forEach(({ productId, productName, reviews }) => {
        reviews.forEach(review => {
          if (review.userId === currentUser.id) {
            myReviews.push({
              ...review,
              productId,
              productName,
            });
          }
        });
      });
      
      setReviews(myReviews);
      setError('');
    } catch (err) {
      setError('리뷰를 불러오는데 실패했습니다: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (review) => {
    setEditingReview(review.id);
    setEditForm({
      rating: review.rating,
      comment: review.comment,
    });
  };

  const handleCancelEdit = () => {
    setEditingReview(null);
    setEditForm({ rating: 5, comment: '' });
  };

  const handleSubmitEdit = async (reviewId) => {
    try {
      await apiService.updateReview(reviewId, editForm);
      alert('리뷰가 수정되었습니다.');
      setEditingReview(null);
      loadReviews();
    } catch (err) {
      alert('리뷰 수정 실패: ' + err.message);
    }
  };

  const handleDelete = async (reviewId) => {
    if (!window.confirm('리뷰를 삭제하시겠습니까?')) {
      return;
    }

    try {
      await apiService.deleteReview(reviewId);
      alert('리뷰가 삭제되었습니다.');
      loadReviews();
    } catch (err) {
      alert('리뷰 삭제 실패: ' + err.message);
    }
  };

  const renderStars = (rating) => {
    return '⭐'.repeat(rating) + '☆'.repeat(5 - rating);
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('ko-KR');
  };

  if (loading) {
    return <div style={styles.loading}>로딩 중...</div>;
  }

  return (
    <div style={styles.container}>
      <h1 style={styles.title}>내가 작성한 리뷰</h1>

      {error && <div style={styles.error}>{error}</div>}

      {reviews.length === 0 ? (
        <div style={styles.empty}>
          <p style={styles.emptyText}>작성한 리뷰가 없습니다.</p>
          <button onClick={() => navigate('/orders')} style={styles.goButton}>
            주문 내역 보기
          </button>
        </div>
      ) : (
        <div style={styles.reviewList}>
          {reviews.map(review => (
            <div key={review.id} style={styles.reviewCard}>
              {editingReview === review.id ? (
                // 수정 모드
                <div style={styles.editForm}>
                  <div style={styles.productHeader}>
                    <h3 
                      style={styles.productName}
                      onClick={() => navigate(`/products/${review.productId}`)}
                    >
                      {review.productName}
                    </h3>
                  </div>

                  <div style={styles.formGroup}>
                    <label style={styles.label}>평점</label>
                    <div style={styles.ratingInput}>
                      {[1, 2, 3, 4, 5].map(star => (
                        <span
                          key={star}
                          onClick={() => setEditForm({ ...editForm, rating: star })}
                          style={{
                            ...styles.star,
                            ...(star <= editForm.rating ? styles.starActive : {}),
                          }}
                        >
                          ⭐
                        </span>
                      ))}
                    </div>
                  </div>

                  <div style={styles.formGroup}>
                    <label style={styles.label}>리뷰 내용</label>
                    <textarea
                      value={editForm.comment}
                      onChange={(e) => setEditForm({ ...editForm, comment: e.target.value })}
                      style={styles.textarea}
                      rows={5}
                      required
                    />
                  </div>

                  <div style={styles.buttonGroup}>
                    <button
                      onClick={() => handleSubmitEdit(review.id)}
                      style={styles.saveButton}
                    >
                      저장
                    </button>
                    <button onClick={handleCancelEdit} style={styles.cancelButton}>
                      취소
                    </button>
                  </div>
                </div>
              ) : (
                // 조회 모드
                <>
                  <div style={styles.productHeader}>
                    <h3 
                      style={styles.productName}
                      onClick={() => navigate(`/products/${review.productId}`)}
                    >
                      {review.productName}
                    </h3>
                    <span style={styles.reviewDate}>
                      {formatDate(review.createdAt)}
                    </span>
                  </div>

                  <div style={styles.rating}>
                    {renderStars(review.rating)}
                  </div>

                  <p style={styles.comment}>{review.comment}</p>

                  <div style={styles.buttonGroup}>
                    <button onClick={() => handleEdit(review)} style={styles.editButton}>
                      수정
                    </button>
                    <button onClick={() => handleDelete(review.id)} style={styles.deleteButton}>
                      삭제
                    </button>
                  </div>
                </>
              )}
            </div>
          ))}
        </div>
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
  reviewList: {
    display: 'flex',
    flexDirection: 'column',
    gap: '15px',
  },
  reviewCard: {
    backgroundColor: 'white',
    borderRadius: '8px',
    padding: '20px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
  },
  productHeader: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '15px',
    paddingBottom: '15px',
    borderBottom: '1px solid #e9ecef',
  },
  productName: {
    fontSize: '18px',
    fontWeight: 'bold',
    color: '#007bff',
    cursor: 'pointer',
    margin: 0,
  },
  reviewDate: {
    fontSize: '14px',
    color: '#999',
  },
  rating: {
    fontSize: '24px',
    marginBottom: '10px',
  },
  comment: {
    fontSize: '16px',
    color: '#333',
    lineHeight: '1.6',
    marginBottom: '15px',
  },
  editForm: {
    display: 'flex',
    flexDirection: 'column',
    gap: '15px',
  },
  formGroup: {
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
  },
  label: {
    fontSize: '14px',
    fontWeight: '600',
    color: '#333',
  },
  ratingInput: {
    display: 'flex',
    gap: '5px',
  },
  star: {
    fontSize: '32px',
    cursor: 'pointer',
    opacity: 0.3,
    transition: 'opacity 0.2s',
  },
  starActive: {
    opacity: 1,
  },
  textarea: {
    width: '100%',
    padding: '12px',
    fontSize: '16px',
    border: '1px solid #ddd',
    borderRadius: '6px',
    resize: 'vertical',
    boxSizing: 'border-box',
  },
  buttonGroup: {
    display: 'flex',
    gap: '10px',
    justifyContent: 'flex-end',
  },
  editButton: {
    padding: '8px 20px',
    fontSize: '14px',
    fontWeight: 'bold',
    color: '#007bff',
    backgroundColor: 'white',
    border: '1px solid #007bff',
    borderRadius: '6px',
    cursor: 'pointer',
  },
  deleteButton: {
    padding: '8px 20px',
    fontSize: '14px',
    fontWeight: 'bold',
    color: '#dc3545',
    backgroundColor: 'white',
    border: '1px solid #dc3545',
    borderRadius: '6px',
    cursor: 'pointer',
  },
  saveButton: {
    padding: '8px 20px',
    fontSize: '14px',
    fontWeight: 'bold',
    color: 'white',
    backgroundColor: '#28a745',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
  },
  cancelButton: {
    padding: '8px 20px',
    fontSize: '14px',
    fontWeight: 'bold',
    color: '#666',
    backgroundColor: 'white',
    border: '1px solid #ddd',
    borderRadius: '6px',
    cursor: 'pointer',
  },
};

export default MyReviewsPage;