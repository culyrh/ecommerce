import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import apiService from '../services/api';

function BecomeSellerPage() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [user, setUser] = useState(null);
  
  const [formData, setFormData] = useState({
    businessName: '',
    businessNumber: '',
    minStockThreshold: 10
  });

  useEffect(() => {
    checkUser();
  }, []);

  const checkUser = async () => {
    try {
      const userData = await apiService.getCurrentUser();
      setUser(userData);
      
      // 이미 판매자인 경우
      if (userData.roles?.includes('ROLE_SELLER') || userData.roles?.includes('ROLE_ADMIN')) {
        alert('이미 판매자이십니다.');
        navigate('/seller/dashboard');
      }
    } catch (err) {
      alert('로그인이 필요합니다.');
      navigate('/login');
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
    
    if (!formData.businessName.trim()) {
      setError('상호명을 입력해주세요.');
      return;
    }
    
    if (!formData.businessNumber.trim()) {
      setError('사업자번호를 입력해주세요.');
      return;
    }

    try {
      setLoading(true);
      setError('');
      
      await apiService.registerSeller({
        ...formData,
        minStockThreshold: parseInt(formData.minStockThreshold)
      });
      
      alert('판매자 등록이 완료되었습니다!');
      
      // 토큰 갱신을 위해 다시 로그인하거나, 페이지 새로고침
      window.location.href = '/seller/dashboard';
      
    } catch (err) {
      if (err.code === 'DUPLICATE_BUSINESS_NUMBER') {
        setError('이미 등록된 사업자번호입니다.');
      } else if (err.code === 'DUPLICATE_RESOURCE') {
        setError('이미 판매자로 등록되어 있습니다.');
      } else {
        setError('판매자 등록 실패: ' + err.message);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.formCard}>
        <h1 style={styles.title}>판매자 등록</h1>
        <p style={styles.subtitle}>
          판매자로 등록하고 상품을 판매해보세요
        </p>

        {error && (
          <div style={styles.error}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} style={styles.form}>
          <div style={styles.formGroup}>
            <label style={styles.label}>
              상호명 (필수)
            </label>
            <input
              type="text"
              name="businessName"
              value={formData.businessName}
              onChange={handleChange}
              placeholder="예: 홍길동상점"
              style={styles.input}
              required
            />
          </div>

          <div style={styles.formGroup}>
            <label style={styles.label}>
              사업자번호 (필수)
            </label>
            <input
              type="text"
              name="businessNumber"
              value={formData.businessNumber}
              onChange={handleChange}
              placeholder="예: 123-45-67890"
              style={styles.input}
              required
            />
            <small style={styles.hint}>
              하이픈(-)을 포함하여 입력해주세요
            </small>
          </div>

          <div style={styles.formGroup}>
            <label style={styles.label}>
              재고 부족 알림 임계값
            </label>
            <input
              type="number"
              name="minStockThreshold"
              value={formData.minStockThreshold}
              onChange={handleChange}
              min="1"
              style={styles.input}
            />
            <small style={styles.hint}>
              재고가 이 값 이하로 떨어지면 알림을 받습니다 (기본값: 10개)
            </small>
          </div>

          <div style={styles.infoBox}>
            <h3 style={styles.infoTitle}>판매자 혜택</h3>
            <ul style={styles.benefitList}>
              <li>✅ 무제한 상품 등록</li>
              <li>✅ 판매 대시보드 제공</li>
              <li>✅ 실시간 주문 관리</li>
              <li>✅ 재고 자동 알림</li>
              <li>✅ 매출 통계 분석</li>
            </ul>
          </div>

          <div style={styles.buttonGroup}>
            <button
              type="button"
              onClick={() => navigate('/')}
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
              {loading ? '처리 중...' : '판매자 등록하기'}
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
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
  },
  formCard: {
    backgroundColor: 'white',
    padding: '40px',
    borderRadius: '12px',
    boxShadow: '0 4px 20px rgba(0,0,0,0.1)',
    maxWidth: '600px',
    width: '100%',
  },
  title: {
    fontSize: '28px',
    fontWeight: 'bold',
    marginBottom: '10px',
    color: '#333',
    textAlign: 'center',
  },
  subtitle: {
    fontSize: '16px',
    color: '#666',
    textAlign: 'center',
    marginBottom: '30px',
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
  hint: {
    fontSize: '12px',
    color: '#999',
  },
  infoBox: {
    padding: '20px',
    backgroundColor: '#f8f9fa',
    borderRadius: '8px',
    marginTop: '10px',
  },
  infoTitle: {
    fontSize: '16px',
    fontWeight: 'bold',
    marginBottom: '15px',
    color: '#333',
  },
  benefitList: {
    listStyle: 'none',
    padding: 0,
    margin: 0,
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
  },
  buttonGroup: {
    display: 'flex',
    gap: '10px',
    marginTop: '10px',
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

export default BecomeSellerPage;