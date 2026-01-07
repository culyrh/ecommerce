import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import apiService from '../services/api';

function ProductListPage() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  // 검색 필터
  const [keyword, setKeyword] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  
  const navigate = useNavigate();

  useEffect(() => {
    loadCategories();
    loadProducts();
  }, [page, selectedCategory]);

  const loadCategories = async () => {
    try {
      const data = await apiService.getCategories();
      setCategories(data);
    } catch (err) {
      console.error('카테고리 로딩 실패:', err);
    }
  };

  const loadProducts = async () => {
    try {
      setLoading(true);
      const params = {
        page,
        size: 12,
        sort: 'createdAt',
        direction: 'DESC'
      };
      
      if (keyword) params.keyword = keyword;
      if (selectedCategory) params.categoryId = selectedCategory;
      
      const data = await apiService.getProducts(params);
      setProducts(data.content);
      setTotalPages(data.totalPages);
      setError('');
    } catch (err) {
      setError('상품을 불러오는데 실패했습니다: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    setPage(0);
    loadProducts();
  };

  const formatPrice = (price) => {
    return new Intl.NumberFormat('ko-KR').format(price) + '원';
  };

  return (
    <div style={styles.container}>
      {/* 검색 섹션 */}
      <div style={styles.searchSection}>
        <h1 style={styles.title}>상품 목록</h1>
        
        <form onSubmit={handleSearch} style={styles.searchForm}>
          <input
            type="text"
            placeholder="상품명으로 검색..."
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            style={styles.searchInput}
          />
          
          <select
            value={selectedCategory}
            onChange={(e) => {
              setSelectedCategory(e.target.value);
              setPage(0);
            }}
            style={styles.select}
          >
            <option value="">모든 카테고리</option>
            {categories.map(cat => (
              <option key={cat.id} value={cat.id}>
                {cat.name}
              </option>
            ))}
          </select>
          
          <button type="submit" style={styles.searchButton}>
            검색
          </button>
        </form>
      </div>

      {/* 에러 메시지 */}
      {error && (
        <div style={styles.error}>
          {error}
        </div>
      )}

      {/* 로딩 */}
      {loading ? (
        <div style={styles.loading}>로딩 중...</div>
      ) : (
        <>
          {/* 상품 그리드 */}
          <div style={styles.productGrid}>
            {products.length === 0 ? (
              <div style={styles.empty}>
                상품이 없습니다.
              </div>
            ) : (
              products.map(product => (
                <div
                  key={product.id}
                  style={styles.productCard}
                  onClick={() => navigate(`/products/${product.id}`)}
                >
                  {product.imageUrl && (
                    <img
                      src={product.imageUrl}
                      alt={product.name}
                      style={styles.productImage}
                    />
                  )}
                  
                  <div style={styles.productInfo}>
                    <h3 style={styles.productName}>{product.name}</h3>
                    <p style={styles.productPrice}>{formatPrice(product.price)}</p>
                    
                    <div style={styles.productMeta}>
                      <span style={styles.badge}>
                        {product.status === 'ACTIVE' ? '판매중' : '품절'}
                      </span>
                      <span style={styles.stock}>
                        재고: {product.stock}개
                      </span>
                    </div>
                    
                    {product.category && (
                      <span style={styles.category}>
                        {product.category.name}
                      </span>
                    )}
                  </div>
                </div>
              ))
            )}
          </div>

          {/* 페이지네이션 */}
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
              
              <span style={styles.pageInfo}>
                {page + 1} / {totalPages}
              </span>
              
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
    maxWidth: '1200px',
    margin: '0 auto',
    padding: '20px',
  },
  searchSection: {
    marginBottom: '30px',
  },
  title: {
    fontSize: '28px',
    fontWeight: 'bold',
    marginBottom: '20px',
    color: '#333',
  },
  searchForm: {
    display: 'flex',
    gap: '10px',
    marginBottom: '20px',
  },
  searchInput: {
    flex: 1,
    padding: '12px',
    fontSize: '16px',
    border: '1px solid #ddd',
    borderRadius: '6px',
  },
  select: {
    padding: '12px',
    fontSize: '16px',
    border: '1px solid #ddd',
    borderRadius: '6px',
    backgroundColor: 'white',
  },
  searchButton: {
    padding: '12px 30px',
    fontSize: '16px',
    fontWeight: 'bold',
    color: 'white',
    backgroundColor: '#007bff',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
  },
  error: {
    padding: '15px',
    backgroundColor: '#fee',
    color: '#c33',
    borderRadius: '6px',
    marginBottom: '20px',
  },
  loading: {
    textAlign: 'center',
    padding: '40px',
    fontSize: '18px',
    color: '#666',
  },
  productGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))',
    gap: '20px',
    marginBottom: '30px',
  },
  productCard: {
    backgroundColor: 'white',
    borderRadius: '8px',
    overflow: 'hidden',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
    cursor: 'pointer',
    transition: 'transform 0.2s, box-shadow 0.2s',
    ':hover': {
      transform: 'translateY(-4px)',
      boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
    },
  },
  productImage: {
    width: '100%',
    height: '200px',
    objectFit: 'cover',
  },
  productInfo: {
    padding: '15px',
  },
  productName: {
    fontSize: '16px',
    fontWeight: 'bold',
    marginBottom: '8px',
    color: '#333',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    whiteSpace: 'nowrap',
  },
  productPrice: {
    fontSize: '20px',
    fontWeight: 'bold',
    color: '#007bff',
    marginBottom: '10px',
  },
  productMeta: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '8px',
  },
  badge: {
    padding: '4px 8px',
    fontSize: '12px',
    fontWeight: 'bold',
    borderRadius: '4px',
    backgroundColor: '#28a745',
    color: 'white',
  },
  stock: {
    fontSize: '14px',
    color: '#666',
  },
  category: {
    fontSize: '12px',
    color: '#888',
  },
  empty: {
    gridColumn: '1 / -1',
    textAlign: 'center',
    padding: '40px',
    color: '#666',
    fontSize: '16px',
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

export default ProductListPage;