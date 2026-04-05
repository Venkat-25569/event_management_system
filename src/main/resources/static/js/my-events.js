 const API_URL = 'http://localhost:8080/api';

function getAuthHeaders() {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
    };
}

function checkAuth() {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = '../login.html';
        return false;
    }
    return true;
}

function displayUserInfo() {
    const fullName = localStorage.getItem('fullName');
    const role = localStorage.getItem('role');
    const userInfoElement = document.querySelector('.navbar-text');
    if (userInfoElement && fullName) {
        userInfoElement.innerHTML = `<i class="fas fa-user"></i> ${fullName} (${role})`;
    }
}

function logout() {
    if (confirm('Are you sure you want to logout?')) {
        localStorage.clear();
        window.location.href = '../login.html';
    }
}

document.addEventListener('DOMContentLoaded', function() {
    if (!checkAuth()) return;
    displayUserInfo();
    
    // Show links for STUDENT
    const userRole = localStorage.getItem('role');
    if (userRole === 'STUDENT') {
        const myEventsLink = document.getElementById('myEventsLink');
        const paymentsLink = document.getElementById('paymentsLink');
        if (myEventsLink) myEventsLink.style.display = 'block';
        if (paymentsLink) paymentsLink.style.display = 'block';
    }
    
    loadMyEvents();
    loadAttendedEvents();
});

async function loadMyEvents() {
    const email = localStorage.getItem('email');
    const listDiv = document.getElementById('registeredEventsList');
    
    try {
        const response = await fetch(`${API_URL}/registrations/student/${email}`, {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) throw new Error('Failed to load registrations');
        
        const registrations = await response.json();
        
        if (registrations.length === 0) {
            listDiv.innerHTML = `
                <div class="text-center text-muted py-5">
                    <i class="fas fa-inbox fa-3x mb-3"></i>
                    <p>You haven't registered for any events yet.</p>
                    <a href="events.html" class="btn btn-primary">Browse Events</a>
                </div>
            `;
            return;
        }
        
        listDiv.innerHTML = registrations.map(reg => `
            <div class="card mb-3">
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-8">
                            <h5 class="card-title">${reg.eventTitle}</h5>
                            <p class="card-text">
                                <i class="fas fa-calendar"></i> ${formatDate(reg.eventDate)} | 
                                <i class="fas fa-map-marker-alt"></i> ${reg.venue}
                            </p>
                            <p class="card-text">
                                ${reg.amount > 0 ? `
                                    <span class="badge bg-${reg.paymentStatus === 'PAID' ? 'success' : 'warning'}">
                                        ${reg.paymentStatus === 'PAID' ? '✓ Paid' : '⏳ Payment Pending'} - ₹${reg.amount}
                                    </span>
                                ` : `
                                    <span class="badge bg-info">FREE Event</span>
                                `}
                                <span class="badge bg-secondary">${reg.status}</span>
                            </p>
                        </div>
                        <div class="col-md-4 text-end">
                            <button class="btn btn-info btn-sm mb-2 w-100" onclick="viewEventDetails(${reg.eventId})">
                                <i class="fas fa-eye"></i> View Details
                            </button>
                            
                            ${reg.paymentStatus === 'PENDING' && reg.amount > 0 ? `
                                <button class="btn btn-success btn-sm mb-2 w-100" onclick="showPaymentModal(${reg.registrationId}, ${reg.amount}, '${reg.eventTitle.replace(/'/g, "\\'")}')">
                                    <i class="fas fa-credit-card"></i> Pay ₹${reg.amount}
                                </button>
                            ` : ''}
                            
                            ${reg.paymentStatus === 'PAID' || reg.amount == 0 ? `
                                <button class="btn btn-primary btn-sm mb-2 w-100" onclick="downloadQR(${reg.eventId})">
                                    <i class="fas fa-qrcode"></i> Get QR Code
                                </button>
                            ` : ''}
                            
                            <button class="btn btn-danger btn-sm mb-2 w-100" onclick="cancelRegistration(${reg.registrationId})">
                                <i class="fas fa-times"></i> Cancel
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `).join('');
        
    } catch (error) {
        console.error('Error:', error);
        listDiv.innerHTML = `
            <div class="alert alert-danger">
                <i class="fas fa-exclamation-triangle"></i> Failed to load your events.
            </div>
        `;
    }
}

async function loadAttendedEvents() {
    const email = localStorage.getItem('email');
    const listDiv = document.getElementById('attendedEventsList');
    
    try {
        const response = await fetch(`${API_URL}/attendance/student/${email}`, {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) throw new Error('Failed to load attendance');
        
        const attendance = await response.json();
        
        if (attendance.length === 0) {
            listDiv.innerHTML = `
                <div class="text-center text-muted py-5">
                    <i class="fas fa-clipboard-check fa-3x mb-3"></i>
                    <p>No attendance records found.</p>
                </div>
            `;
            return;
        }
        
        listDiv.innerHTML = attendance.map(att => `
            <div class="card mb-3">
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-8">
                            <h5 class="card-title">${att.eventTitle || 'Event #' + att.eventId}</h5>
                            <p class="card-text">
                                <i class="fas fa-calendar"></i> Attended on: ${formatDateTime(att.checkInTime)}
                            </p>
                            <span class="badge bg-success">Attended</span>
                        </div>
                        <div class="col-md-4 text-end">
                            ${att.photoPath ? `
                                <img src="/${att.photoPath}" alt="Attendance Photo" 
                                     class="img-thumbnail mb-2" style="max-width: 100px;">
                            ` : ''}
                            <button class="btn btn-success btn-sm d-block w-100" onclick="downloadCertificate(${att.eventId})">
                                <i class="fas fa-certificate"></i> Get Certificate
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `).join('');
        
    } catch (error) {
        console.error('Error:', error);
        listDiv.innerHTML = `
            <div class="alert alert-danger">
                <i class="fas fa-exclamation-triangle"></i> Failed to load attendance records.
            </div>
        `;
    }
}

async function cancelRegistration(registrationId) {
    if (!confirm('Are you sure you want to cancel this registration?')) return;
    
    const email = localStorage.getItem('email');
    
    try {
        const response = await fetch(`${API_URL}/registrations/${registrationId}?studentEmail=${email}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        
        const data = await response.json();
        
        if (response.ok && data.success) {
            showAlert(data.message, 'success');
            loadMyEvents();
        } else {
            showAlert(data.message || 'Failed to cancel registration', 'danger');
        }
    } catch (error) {
        console.error('Error:', error);
        showAlert('Failed to cancel registration', 'danger');
    }
}

function viewEventDetails(eventId) {
    window.location.href = `events.html?id=${eventId}`;
}

async function downloadQR(eventId) {
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`${API_URL}/qrcode/download/${eventId}`, {
            headers: { 'Authorization': token ? `Bearer ${token}` : '' }
        });
        
        if (!response.ok) throw new Error('Failed to download QR');
        
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `event-${eventId}-qr.png`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
        
        showAlert('QR Code downloaded!', 'success');
    } catch (error) {
        console.error('Error:', error);
        showAlert('Failed to download QR code', 'danger');
    }
}

async function downloadCertificate(eventId) {
    const participantName = localStorage.getItem('fullName');
    
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(
            `${API_URL}/certificate/generate/${eventId}?participantName=${encodeURIComponent(participantName)}`,
            {
                headers: { 'Authorization': token ? `Bearer ${token}` : '' }
            }
        );
        
        if (!response.ok) throw new Error('Failed to generate certificate');
        
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `certificate-${participantName.replace(/\s+/g, '_')}-event${eventId}.pdf`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
        
        showAlert('Certificate downloaded!', 'success');
    } catch (error) {
        console.error('Error:', error);
        showAlert('Failed to download certificate', 'danger');
    }
}

// PAYMENT FUNCTIONS

function showPaymentModal(registrationId, amount, eventTitle) {
    const modalHTML = `
        <div class="modal fade" id="paymentModal" tabindex="-1">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header bg-success text-white">
                        <h5 class="modal-title">
                            <i class="fas fa-credit-card"></i> Make Payment
                        </h5>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <div class="alert alert-info">
                            <strong>Event:</strong> ${eventTitle}<br>
                            <strong>Amount:</strong> ₹${amount}
                        </div>
                        
                        <form id="paymentForm">
                            <input type="hidden" id="paymentRegistrationId" value="${registrationId}">
                            <input type="hidden" id="paymentAmount" value="${amount}">
                            
                            <div class="mb-3">
                                <label class="form-label">Payment Method *</label>
                                <select class="form-select" id="paymentMethod" required>
                                    <option value="">Select Payment Method</option>
                                    <option value="UPI">UPI</option>
                                    <option value="CARD">Credit/Debit Card</option>
                                    <option value="NET_BANKING">Net Banking</option>
                                    <option value="WALLET">Wallet</option>
                                </select>
                            </div>
                            
                            <div class="alert alert-warning">
                                <i class="fas fa-info-circle"></i> 
                                This is a demo payment system. Click "Pay Now" to simulate payment.
                            </div>
                        </form>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                        <button type="button" class="btn btn-success" onclick="processPayment()">
                            <i class="fas fa-check"></i> Pay ₹${amount}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    const existingModal = document.getElementById('paymentModal');
    if (existingModal) {
        existingModal.remove();
    }
    
    document.body.insertAdjacentHTML('beforeend', modalHTML);
    
    const modal = new bootstrap.Modal(document.getElementById('paymentModal'));
    modal.show();
}

async function processPayment() {
    const registrationId = document.getElementById('paymentRegistrationId').value;
    const amount = document.getElementById('paymentAmount').value;
    const paymentMethod = document.getElementById('paymentMethod').value;
    const email = localStorage.getItem('email');
    
    if (!paymentMethod) {
        showAlert('Please select a payment method', 'warning');
        return;
    }
    
    try {
        const response = await fetch(`${API_URL}/payments/process`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({
                registrationId: parseInt(registrationId),
                studentEmail: email,
                amount: parseFloat(amount),
                paymentMethod: paymentMethod
            })
        });
        
        const data = await response.json();
        
        if (response.ok && data.success) {
            showAlert('✅ Payment successful! Transaction ID: ' + data.transactionId, 'success');
            
            const modal = bootstrap.Modal.getInstance(document.getElementById('paymentModal'));
            modal.hide();
            
            setTimeout(() => {
                loadMyEvents();
            }, 1500);
        } else {
            showAlert('❌ ' + (data.message || 'Payment failed'), 'danger');
        }
    } catch (error) {
        console.error('Payment error:', error);
        showAlert('❌ Payment processing failed. Please try again.', 'danger');
    }
}

// UTILITY FUNCTIONS

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
}

function formatDateTime(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function showAlert(message, type) {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show position-fixed top-0 start-50 translate-middle-x mt-3`;
    alertDiv.style.zIndex = '9999';
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    document.body.appendChild(alertDiv);
    setTimeout(() => alertDiv.remove(), 3000);
}