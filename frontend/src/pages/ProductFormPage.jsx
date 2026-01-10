import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import apiService from '../services/api';

function ProductFormPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEditMode = !!id;

  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    price: '',
    stock: '',
    categoryId: '',
    imageUrl: '',
    status: 'ACTIVE'
  });

  useEffect(() => {
    loadCategories();
    if (isEditMode) {
      loadProduct();
    }
  }, [id]);

  const loadCategories = async () => {
    try {
      const data = await apiService.getCategories();
      setCategories(data || []); // data.content가 아니라 data 직접 사용
    } catch (err) {
      console.error('카테고리 로딩 실패:', err);
    }
  };

  const loadProduct = async () => {
    try {
      const data = await apiService.getProduct(id);
      setFormData({
        name: data.name || '',
        description: data.description || '',
        price: data.price || '',
        stock: data.stock || '',
        categoryId: data.category?.id || '',
        imageUrl: data.imageUrl || '',
        status: data.status || 'ACTIVE'
      });
    } catch (err) {
      setError('상품 정보를 불러오는데 실패했습니다: ' + err.message);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.name.trim()) {
      setError('상품명을 입력해주세요.');
      return;
    }
    
    if (!formData.price || formData.price <= 0) {
      setError('가격을 올바르게 입력해주세요.');
      return;
    }
    
    if (!formData.stock || formData.stock < 0) {
      setError('재고를 올바르게 입력해주세요.');
      return;
    }
    
    if (!formData.categoryId) {
      setError('카테고리를 선택해주세요.');
      return;
    }

    try {
      setLoading(true);
      setError('');
      
      const productData = {
        name: formData.name,
        description: formData.description,
        price: parseFloat(formData.price),
        stock: parseInt(formData.stock),
        categoryId: parseInt(formData.categoryId),
        imageUrl: formData.imageUrl,
        status: formData.status
      };

      if (isEditMode) {
        await apiService.updateProduct(id, productData);
        alert('상품이 수정되었습니다!');
      } else {
        await apiService.createProduct(productData);
        alert('상품이 등록되었습니다!');
      }
      
      navigate('/seller/products');
      
    } catch (err) {
      setError(`상품 ${isEditMode ? '수정' : '등록'} 실패: ` + err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.formCard}>
        <h1 style={styles.title}>
          {isEditMode ? '상품 수정' : '상품 등록'}
        </h1>

        {error && (
          <div style={styles.error}>{error}</div>
        )}

        <form onSubmit={handleSubmit} style={styles.form}>
          <div style={styles.formGroup}>
            <label style={styles.label}>
              상품명 <span style={styles.required}>*</span>
            </label>
            <input
              type="text"
              name="name"
              value={formData.name}
              onChange={handleChange}
              placeholder="예: 나이키 에어맥스"
              style={styles.input}
              required
            />
          </div>

          <div style={styles.formGroup}>
            <label style={styles.label}>
              상품 설명
            </label>
            <textarea
              name="description"
              value={formData.description}
              onChange={handleChange}
              placeholder="상품에 대한 상세한 설명을 입력해주세요"
              style={styles.textarea}
              rows="5"
            />
          </div>

          <div style={styles.formRow}>
            <div style={styles.formGroup}>
              <label style={styles.label}>
                가격 (원) <span style={styles.required}>*</span>
              </label>
              <input
                type="number"
                name="price"
                value={formData.price}
                onChange={handleChange}
                placeholder="10000"
                min="0"
                style={styles.input}
                required
              />
            </div>

            <div style={styles.formGroup}>
              <label style={styles.label}>
                재고 수량 <span style={styles.required}>*</span>
              </label>
              <input
                type="number"
                name="stock"
                value={formData.stock}
                onChange={handleChange}
                placeholder="100"
                min="0"
                style={styles.input}
                required
              />
            </div>
          </div>

          <div style={styles.formGroup}>
            <label style={styles.label}>
              카테고리 <span style={styles.required}>*</span>
            </label>
            <select
              name="categoryId"
              value={formData.categoryId}
              onChange={handleChange}
              style={styles.select}
              required
            >
              <option value="">카테고리를 선택하세요</option>
              {categories.map(category => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
          </div>

          <div style={styles.formGroup}>
            <label style={styles.label}>
              이미지 URL
            </label>
            <input
              type="url"
              name="imageUrl"
              value={formData.imageUrl}
              onChange={handleChange}
              placeholder="https://example.com/image.jpg"
              style={styles.input}
            />
            {formData.imageUrl && (
              <div style={styles.imagePreview}>
                <img
                  src={formData.imageUrl}
                  alt="미리보기"
                  style={styles.previewImage}
                  onError={(e) => {
                    e.target.style.display = 'none';
                  }}
                />
              </div>
            )}
          </div>

          <div style={styles.formGroup}>
            <label style={styles.label}>
              상태
            </label>
            <select
              name="status"
              value={formData.status}
              onChange={handleChange}
              style={styles.select}
            >
              <option value="ACTIVE">판매중</option>
              <option value="OUT_OF_STOCK">품절</option>
              <option value="INACTIVE">판매중지</option>
            </select>
          </div>

          <div style={styles.buttonGroup}>
            <button
              type="button"
              onClick={() => navigate('/seller/products')}
              style={styles.cancelButton}
              disabled={loading}
            >
              취소
            </button>
            <button
              type="submit"
              style={styles.submitButton}
              disabled={loading}
            >
              {loading ? '처리 중...' : (isEditMode ? '수정하기' : '등록하기')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

const styles = {
  container: {
    minHeight: '100vh',
    backgroundColor: '#f5f5f5',
    padding: '40px 20px',
  },
  formCard: {
    maxWidth: '800px',
    margin: '0 auto',
    backgroundColor: 'white',
    padding: '40px',
    borderRadius: '12px',
    boxShadow: '0 4px 20px rgba(0,0,0,0.1)',
  },
  title: {
    fontSize: '28px',
    fontWeight: 'bold',
    marginBottom: '30px',
    color: '#333',
    textAlign: 'center',
  },
  error: {
    padding: '15px',
    backgroundColor: '#fee',
    color: '#c33',
    borderRadius: '6px',
    marginBottom: '20px',
    textAlign: 'center',
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: '20px',
  },
  formRow: {
    display: 'grid',
    gridTemplateColumns: '1fr 1fr',
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
  required: {
    color: '#dc3545',
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
    fontFamily: 'inherit',
  },
  select: {
    padding: '12px',
    fontSize: '16px',
    border: '1px solid #ddd',
    borderRadius: '6px',
    outline: 'none',
    backgroundColor: 'white',
    cursor: 'pointer',
  },
  imagePreview: {
    marginTop: '10px',
    padding: '10px',
    border: '1px solid #ddd',
    borderRadius: '6px',
    backgroundColor: '#f8f9fa',
  },
  previewImage: {
    maxWidth: '100%',
    maxHeight: '300px',
    objectFit: 'contain',
    display: 'block',
    margin: '0 auto',
  },
  buttonGroup: {
    display: 'flex',
    gap: '10px',
    marginTop: '20px',
  },
  cancelButton: {
    flex: 1,
    padding: '14px',
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
    padding: '14px',
    fontSize: '16px',
    fontWeight: 'bold',
    color: 'white',
    backgroundColor: '#007bff',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
  },
};

export default ProductFormPage;