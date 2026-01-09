// frontend/src/services/api.js
const BACKEND_URL = process.env.REACT_APP_BACKEND_URL || 'http://localhost:8080';
const API_BASE_URL = `${BACKEND_URL}/api`;

class ApiService {
  getHeaders(includeAuth = false) {
    const headers = {
      'Content-Type': 'application/json',
    };

    if (includeAuth) {
      const token = localStorage.getItem('accessToken');
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
    }

    return headers;
  }

  async handleResponse(response) {
    if (!response.ok) {
      const error = await response.json().catch(() => ({ 
        message: 'Unknown error',
        code: 'UNKNOWN_ERROR' 
      }));
      const err = new Error(error.message || `HTTP ${response.status}`);
      err.code = error.code;
      err.status = response.status;
      throw err;
    }
    
    // 204 No Content 처리
    if (response.status === 204) {
      return null;
    }
    
    return response.json();
  }

  // ==================== AUTH ====================
  
  async register(data) {
    const response = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(data),
    });
    return this.handleResponse(response);
  }

  async login(data) {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(data),
    });
    return this.handleResponse(response);
  }

  // ==================== USER ====================
  
  async getCurrentUser() {
    const response = await fetch(`${API_BASE_URL}/users/me`, {
      method: 'GET',
      headers: this.getHeaders(true),
    });
    return this.handleResponse(response);
  }

  async updateUser(data) {
    const response = await fetch(`${API_BASE_URL}/users/me`, {
      method: 'PUT',
      headers: this.getHeaders(true),
      body: JSON.stringify(data),
    });
    return this.handleResponse(response);
  }

  async deleteAccount() {
    const response = await fetch(`${API_BASE_URL}/users/me`, {
      method: 'DELETE',
      headers: this.getHeaders(true),
    });
    return this.handleResponse(response);
  }

  // ==================== SELLER ====================
  
  async registerSeller(data) {
    const response = await fetch(`${API_BASE_URL}/sellers`, {
      method: 'POST',
      headers: this.getHeaders(true),
      body: JSON.stringify(data),
    });
    return this.handleResponse(response);
  }

  async getMySeller() {
    const response = await fetch(`${API_BASE_URL}/sellers/me`, {
      method: 'GET',
      headers: this.getHeaders(true),
    });
    return this.handleResponse(response);
  }

  async getDashboard() {
    const response = await fetch(`${API_BASE_URL}/sellers/me/dashboard`, {
      method: 'GET',
      headers: this.getHeaders(true),
    });
    return this.handleResponse(response);
  }

  // ==================== PRODUCT ====================
  
  async getMyProducts(page = 0, size = 20) {
    const response = await fetch(
      `${API_BASE_URL}/products/my?page=${page}&size=${size}`,
      {
        method: 'GET',
        headers: this.getHeaders(true),
      }
    );
    return this.handleResponse(response);
  }

  async getProducts(params = {}) {
    const queryString = new URLSearchParams(params).toString();
    const response = await fetch(`${API_BASE_URL}/products?${queryString}`, {
      method: 'GET',
      headers: this.getHeaders(),
    });
    return this.handleResponse(response);
  }

  async getProduct(id) {
    const response = await fetch(`${API_BASE_URL}/products/${id}`, {
      method: 'GET',
      headers: this.getHeaders(),
    });
    return this.handleResponse(response);
  }

  async createProduct(data) {
    const response = await fetch(`${API_BASE_URL}/products`, {
      method: 'POST',
      headers: this.getHeaders(true),
      body: JSON.stringify(data),
    });
    return this.handleResponse(response);
  }

  async updateProduct(id, data) {
    const response = await fetch(`${API_BASE_URL}/products/${id}`, {
      method: 'PUT',
      headers: this.getHeaders(true),
      body: JSON.stringify(data),
    });
    return this.handleResponse(response);
  }

  async deleteProduct(id) {
    const response = await fetch(`${API_BASE_URL}/products/${id}`, {
      method: 'DELETE',
      headers: this.getHeaders(true),
    });
    return this.handleResponse(response);
  }

  async updateStock(id, quantity) {
    const response = await fetch(`${API_BASE_URL}/products/${id}/stock`, {
      method: 'PUT',
      headers: this.getHeaders(true),
      body: JSON.stringify({ quantity }),
    });
    return this.handleResponse(response);
  }

  async searchNaverProducts(keyword, display = 10) {
    const response = await fetch(
      `${API_BASE_URL}/products/naver/search?keyword=${encodeURIComponent(keyword)}&display=${display}`,
      {
        method: 'GET',
        headers: this.getHeaders(true),
      }
    );
    return this.handleResponse(response);
  }

  // ==================== CATEGORY ====================
  
  async getCategories() {
    const response = await fetch(`${API_BASE_URL}/categories`, {
      method: 'GET',
      headers: this.getHeaders(),
    });
    return this.handleResponse(response);
  }

  // ==================== CART ====================
  
  async addToCart(productId, quantity) {
    const response = await fetch(`${API_BASE_URL}/cart`, {
      method: 'POST',
      headers: this.getHeaders(true),
      body: JSON.stringify({ productId, quantity }),
    });
    return this.handleResponse(response);
  }

  async getMyCart() {
    const response = await fetch(`${API_BASE_URL}/cart`, {
      method: 'GET',
      headers: this.getHeaders(true),
    });
    return this.handleResponse(response);
  }

  async getCartCount() {
    const response = await fetch(`${API_BASE_URL}/cart/count`, {
      method: 'GET',
      headers: this.getHeaders(true),
    });
    return this.handleResponse(response);
  }

  async updateCartItem(id, quantity) {
    const response = await fetch(`${API_BASE_URL}/cart/${id}`, {
      method: 'PUT',
      headers: this.getHeaders(true),
      body: JSON.stringify({ quantity }),
    });
    return this.handleResponse(response);
  }

  async removeCartItem(id) {
    const response = await fetch(`${API_BASE_URL}/cart/${id}`, {
      method: 'DELETE',
      headers: this.getHeaders(true),
    });
    return this.handleResponse(response);
  }

  async clearCart() {
    const response = await fetch(`${API_BASE_URL}/cart`, {
      method: 'DELETE',
      headers: this.getHeaders(true),
    });
    return this.handleResponse(response);
  }

  // ==================== ORDER ====================
  
  async createOrder(data) {
    const response = await fetch(`${API_BASE_URL}/orders`, {
      method: 'POST',
      headers: this.getHeaders(true),
      body: JSON.stringify(data),
    });
    return this.handleResponse(response);
  }

  async getMyOrders(page = 0, size = 20) {
    const response = await fetch(
      `${API_BASE_URL}/orders?page=${page}&size=${size}`,
      {
        method: 'GET',
        headers: this.getHeaders(true),
      }
    );
    return this.handleResponse(response);
  }

  async getOrder(id) {
    const response = await fetch(`${API_BASE_URL}/orders/${id}`, {
      method: 'GET',
      headers: this.getHeaders(true),
    });
    return this.handleResponse(response);
  }

  // ==================== REVIEW ====================
  
  async getProductReviews(productId, page = 0, size = 20) {
    const response = await fetch(
      `${API_BASE_URL}/products/${productId}/reviews?page=${page}&size=${size}`,
      {
        method: 'GET',
        headers: this.getHeaders(),
      }
    );
    return this.handleResponse(response);
  }

  async createReview(data) {
    const response = await fetch(`${API_BASE_URL}/reviews`, {
      method: 'POST',
      headers: this.getHeaders(true),
      body: JSON.stringify(data),
    });
    return this.handleResponse(response);
  }

  async updateReview(id, data) {
    const response = await fetch(`${API_BASE_URL}/reviews/${id}`, {
      method: 'PUT',
      headers: this.getHeaders(true),
      body: JSON.stringify(data),
    });
    return this.handleResponse(response);
  }

  async deleteReview(id) {
    const response = await fetch(`${API_BASE_URL}/reviews/${id}`, {
      method: 'DELETE',
      headers: this.getHeaders(true),
    });
    return this.handleResponse(response);
  }

  // ==================== RESTOCK ====================
  
  async voteRestock(productId) {
    const response = await fetch(`${API_BASE_URL}/restock-votes`, {
      method: 'POST',
      headers: this.getHeaders(true),
      body: JSON.stringify({ productId }),
    });
    return this.handleResponse(response);
  }

  async getMyRestockVotes(page = 0, size = 20) {
    const response = await fetch(
      `${API_BASE_URL}/restock-votes/my?page=${page}&size=${size}`,
      {
        method: 'GET',
        headers: this.getHeaders(true),
      }
    );
    return this.handleResponse(response);
  }

  async requestRestockNotification(productId) {
    const response = await fetch(`${API_BASE_URL}/restock-notifications`, {
      method: 'POST',
      headers: this.getHeaders(true),
      body: JSON.stringify({ productId }),
    });
    return this.handleResponse(response);
  }

  async getMyRestockNotifications(page = 0, size = 20) {
    const response = await fetch(
      `${API_BASE_URL}/restock-notifications/my?page=${page}&size=${size}`,
      {
        method: 'GET',
        headers: this.getHeaders(true),
      }
    );
    return this.handleResponse(response);
  }

  // ==================== NOTIFICATION ====================
  
  async getMyNotifications(page = 0, size = 20) {
    const response = await fetch(
      `${API_BASE_URL}/notifications?page=${page}&size=${size}`,
      {
        method: 'GET',
        headers: this.getHeaders(true),
      }
    );
    return this.handleResponse(response);
  }

  async markNotificationAsRead(id) {
    const response = await fetch(`${API_BASE_URL}/notifications/${id}`, {
      method: 'PUT',
      headers: this.getHeaders(true),
    });
    return this.handleResponse(response);
  }

  // ==================== COUPON ====================
  
  async getMyCoupons(page = 0, size = 20) {
    const response = await fetch(
      `${API_BASE_URL}/user-coupons/my?page=${page}&size=${size}`,
      {
        method: 'GET',
        headers: this.getHeaders(true),
      }
    );
    return this.handleResponse(response);
  }
}

export const apiService = new ApiService();
export default apiService;