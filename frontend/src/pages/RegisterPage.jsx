import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

const RegisterPage = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({
        email: '',
        password: '',
        confirmPassword: '',
        name: '',
        phone: '',
        birthDate: '',
    });
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const BACKEND_URL = process.env.REACT_APP_BACKEND_URL || 'http://localhost:8080';

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value,
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        // 비밀번호 확인
        if (formData.password !== formData.confirmPassword) {
            setError('비밀번호가 일치하지 않습니다.');
            return;
        }

        // 비밀번호 길이 확인
        if (formData.password.length < 8) {
            setError('비밀번호는 8자 이상이어야 합니다.');
            return;
        }

        setLoading(true);

        try {
            const response = await fetch(`${BACKEND_URL}/api/auth/register`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    email: formData.email,
                    password: formData.password,
                    name: formData.name,
                    phone: formData.phone,
                    birthDate: formData.birthDate,
                }),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || '회원가입 실패');
            }

            const data = await response.json();
            
            // 회원가입 성공 시 자동 로그인
            localStorage.setItem('accessToken', data.accessToken);
            localStorage.setItem('refreshToken', data.refreshToken);
            
            alert('회원가입 성공!');
            navigate('/');
        } catch (err) {
            setError(err.message || '회원가입 중 오류가 발생했습니다.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={styles.container}>
            <div style={styles.registerBox}>
                <h1 style={styles.title}>🛒 회원가입</h1>
                
                <form onSubmit={handleSubmit} style={styles.form}>
                    <input
                        type="email"
                        name="email"
                        placeholder="이메일 *"
                        value={formData.email}
                        onChange={handleChange}
                        style={styles.input}
                        required
                    />
                    
                    <input
                        type="password"
                        name="password"
                        placeholder="비밀번호 (8자 이상) *"
                        value={formData.password}
                        onChange={handleChange}
                        style={styles.input}
                        required
                    />
                    
                    <input
                        type="password"
                        name="confirmPassword"
                        placeholder="비밀번호 확인 *"
                        value={formData.confirmPassword}
                        onChange={handleChange}
                        style={styles.input}
                        required
                    />
                    
                    <input
                        type="text"
                        name="name"
                        placeholder="이름 *"
                        value={formData.name}
                        onChange={handleChange}
                        style={styles.input}
                        required
                    />
                    
                    <input
                        type="tel"
                        name="phone"
                        placeholder="전화번호 (예: 010-1234-5678)"
                        value={formData.phone}
                        onChange={handleChange}
                        style={styles.input}
                    />
                    
                    <input
                        type="date"
                        name="birthDate"
                        placeholder="생년월일"
                        value={formData.birthDate}
                        onChange={handleChange}
                        style={styles.input}
                    />
                    
                    {error && <p style={styles.error}>{error}</p>}
                    
                    <button 
                        type="submit" 
                        style={styles.registerButton}
                        disabled={loading}
                    >
                        {loading ? '가입 중...' : '회원가입'}
                    </button>
                </form>

                <p style={styles.loginText}>
                    이미 계정이 있으신가요?{' '}
                    <span 
                        onClick={() => navigate('/login')}
                        style={styles.loginLink}
                    >
                        로그인
                    </span>
                </p>
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
    registerBox: {
        backgroundColor: 'white',
        padding: '40px',
        borderRadius: '12px',
        boxShadow: '0 4px 20px rgba(0,0,0,0.1)',
        width: '100%',
        maxWidth: '450px',
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
    registerButton: {
        padding: '14px',
        fontSize: '16px',
        fontWeight: 'bold',
        backgroundColor: '#28a745',
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
    loginText: {
        textAlign: 'center',
        marginTop: '20px',
        color: '#666',
        fontSize: '14px',
    },
    loginLink: {
        color: '#007bff',
        cursor: 'pointer',
        fontWeight: 'bold',
        textDecoration: 'underline',
    },
};

export default RegisterPage;