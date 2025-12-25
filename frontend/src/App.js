import React from 'react';
import { BrowserRouter, Routes, Route, Link, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import OAuthCallback from './pages/OAuthCallback';

// 홈 페이지 컴포넌트
function Home() {
  const accessToken = localStorage.getItem('accessToken');
  
  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    window.location.reload();
  };
  
  return (
    <div style={styles.container}>
      <div style={styles.header}>
        <h1>이커머스 플랫폼</h1>
        {accessToken && (
          <button onClick={handleLogout} style={styles.logoutBtn}>
            로그아웃
          </button>
        )}
      </div>
      
      {accessToken ? (
        <div style={styles.content}>
          <div style={styles.successBox}>
            <h2>✅ 로그인 성공!</h2>
            <div style={styles.tokenBox}>
              <p><strong>Access Token:</strong></p>
              <code>{accessToken.substring(0, 50)}...</code>
            </div>
            <p style={styles.welcomeText}>환영합니다! 쇼핑을 시작해보세요.</p>
          </div>
        </div>
      ) : (
        <div style={styles.content}>
          <div style={styles.welcomeBox}>
            <h2>환영합니다!</h2>
            <p>로그인하고 다양한 상품을 만나보세요</p>
            <div style={styles.buttonGroup}>
              <Link to="/login" style={styles.link}>
                <button style={styles.loginButton}>로그인</button>
              </Link>
              <Link to="/register" style={styles.link}>
                <button style={styles.registerButton}>회원가입</button>
              </Link>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

// Protected Route (로그인 필요)
function ProtectedRoute({ children }) {
  const accessToken = localStorage.getItem('accessToken');
  return accessToken ? children : <Navigate to="/login" />;
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/oauth/callback" element={<OAuthCallback />} />
        
        {/* 로그인 필요한 페이지 예시 */}
        {/* <Route path="/mypage" element={
          <ProtectedRoute>
            <MyPage />
          </ProtectedRoute>
        } /> */}
      </Routes>
    </BrowserRouter>
  );
}

// 스타일
const styles = {
  container: {
    minHeight: '100vh',
    backgroundColor: '#f5f5f5',
  },
  header: {
    backgroundColor: 'white',
    padding: '20px 40px',
    boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  content: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    minHeight: 'calc(100vh - 80px)',
    padding: '40px 20px',
  },
  successBox: {
    backgroundColor: 'white',
    padding: '40px',
    borderRadius: '12px',
    boxShadow: '0 4px 20px rgba(0,0,0,0.1)',
    textAlign: 'center',
    maxWidth: '600px',
    width: '100%',
  },
  welcomeBox: {
    backgroundColor: 'white',
    padding: '60px 40px',
    borderRadius: '12px',
    boxShadow: '0 4px 20px rgba(0,0,0,0.1)',
    textAlign: 'center',
    maxWidth: '500px',
    width: '100%',
  },
  tokenBox: {
    backgroundColor: '#f8f9fa',
    padding: '20px',
    borderRadius: '8px',
    margin: '20px 0',
    wordBreak: 'break-all',
    textAlign: 'left',
  },
  welcomeText: {
    marginTop: '20px',
    color: '#666',
    fontSize: '16px',
  },
  buttonGroup: {
    display: 'flex',
    gap: '15px',
    marginTop: '30px',
    justifyContent: 'center',
  },
  link: {
    textDecoration: 'none',
  },
  loginButton: {
    backgroundColor: '#007bff',
    color: 'white',
    border: 'none',
    padding: '12px 30px',
    fontSize: '16px',
    fontWeight: 'bold',
    borderRadius: '6px',
    cursor: 'pointer',
  },
  registerButton: {
    backgroundColor: '#28a745',
    color: 'white',
    border: 'none',
    padding: '12px 30px',
    fontSize: '16px',
    fontWeight: 'bold',
    borderRadius: '6px',
    cursor: 'pointer',
  },
  logoutBtn: {
    backgroundColor: '#dc3545',
    color: 'white',
    border: 'none',
    padding: '10px 20px',
    fontSize: '14px',
    fontWeight: 'bold',
    borderRadius: '6px',
    cursor: 'pointer',
  },
};

export default App;