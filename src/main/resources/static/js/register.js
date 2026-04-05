const API_URL = 'http://localhost:8080/api';

 document.getElementById('registerForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    
    const fullName = document.getElementById('fullName').value.trim();
    const username = document.getElementById('username').value.trim();
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const role = document.getElementById('role').value;
    const phone = document.getElementById('phone').value.trim();
    const department = document.getElementById('department').value.trim();
    
    // Validation
    if (password !== confirmPassword) {
        showAlert('Passwords do not match!', 'danger');
        return;
    }
    
    if (password.length < 6) {
        showAlert('Password must be at least 6 characters long!', 'warning');
        return;
    }
    
    if (!role) {
        showAlert('Please select a role!', 'warning');
        return;
    }
    
    // Validate role
    const validRoles = ['STUDENT', 'EVENT_COORDINATOR', 'HOD', 'ADMIN'];
    if (!validRoles.includes(role)) {
        showAlert('Invalid role selected!', 'danger');
        return;
    }
    
    try {
        const response = await fetch('http://localhost:8080/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                fullName: fullName,
                username: username,
                email: email,
                password: password,
                role: role,
                phone: phone,
                department: department
            })
        });
        
        const data = await response.json();
        
        if (response.ok) {
            showAlert('✅ Registration successful! Redirecting to login...', 'success');
            setTimeout(() => {
                window.location.href = 'login.html';
            }, 2000);
        } else {
            showAlert('❌ ' + (data.message || data.error || 'Registration failed'), 'danger');
        }
    } catch (error) {
        console.error('Registration error:', error);
        showAlert('❌ Failed to register. Please try again.', 'danger');
    }
});

function showAlert(message, type) {
    const alertContainer = document.getElementById('alertContainer');
    alertContainer.innerHTML = `
        <div class="alert alert-${type} alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    `;
}