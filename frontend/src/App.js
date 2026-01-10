import React, { useState, useEffect } from 'react';
import { BrowserRouter, Routes, Route, Link, Navigate, useNavigate } from 'react-router-dom';
import apiService from './services/api';

// 모든 페이지 import
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import OAuthCallback from './pages/OAuthCallback';
import ProductListPage from './pages/ProductListPage';
import ProductDetailPage from './pages/ProductDetailPage';
import BecomeSellerPage from './pages/BecomeSellerPage';
import SellerDashboardPage from './pages/SellerDashboardPage';
import SellerProductListPage from './pages/SellerProductListPage';
import ProductFormPage from './pages/ProductFormPage';
import OrderCreatePage from './pages/OrderCreatePage';
import OrderListPage from './pages/OrderListPage';
import OrderDetailPage from './pages/OrderDetailPage';

// 새로 추가된 페이지들
import CartPage from './pages/CartPage';
import CouponPage from './pages/CouponPage';
import NotificationPage from './pages/NotificationPage';
import ProfilePage from './pages/ProfilePage';
import MyReviewsPage from './pages/MyReviewsPage';
import RestockPage from './pages/RestockPage';
// 레이아웃 컴포넌트
function Layout({ children }) {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [cartCount, setCartCount] = useState(0);
  const [notificationCount, setNotificationCount] = useState(0);
  const accessToken = localStorage.getItem('accessToken');

  useEffect(() => {
    if (accessToken) {
      loadUser();
      loadCartCount();
      loadNotificationCount();
    }
  }, [accessToken]);

  const loadUser = async () => {
    try {
      const userData = await apiService.getCurrentUser();
      setUser(userData);
    } catch (err) {
      console.error('사용자 정보 로딩 실패:', err);
      if (err.status === 401) {
        handleLogout();
      }
    }
  };

  const loadCartCount = async () => {
    try {
      const count = await apiService.getCartCount();
      setCartCount(count || 0);
    } catch (err) {
      console.error('장바구니 카운트 로딩 실패:', err);
      setCartCount(0);
    }
  };

  const loadNotificationCount = async () => {
    try {
      const data = await apiService.getMyNotifications(0, 100);
      const unreadCount = data.content.filter(n => !n.isRead).length;
      setNotificationCount(unreadCount);
    } catch (err) {
      // 알림 로딩 실패는 무시 (서버 문제일 수 있음)
      console.error('알림 로딩 실패:', err);
      setNotificationCount(0);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    setUser(null);
    navigate('/');
    window.location.reload();
  };

  return (
    <div style={styles.layout}>
      <nav style={styles.navbar}>
        <div style={styles.navContainer}>
          <Link to="/" style={styles.logo}>
            🛒 이커머스
          </Link>

          <div style={styles.navLinks}>
            <Link to="/products" style={styles.navLink}>
              상품 목록
            </Link>

            {accessToken && user ? (
              <>
                {/* 장바구니 */}
                <Link to="/cart" style={styles.navLinkWithBadge}>
                  🛒 장바구니
                  {cartCount > 0 && (
                    <span style={styles.badge}>{cartCount}</span>
                  )}
                </Link>

                {/* 알림 */}
                <Link to="/notifications" style={styles.navLinkWithBadge}>
                  🔔 알림
                  {notificationCount > 0 && (
                    <span style={styles.badge}>{notificationCount}</span>
                  )}
                </Link>

                {user.roles?.includes('ROLE_USER') && !user.roles?.includes('ROLE_SELLER') && (
                  <>
                    <Link to="/seller/register" style={styles.navLinkPrimary}>
                      판매자 되기
                    </Link>
                  </>
                )}

                {(user.roles?.includes('ROLE_SELLER') || user.roles?.includes('ROLE_ADMIN')) && (
                  <>
                    <Link to="/seller/dashboard" style={styles.navLink}>
                      대시보드
                    </Link>
                    <Link to="/seller/products" style={styles.navLink}>
                      상품 관리
                    </Link>
                  </>
                )}

                <div style={styles.userInfo}>
                  <Link to="/profile" style={styles.userName}>
                    {user.name}님
                  </Link>
                  <button onClick={handleLogout} style={styles.logoutBtn}>
                    로그아웃
                  </button>
                </div>
              </>
            ) : (
              <>
                <Link to="/login" style={styles.navLink}>
                  로그인
                </Link>
                <Link to="/register" style={styles.navLinkPrimary}>
                  회원가입
                </Link>
              </>
            )}
          </div>
        </div>
      </nav>

      <main style={styles.main}>
        {children}
      </main>

      <footer style={styles.footer}>
        <div style={styles.footerContent}>
          <p>© 2026 이커머스 플랫폼. All rights reserved.</p>
        </div>
      </footer>
    </div>
  );
}

// 홈 페이지
function HomePage() {
  const navigate = useNavigate();

  return (
    <div style={styles.homePage}>
      <div style={styles.hero}>
        <h1 style={styles.heroTitle}>환영합니다!</h1>
        <p style={styles.heroSubtitle}>
          다양한 상품을 둘러보고, 원하는 상품을 구매해보세요
        </p>
        <div style={styles.heroButtons}>
          <button
            onClick={() => navigate('/products')}
            style={styles.primaryButton}
          >
            상품 둘러보기
          </button>
          <button
            onClick={() => navigate('/seller/register')}
            style={styles.secondaryButton}
          >
            판매자 되기
          </button>
        </div>
      </div>

      <div style={styles.features}>
        <div style={styles.featureCard}>
          <div style={styles.featureIcon}>🛍️</div>
          <h3 style={styles.featureTitle}>다양한 상품</h3>
          <p style={styles.featureText}>
            수천 개의 상품 중에서 원하는 것을 찾아보세요
          </p>
        </div>
        <div style={styles.featureCard}>
          <div style={styles.featureIcon}>🚚</div>
          <h3 style={styles.featureTitle}>빠른 배송</h3>
          <p style={styles.featureText}>
            주문 후 빠르게 받아볼 수 있습니다
          </p>
        </div>
        <div style={styles.featureCard}>
          <div style={styles.featureIcon}>💰</div>
          <h3 style={styles.featureTitle}>판매자 지원</h3>
          <p style={styles.featureText}>
            누구나 쉽게 판매자가 될 수 있습니다
          </p>
        </div>
      </div>
    </div>
  );
}

// Protected Route
function ProtectedRoute({ children, requiredRole }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const accessToken = localStorage.getItem('accessToken');

  useEffect(() => {
    checkAuth();
  }, []);

  const checkAuth = async () => {
    if (!accessToken) {
      setLoading(false);
      return;
    }

    try {
      const userData = await apiService.getCurrentUser();
      setUser(userData);
    } catch (err) {
      console.error('인증 실패:', err);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div style={styles.loading}>인증 확인 중...</div>;
  }

  if (!accessToken || !user) {
    return <Navigate to="/login" />;
  }

  // requiredRole이 지정된 경우, 권한 체크 후 리다이렉트
  if (requiredRole) {
    // roles가 배열이므로 includes로 체크
    const hasRequiredRole = user.roles?.includes(requiredRole);
    const isAdmin = user.roles?.includes('ROLE_ADMIN');
    
    if (!hasRequiredRole && !isAdmin) {
      alert('접근 권한이 없습니다.');
      return <Navigate to="/" />;
    }
  }
  
  return children;
}

// 메인 App 컴포넌트
function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* 레이아웃 없는 페이지 */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/oauth/callback" element={<OAuthCallback />} />

        {/* 레이아웃 있는 페이지 */}
        <Route path="/*" element={
          <Layout>
            <Routes>
              <Route path="/" element={<HomePage />} />
              
              {/* 상품 관련 */}
              <Route path="/products" element={<ProductListPage />} />
              <Route path="/products/:id" element={<ProductDetailPage />} />

              {/* 장바구니 */}
              <Route path="/cart" element={
                <ProtectedRoute>
                  <CartPage />
                </ProtectedRoute>
              } />

              {/* 주문 관련 (로그인 필요) */}
              <Route path="/orders/create" element={
                <ProtectedRoute>
                  <OrderCreatePage />
                </ProtectedRoute>
              } />
              <Route path="/orders" element={
                <ProtectedRoute>
                  <OrderListPage />
                </ProtectedRoute>
              } />
              <Route path="/orders/:id" element={
                <ProtectedRoute>
                  <OrderDetailPage />
                </ProtectedRoute>
              } />

              {/* 사용자 페이지 (로그인 필요) */}
              <Route path="/profile" element={
                <ProtectedRoute>
                  <ProfilePage />
                </ProtectedRoute>
              } />
              <Route path="/coupons" element={
                <ProtectedRoute>
                  <CouponPage />
                </ProtectedRoute>
              } />
              <Route path="/notifications" element={
                <ProtectedRoute>
                  <NotificationPage />
                </ProtectedRoute>
              } />
              <Route path="/reviews" element={
                <ProtectedRoute>
                  <MyReviewsPage />
                </ProtectedRoute>
              } />
              <Route path="/restock" element={
                <ProtectedRoute>
                  <RestockPage />
                </ProtectedRoute>
              } />

              {/* 판매자 등록 (USER만) */}
              <Route path="/seller/register" element={
                <ProtectedRoute requiredRole="ROLE_USER">
                  <BecomeSellerPage />
                </ProtectedRoute>
              } />

              {/* 판매자 기능 (SELLER, ADMIN) */}
              <Route path="/seller/dashboard" element={
                <ProtectedRoute>
                  <SellerDashboardPage />
                </ProtectedRoute>
              } />
              <Route path="/seller/products" element={
                <ProtectedRoute>
                  <SellerProductListPage />
                </ProtectedRoute>
              } />
              <Route path="/seller/products/new" element={
                <ProtectedRoute>
                  <ProductFormPage />
                </ProtectedRoute>
              } />
              <Route path="/seller/products/:id/edit" element={
                <ProtectedRoute>
                  <ProductFormPage />
                </ProtectedRoute>
              } />
            </Routes>
          </Layout>
        } />
      </Routes>
    </BrowserRouter>
  );
}

// 스타일
const styles = {
  layout: {
    display: 'flex',
    flexDirection: 'column',
    minHeight: '100vh',
  },
  navbar: {
    backgroundColor: 'white',
    boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
    position: 'sticky',
    top: 0,
    zIndex: 1000,
  },
  navContainer: {
    maxWidth: '1200px',
    margin: '0 auto',
    padding: '15px 20px',
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  logo: {
    fontSize: '24px',
    fontWeight: 'bold',
    color: '#007bff',
    textDecoration: 'none',
  },
  navLinks: {
    display: 'flex',
    gap: '20px',
    alignItems: 'center',
    flexWrap: 'wrap',
  },
  navLink: {
    color: '#333',
    textDecoration: 'none',
    fontSize: '14px',
    fontWeight: '500',
  },
  navLinkWithBadge: {
    position: 'relative',
    color: '#333',
    textDecoration: 'none',
    fontSize: '14px',
    fontWeight: '500',
  },
  navLinkPrimary: {
    color: 'white',
    backgroundColor: '#007bff',
    padding: '8px 16px',
    borderRadius: '6px',
    textDecoration: 'none',
    fontSize: '14px',
    fontWeight: 'bold',
  },
  badge: {
    position: 'absolute',
    top: '-8px',
    right: '-8px',
    backgroundColor: '#dc3545',
    color: 'white',
    fontSize: '10px',
    fontWeight: 'bold',
    padding: '2px 6px',
    borderRadius: '10px',
    minWidth: '18px',
    textAlign: 'center',
  },
  userInfo: {
    display: 'flex',
    alignItems: 'center',
    gap: '10px',
  },
  userName: {
    fontSize: '14px',
    fontWeight: 'bold',
    color: '#333',
    textDecoration: 'none',
  },
  logoutBtn: {
    padding: '8px 16px',
    fontSize: '14px',
    fontWeight: 'bold',
    color: 'white',
    backgroundColor: '#dc3545',
    border: 'none',
    borderRadius: '6px',
    cursor: 'pointer',
  },
  main: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  footer: {
    backgroundColor: '#333',
    color: 'white',
    padding: '20px',
    marginTop: 'auto',
  },
  footerContent: {
    maxWidth: '1200px',
    margin: '0 auto',
    textAlign: 'center',
  },
  homePage: {
    maxWidth: '1200px',
    margin: '0 auto',
    padding: '40px 20px',
  },
  hero: {
    textAlign: 'center',
    padding: '60px 20px',
    backgroundColor: 'white',
    borderRadius: '12px',
    boxShadow: '0 4px 20px rgba(0,0,0,0.1)',
    marginBottom: '40px',
  },
  heroTitle: {
    fontSize: '48px',
    fontWeight: 'bold',
    marginBottom: '20px',
    color: '#333',
  },
  heroSubtitle: {
    fontSize: '20px',
    color: '#666',
    marginBottom: '40px',
  },
  heroButtons: {
    display: 'flex',
    gap: '20px',
    justifyContent: 'center',
  },
  primaryButton: {
    padding: '15px 40px',
    fontSize: '18px',
    fontWeight: 'bold',
    color: 'white',
    backgroundColor: '#007bff',
    border: 'none',
    borderRadius: '8px',
    cursor: 'pointer',
  },
  secondaryButton: {
    padding: '15px 40px',
    fontSize: '18px',
    fontWeight: 'bold',
    color: '#007bff',
    backgroundColor: 'white',
    border: '2px solid #007bff',
    borderRadius: '8px',
    cursor: 'pointer',
  },
  features: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
    gap: '30px',
  },
  featureCard: {
    backgroundColor: 'white',
    padding: '40px',
    borderRadius: '12px',
    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
    textAlign: 'center',
  },
  featureIcon: {
    fontSize: '60px',
    marginBottom: '20px',
  },
  featureTitle: {
    fontSize: '24px',
    fontWeight: 'bold',
    marginBottom: '10px',
    color: '#333',
  },
  featureText: {
    fontSize: '16px',
    color: '#666',
    lineHeight: '1.6',
  },
  loading: {
    textAlign: 'center',
    padding: '40px',
    fontSize: '18px',
    color: '#666',
  },
};

export default App;