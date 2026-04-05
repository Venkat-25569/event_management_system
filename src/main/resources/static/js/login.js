  const API_URL = 'http://localhost:8080/api';

// Check if already logged in
document.addEventListener('DOMContentLoaded', function() {
    const token = localStorage.getItem('token');
    if (token) {
        // Already logged in, redirect to dashboard
        window.location.href = 'pages/dashboard.html';
    }
});

// Login form handler
document.getElementById('loginForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    
    console.log('Login form submitted');
    
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    
    console.log('Email:', email);
    console.log('Password length:', password.length);
    
    // Validate inputs
    if (!email || !password) {
        showAlert('Please enter both email and password', 'warning');
        return;
    }
    
    // Disable submit button to prevent double submission
    const loginButton = document.getElementById('loginButton');
    const originalText = loginButton.innerHTML;
    loginButton.disabled = true;
    loginButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Logging in...';
    
    try {
        console.log('Sending login request to:', `${API_URL}/auth/login`);
        
        const response = await fetch(`${API_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                email: email,
                password: password
            })
        });
        
        console.log('Response status:', response.status);
        
        const data = await response.json();
        console.log('Response data:', data);
        
        if (response.ok && data.token) {
            // Success - store user data
            localStorage.setItem('token', data.token);
            localStorage.setItem('email', data.email);
            localStorage.setItem('fullName', data.fullName);
            localStorage.setItem('role', data.role);
            localStorage.setItem('username', data.username);
            
            console.log('Login successful!');
            console.log('Stored token:', data.token);
            console.log('Stored role:', data.role);
            
            showAlert('✅ Login successful! Redirecting...', 'success');
            
            // Redirect after 1 second
            setTimeout(() => {
                window.location.href = 'pages/dashboard.html';
            }, 1000);
            
        } else {
            // Login failed
            console.error('Login failed:', data);
            showAlert('❌ ' + (data.error || data.message || 'Invalid email or password'), 'danger');
            
            // Re-enable button
            loginButton.disabled = false;
            loginButton.innerHTML = originalText;
        }
        
    } catch (error) {
        console.error('Login error:', error);
        showAlert('❌ Login failed. Please check if the server is running.', 'danger');
        
        // Re-enable button
        loginButton.disabled = false;
        loginButton.innerHTML = originalText;
    }
});

// Show alert function
function showAlert(message, type) {
    const alertContainer = document.getElementById('alertContainer');
    alertContainer.innerHTML = `
        <div class="alert alert-${type} alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
    
    // Auto-dismiss after 5 seconds
    setTimeout(() => {
        const alert = alertContainer.querySelector('.alert');
        if (alert) {
            alert.classList.remove('show');
            setTimeout(() => alertContainer.innerHTML = '', 300);
        }
    }, 5000);
}