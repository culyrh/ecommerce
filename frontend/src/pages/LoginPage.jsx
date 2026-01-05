import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import apiService from '../services/api';
import { saveTokens } from '../utils/auth';

const LoginPage = () => {
    const navigate = useNavigate();
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const BACKEND_URL = process.env.REACT_APP_BACKEND_URL || 'http://localhost:8080';
    const NAVER_CLIENT_ID = process.env.REACT_APP_NAVER_CLIENT_ID;

    // 일반 로그인
    const handleEmailLogin = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            const response = await fetch(`${BACKEND_URL}/api/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ email, password }),
            });

            if (!response.ok) {
                throw new Error('로그인 실패');
            }

            const data = await response.json();
        
            // 수정 후:
            saveTokens(data);
        
            alert('로그인 성공!');
            navigate('/');
        } catch (err) {
            setError('이메일 또는 비밀번호가 올바르지 않습니다.');
        } finally {
            setLoading(false);
        }
    };

    // 네이버 로그인
    const handleNaverLogin = () => {
        const state = Math.random().toString(36).substring(2, 15);
        localStorage.setItem('naver_oauth_state', state);
        
        const naverAuthUrl = 
            `https://nid.naver.com/oauth2.0/authorize?` +
            `response_type=code` +
            `&client_id=${NAVER_CLIENT_ID}` +
            `&redirect_uri=${encodeURIComponent(window.location.origin + '/oauth/callback')}` +
            `&state=${state}`;
        
        window.location.href = naverAuthUrl;
    };

    return (
        <div style={styles.container}>
            <div style={styles.loginBox}>
                <h1 style={styles.title}>이커머스 로그인</h1>
                
                {/* 일반 로그인 폼 */}
                <form onSubmit={handleEmailLogin} style={styles.form}>
                    <input
                        type="email"
                        placeholder="이메일"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        style={styles.input}
                        required
                    />
                    <input
                        type="password"
                        placeholder="비밀번호"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        style={styles.input}
                        required
                    />
                    
                    {error && <p style={styles.error}>{error}</p>}
                    
                    <button 
                        type="submit" 
                        style={styles.loginButton}
                        disabled={loading}
                    >
                        {loading ? '로그인 중...' : '일반 로그인'}
                    </button>
                </form>

                {/* 회원가입 링크 */}
                <p style={styles.signupText}>
                    계정이 없으신가요?{' '}
                    <span 
                        onClick={() => navigate('/register')}
                        style={styles.signupLink}
                    >
                        회원가입
                    </span>
                </p>

                {/* 구분선 */}
                <div style={styles.divider}>
                    <span style={styles.dividerText}>또는</span>
                </div>

                {/* 소셜 로그인 버튼들 */}
                <div style={styles.socialButtons}>
                    {/* 네이버 로그인 (기존 유지) */}
                    <button 
                        onClick={handleNaverLogin}
                        style={styles.naverButton}
                    >
                        <svg viewBox="0 0 20 20" style={styles.icon}>
                            <path d="M13.2 10L7.8 0H0v20h6.8V10L12.2 20H20V0h-6.8z" fill="white"/>
                        </svg>
                        네이버로 로그인
                    </button>
                </div>
            </div>
        </div>
    );
};

// 스타일
const styles = {
    container: {
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        minHeight: '100vh',
        backgroundColor: '#f5f5f5',
        padding: '20px',
    },
    loginBox: {
        backgroundColor: 'white',
        padding: '40px',
        borderRadius: '12px',
        boxShadow: '0 4px 20px rgba(0,0,0,0.1)',
        width: '100%',
        maxWidth: '400px',
    },
    title: {
        textAlign: 'center',
        marginBottom: '30px',
        color: '#333',
    },
    form: {
        display: 'flex',
        flexDirection: 'column',
        gap: '15px',
    },
    input: {
        padding: '12px 15px',
        fontSize: '16px',
        border: '1px solid #ddd',
        borderRadius: '6px',
        outline: 'none',
    },
    loginButton: {
        padding: '12px',
        fontSize: '16px',
        fontWeight: 'bold',
        backgroundColor: '#007bff',
        color: 'white',
        border: 'none',
        borderRadius: '6px',
        cursor: 'pointer',
        marginTop: '10px',
    },
    error: {
        color: '#dc3545',
        fontSize: '14px',
        margin: '0',
        textAlign: 'center',
    },
    signupText: {
        textAlign: 'center',
        marginTop: '20px',
        color: '#666',
        fontSize: '14px',
    },
    signupLink: {
        color: '#007bff',
        cursor: 'pointer',
        fontWeight: 'bold',
        textDecoration: 'underline',
    },
    divider: {
        display: 'flex',
        alignItems: 'center',
        margin: '30px 0',
    },
    dividerText: {
        padding: '0 10px',
        color: '#999',
        fontSize: '14px',
        backgroundColor: 'white',
        position: 'relative',
        zIndex: 1,
        margin: '0 auto',
    },
    socialButtons: {
        display: 'flex',
        flexDirection: 'column',
        gap: '12px',
    },
    naverButton: {
        padding: '12px',
        fontSize: '16px',
        fontWeight: 'bold',
        backgroundColor: '#03c75a',
        color: 'white',
        border: 'none',
        borderRadius: '6px',
        cursor: 'pointer',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '10px',
    },
    icon: {
        width: '20px',
        height: '20px',
    },
};

export default LoginPage;