import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import apiService from '../services/api';

function SellerProductListPage() {
  const navigate = useNavigate();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadProducts();
  }, []);

  const loadProducts = async () => {
    try {
      setLoading(true);
      const data = await apiService.getMyProducts(0, 100);
      setProducts(data.content || []);
      setError('');
    } catch (err) {
      setError('상품 목록을 불러오는데 실패했습니다: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('정말 삭제하시겠습니까?')) return;

    try {
      await apiService.deleteProduct(id);
      alert('삭제되었습니다.');
      loadProducts();
    } catch (err) {
      alert('삭제 실패: ' + err.message);
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
      <div style={styles.header}>
        <h1 style={styles.title}>상품 관리</h1>
        <button
          onClick={() => navigate('/seller/products/new')}
          style={styles.addButton}
        >
          + 상품 등록
        </button>
      </div>

      {error && (
        <div style={styles.error}>{error}</div>
      )}

      {products.length === 0 ? (
        <div style={styles.empty}>
          <p>등록된 상품이 없습니다.</p>
          <button
            onClick={() => navigate('/seller/products/new')}
            style={styles.addButton}
          >
            첫 상품 등록하기
          </button>
        </div>
      ) : (
        <div style={styles.tableContainer}>
          <table style={styles.table}>
            <thead>
              <tr style={styles.tableHeader}>
                <th style={styles.th}>이미지</th>
                <th style={styles.th}>상품명</th>
                <th style={styles.th}>가격</th>
                <th style={styles.th}>재고</th>
                <th style={styles.th}>상태</th>
                <th style={styles.th}>관리</th>
              </tr>
            </thead>
            <tbody>
              {products.map(product => (
                <tr key={product.id} style={styles.tableRow}>
                  <td style={styles.td}>
                    {product.imageUrl ? (
                      <img
                        src={product.imageUrl}
                        alt={product.name}
                        style={styles.productImage}
                      />
                    ) : (
                      <div style={styles.noImage}>No Image</div>
                    )}
                  </td>
                  <td style={styles.td}>{product.name}</td>
                  <td style={styles.td}>{formatPrice(product.price)}</td>
                  <td style={styles.td}>
                    <span style={{
                      ...styles.stockBadge,
                      ...(product.stockQuantity < 10 ? styles.stockLow : {})
                    }}>
                      {product.stockQuantity}개
                    </span>
                  </td>
                  <td style={styles.td}>
                    <span style={{
                      ...styles.statusBadge,
                      backgroundColor: product.status === 'ACTIVE' ? '#28a745' : '#6c757d'
                    }}>
                      {product.status === 'ACTIVE' ? '판매중' : '중단'}
                    </span>
                  </td>
                  <td style={styles.td}>
                    <div style={styles.actions}>
                      <button
                        onClick={() => navigate(`/seller/products/${product.id}/edit`)}
                        style={styles.editButton}
                      >
                        수정
                      </button>
                      <button
                        onClick={() => handleDelete(product.id)}
                        style={styles.deleteButton}
                      >
                        삭제
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
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
  addButton: {
    padding: '12px 24px',
    fontSize: '16px',
    fontWeight: 'bold',
    color: 'white',
    backgroundColor: '#007bff',
    border: 'none',
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
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
  },
  tableContainer: {
    backgroundColor: 'white',
    borderRadius: '8px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
    overflow: 'hidden',
  },
  table: {
    width: '100%',
    borderCollapse: 'collapse',
  },
  tableHeader: {
    backgroundColor: '#f8f9fa',
  },
  th: {
    padding: '15px',
    textAlign: 'left',
    fontWeight: 'bold',
    color: '#333',
    borderBottom: '2px solid #dee2e6',
  },
  tableRow: {
    borderBottom: '1px solid #dee2e6',
  },
  td: {
    padding: '15px',
    color: '#666',
  },
  productImage: {
    width: '60px',
    height: '60px',
    objectFit: 'cover',
    borderRadius: '4px',
  },
  noImage: {
    width: '60px',
    height: '60px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#e9ecef',
    borderRadius: '4px',
    fontSize: '10px',
    color: '#6c757d',
  },
  stockBadge: {
    padding: '4px 8px',
    borderRadius: '4px',
    fontSize: '14px',
    fontWeight: 'bold',
    backgroundColor: '#d4edda',
    color: '#155724',
  },
  stockLow: {
    backgroundColor: '#f8d7da',
    color: '#721c24',
  },
  statusBadge: {
    padding: '4px 12px',
    borderRadius: '12px',
    fontSize: '12px',
    fontWeight: 'bold',
    color: 'white',
  },
  actions: {
    display: 'flex',
    gap: '8px',
  },
  editButton: {
    padding: '8px 16px',
    fontSize: '14px',
    fontWeight: 'bold',
    color: 'white',
    backgroundColor: '#007bff',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
  },
  deleteButton: {
    padding: '8px 16px',
    fontSize: '14px',
    fontWeight: 'bold',
    color: 'white',
    backgroundColor: '#dc3545',
    border: 'none',
    borderRadius: '4px',
    cursor: 'pointer',
  },
};

export default SellerProductListPage;