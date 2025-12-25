// frontend/src/utils/auth.js

export const saveTokens = (tokenResponse) => {
  localStorage.setItem('accessToken', tokenResponse.accessToken);
  localStorage.setItem('refreshToken', tokenResponse.refreshToken);
  localStorage.setItem('tokenExpiry', String(Date.now() + tokenResponse.expiresIn));
};

export const getAccessToken = () => {
  return localStorage.getItem('accessToken');
};

export const getRefreshToken = () => {
  return localStorage.getItem('refreshToken');
};

export const isTokenExpired = () => {
  const expiry = localStorage.getItem('tokenExpiry');
  if (!expiry) return true;
  return Date.now() > parseInt(expiry);
};

export const clearTokens = () => {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('tokenExpiry');
};

export const isAuthenticated = () => {
  const token = getAccessToken();
  return !!token && !isTokenExpired();
};