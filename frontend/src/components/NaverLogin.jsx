import React, { useEffect } from 'react';

// Naver 로그인 컴포넌트
const NaverLogin = () => {
    // 환경변수에서 가져오기 (Docker 빌드 시 주입됨)
    const NAVER_CLIENT_ID = process.env.REACT_APP_NAVER_CLIENT_ID;
    const REDIRECT_URI = 'http://localhost:3000/oauth/callback';
    const BACKEND_API_URL = 'http://localhost:8080';

    // 랜덤 state 생성
    const generateState = () => {
        return Math.random().toString(36).substring(2, 15);
    };

    // 네이버 로그인 버튼 클릭
    const handleNaverLogin = () => {
        const state = generateState();

        // state를 localStorage에 저장
        localStorage.setItem('naver_oauth_state', state);

        // 네이버 로그인 페이지로 리다이렉트
        const naverAuthUrl =
            `https://nid.naver.com/oauth2.0/authorize?` +
            `response_type=code` +
            `&client_id=${NAVER_CLIENT_ID}` +
            `&redirect_uri=${encodeURIComponent(REDIRECT_URI)}` +
            `&state=${state}`;

        window.location.href = naverAuthUrl;
    };

    return (
        <div style={styles.container}>
            <h1>이커머스 플랫폼</h1>
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
    );
};

// Callback 처리 컴포넌트
const NaverCallback = () => {
    const BACKEND_API_URL = 'http://localhost:8080';

    useEffect(() => {
        const handleCallback = async () => {
            const urlParams = new URLSearchParams(window.location.search);
            const code = urlParams.get('code');
            const state = urlParams.get('state');
            const error = urlParams.get('error');

            // 에러 처리
            if (error) {
                alert('네이버 로그인에 실패했습니다: ' + error);
                window.location.href = '/login';
                return;
            }

            if (code && state) {
                // state 검증
                const savedState = localStorage.getItem('naver_oauth_state');
                if (state !== savedState) {
                    alert('잘못된 요청입니다. (state 불일치)');
                    window.location.href = '/login';
                    return;
                }

                try {
                    // 백엔드 API 호출
                    const response = await fetch(
                        `${BACKEND_API_URL}/api/auth/naver?code=${code}&state=${state}`
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
                    window.location.href = '/';

                } catch (error) {
                    console.error('Error:', error);
                    alert('로그인 처리 중 오류가 발생했습니다.');
                    window.location.href = '/login';
                }
            }
        };

        handleCallback();
    }, []);

    return (
        <div style={styles.loading}>
            <p>로그인 처리 중...</p>
        </div>
    );
};

// 스타일
const styles = {
    container: {
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        backgroundColor: '#f5f5f5',
    },
    naverButton: {
        backgroundColor: '#03c75a',
        color: 'white',
        border: 'none',
        padding: '15px 40px',
        fontSize: '16px',
        fontWeight: 'bold',
        borderRadius: '4px',
        cursor: 'pointer',
        display: 'flex',
        alignItems: 'center',
        gap: '10px',
    },
    icon: {
        width: '20px',
        height: '20px',
    },
    loading: {
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
    }
};

export { NaverLogin, NaverCallback };