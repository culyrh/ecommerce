import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import apiService from '../services/api';

function ProfilePage() {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    name: '',
    phone: '',
    address: '',
  });
  const navigate = useNavigate();

  useEffect(() => {
    loadUser();
  }, []);

  const loadUser = async () => {
    try {
      setLoading(true);
      const userData = await apiService.getCurrentUser();
      setUser(userData);
      setFormData({
        name: userData.name || '',
        phone: userData.phone || '',
        address: userData.address || '',
      });
      setError('');
    } catch (err) {
      setError('사용자 정보를 불러오는데 실패했습니다: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      await apiService.updateUser(formData);
      alert('정보가 수정되었습니다.');
      setIsEditing(false);
      loadUser();
    } catch (err) {
      alert('수정 실패: ' + err.message);
    }
  };

  const handleDeleteAccount = async () => {
    if (!window.confirm('정말로 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
      return;
    }

    if (!window.confirm('모든 데이터가 삭제됩니다. 계속하시겠습니까?')) {
      return;
    }

    try {
      await apiService.deleteAccount();
      alert('회원 탈퇴가 완료되었습니다.');
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      navigate('/');
      window.location.reload();
    } catch (err) {
      alert('탈퇴 실패: ' + err.message);
    }
  };

  if (loading) {
    return <div style={styles.loading}>로딩 중...</div>;
  }

  if (!user) {
    return <div style={styles.error}>사용자 정보를 불러올 수 없습니다.</div>;
  }

  return (
    <div style={styles.container}>
      <h1 style={styles.title}>내 정보</h1>

      {error && <div style={styles.error}>{error}</div>}

      <div style={styles.card}>
        {!isEditing ? (
          // 조회 모드
          <>
            <div style={styles.section}>
              <h2 style={styles.sectionTitle}>기본 정보</h2>
              
              <div style={styles.infoRow}>
                <span style={styles.label}>이름:</span>
                <span style={styles.value}>{user.name}</span>
              </div>
              
              <div style={styles.infoRow}>
                <span style={styles.label}>이메일:</span>
                <span style={styles.value}>{user.email}</span>
              </div>
              
              <div style={styles.infoRow}>
                <span style={styles.label}>전화번호:</span>
                <span style={styles.value}>{user.phone || '미등록'}</span>
              </div>
              
              <div style={styles.infoRow}>
                <span style={styles.label}>주소:</span>
                <span style={styles.value}>{user.address || '미등록'}</span>
              </div>
              
              <div style={styles.infoRow}>
                <span style={styles.label}>생년월일:</span>
                <span style={styles.value}>
                  {user.birthDate ? new Date(user.birthDate).toLocaleDateString('ko-KR') : '미등록'}
                </span>
              </div>
            </div>

            <div style={styles.section}>
              <h2 style={styles.sectionTitle}>계정 정보</h2>
              
              <div style={styles.infoRow}>
                <span style={styles.label}>역할:</span>
                <span style={styles.value}>
                  {user.role === 'ROLE_USER' && '일반 회원'}
                  {user.role === 'ROLE_SELLER' && '판매자'}
                  {user.role === 'ROLE_ADMIN' && '관리자'}
                </span>
              </div>
              
              <div style={styles.infoRow}>
                <span style={styles.label}>가입일:</span>
                <span style={styles.value}>
                  {new Date(user.createdAt).toLocaleDateString('ko-KR')}
                </span>
              </div>
              
              {user.totalSpent !== undefined && (
                <div style={styles.infoRow}>
                  <span style={styles.label}>누적 구매금액:</span>
                  <span style={styles.value}>
                    {new Intl.NumberFormat('ko-KR').format(user.totalSpent)}원
                  </span>
                </div>
              )}
            </div>

            <div style={styles.buttonGroup}>
              <button onClick={() => setIsEditing(true)} style={styles.editButton}>
                정보 수정
              </button>
              <button onClick={handleDeleteAccount} style={styles.deleteButton}>
                회원 탈퇴
              </button>
            </div>
          </>
        ) : (
          // 수정 모드
          <form onSubmit={handleSubmit}>
            <div style={styles.section}>
              <h2 style={styles.sectionTitle}>정보 수정</h2>
              
              <div style={styles.formGroup}>
                <label style={styles.formLabel}>이름</label>
                <input
                  type="text"
                  name="name"
                  value={formData.name}
                  onChange={handleChange}
                  style={styles.input}
                  required
                />
              </div>
              
              <div style={styles.formGroup}>
                <label style={styles.formLabel}>전화번호</label>
                <input
                  type="tel"
                  name="phone"
                  value={formData.phone}
                  onChange={handleChange}
                  style={styles.input}
                  placeholder="010-1234-5678"
                />
              </div>
              
              <div style={styles.formGroup}>
                <label style={styles.formLabel}>주소</label>
                <input
                  type="text"
                  name="address"
                  value={formData.address}
                  onChange={handleChange}
                  style={styles.input}
                  placeholder="서울시 강남구..."
                />
              </div>
            </div>

            <div style={styles.buttonGroup}>
              <button type="submit" style={styles.saveButton}>
                저장
              </button>
              <button
                type="button"
                onClick={() => {
                  setIsEditing(false);
                  setFormData({
                    name: user.name || '',
                    phone: user.phone || '',
                    address: user.address || '',
                  });
                }}
                style={styles.cancelButton}
              >
                취소
              </button>
            </div>
          </form>
        )}
      </div>

      {/* 빠른 링크 */}
      <div style={styles.quickLinks}>
        <h2 style={styles.sectionTitle}>빠른 이동</h2>
        <div style={styles.linkGrid}>
          <button onClick={() => navigate('/orders')} style={styles.linkButton}>
            📦 주문 내역
          </button>
          <button onClick={() => navigate('/coupons')} style={styles.linkButton}>
            🎟️ 내 쿠폰
          </button>
          <button onClick={() => navigate('/notifications')} style={styles.linkButton}>
            🔔 알림
          </button>
          <button onClick={() => navigate('/reviews')} style={styles.linkButton}>
            ⭐ 내 리뷰
          </button>
          <button onClick={() => navigate('/restock')} style={styles.linkButton}>
            🔄 재입고 알림
          </button>
          {user.role === 'ROLE_USER' && (
            <button onClick={() => navigate('/seller/register')} style={styles.linkButton}>
              🏪 판매자 되기
            </button>
          )}
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
  card: {
    backgroundColor: 'white',
    borderRadius: '8px',
    padding: '30px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
    marginBottom: '20px',
  },
  section: {
    marginBottom: '30px',
    paddingBottom: '30px',
    borderBottom: '1px solid #e9ecef',
  },
  sectionTitle: {
    fontSize: '20px',
    fontWeight: 'bold',
    marginBottom: '20px',
    color: '#333',
  },
  infoRow: {
    display: 'flex',
    padding: '12px 0',
    borderBottom: '1px solid #f8f9fa',
  },
  label: {
    width: '150px',
    fontWeight: '600',
    color: '#666',
  },
  value: {
    flex: 1,
    color: '#333',
  },
  formGroup: {
    marginBottom: '20px',
  },
  formLabel: {
    display: 'block',
    marginBottom: '8px',
    fontSize: '14px',
    fontWeight: '600',
    color: '#333',
  },
  input: {
    width: '100%',
    padding: '12px',
    fontSize: '16px',
    border: '1px solid #ddd',
    borderRadius: '6px',
    boxSizing: 'border-box',
  },
  buttonGroup: {
    display: 'flex',
    gap: '10px',
    marginTop: '20px',
  },
  editButton: {
    flex: 1,
    padding: '12px 24px',
    fontSize: '16px',
    fontWeight: 'bold',
    color: 'white',
    backgroundColor: '#007bff',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
  },
  deleteButton: {
    flex: 1,
    padding: '12px 24px',
    fontSize: '16px',
    fontWeight: 'bold',
    color: '#dc3545',
    backgroundColor: 'white',
    border: '1px solid #dc3545',
    borderRadius: '6px',
    cursor: 'pointer',
  },
  saveButton: {
    flex: 1,
    padding: '12px 24px',
    fontSize: '16px',
    fontWeight: 'bold',
    color: 'white',
    backgroundColor: '#28a745',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
  },
  cancelButton: {
    flex: 1,
    padding: '12px 24px',
    fontSize: '16px',
    fontWeight: 'bold',
    color: '#666',
    backgroundColor: 'white',
    border: '1px solid #ddd',
    borderRadius: '6px',
    cursor: 'pointer',
  },
  quickLinks: {
    backgroundColor: 'white',
    borderRadius: '8px',
    padding: '30px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
  },
  linkGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))',
    gap: '15px',
  },
  linkButton: {
    padding: '15px 20px',
    fontSize: '16px',
    fontWeight: '500',
    color: '#333',
    backgroundColor: '#f8f9fa',
    border: '1px solid #e9ecef',
    borderRadius: '6px',
    cursor: 'pointer',
    transition: 'all 0.2s',
  },
};

export default ProfilePage;