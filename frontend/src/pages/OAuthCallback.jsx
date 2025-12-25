import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const OAuthCallback = () => {
    const navigate = useNavigate();
    const BACKEND_URL = process.env.REACT_APP_BACKEND_URL || 'http://localhost:8080';

    useEffect(() => {
        const handleCallback = async () => {
            const urlParams = new URLSearchParams(window.location.search);
            const code = urlParams.get('code');
            const state = urlParams.get('state');
            const error = urlParams.get('error');

            // 에러 처리
            if (error) {
                alert('로그인에 실패했습니다: ' + error);
                navigate('/login');
                return;
            }

            if (code && state) {
                // state 검증
                const savedState = localStorage.getItem('naver_oauth_state');
                if (state !== savedState) {
                    alert('잘못된 요청입니다. (state 불일치)');
                    navigate('/login');
                    return;
                }

                try {
                    // 네이버 OAuth 백엔드 API 호출
                    const response = await fetch(
                        `${BACKEND_URL}/api/auth/naver?code=${code}&state=${state}`
                    );

                    if (!response.ok) {
                        throw new Error('로그인 실패');
                    }

                    const data = await response.json();

                    // JWT 토큰 저장
                    localStorage.setItem('accessToken', data.accessToken);
                    localStorage.setItem('refreshToken', data.refreshToken);
                    
                    // state 제거
                    localStorage.removeItem('naver_oauth_state');
                    
                    alert('로그인 성공!');
                    
                    // 메인 페이지로 이동
                    navigate('/');
                    
                } catch (error) {
                    console.error('Error:', error);
                    alert('로그인 처리 중 오류가 발생했습니다.');
                    navigate('/login');
                }
            }
        };

        handleCallback();
    }, [navigate, BACKEND_URL]);

    return (
        <div style={styles.container}>
            <div style={styles.loadingBox}>
                <div style={styles.spinner}></div>
                <p style={styles.text}>로그인 처리 중...</p>
            </div>
        </div>
    );
};

const styles = {
    container: {
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        backgroundColor: '#f5f5f5',
    },
    loadingBox: {
        textAlign: 'center',
        backgroundColor: 'white',
        padding: '40px',
        borderRadius: '12px',
        boxShadow: '0 4px 20px rgba(0,0,0,0.1)',
    },
    spinner: {
        width: '50px',
        height: '50px',
        margin: '0 auto 20px',
        border: '5px solid #f3f3f3',
        borderTop: '5px solid #007bff',
        borderRadius: '50%',
        animation: 'spin 1s linear infinite',
    },
    text: {
        color: '#666',
        fontSize: '16px',
    },
    // CSS 애니메이션은 인라인 스타일에서 작동 안 함
    // 대신 index.css에 추가하거나 styled-components 사용
};

export default OAuthCallback;