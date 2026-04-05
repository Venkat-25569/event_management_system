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
    
    const userRole = localStorage.getItem('role');
    if (userRole === 'STUDENT') {
        const myEventsLink = document.getElementById('myEventsLink');
        const paymentsLink = document.getElementById('paymentsLink');
        if (myEventsLink) myEventsLink.style.display = 'block';
        if (paymentsLink) paymentsLink.style.display = 'block';
    }
    
    loadPaymentHistory();
});

async function loadPaymentHistory() {
    const email = localStorage.getItem('email');
    const listDiv = document.getElementById('paymentHistoryList');
    
    try {
        const response = await fetch(`${API_URL}/payments/student/${email}`, {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) throw new Error('Failed to load payments');
        
        const payments = await response.json();
        
        if (payments.length === 0) {
            listDiv.innerHTML = `
                <div class="text-center text-muted py-5">
                    <i class="fas fa-file-invoice-dollar fa-3x mb-3"></i>
                    <p>No payment history found.</p>
                    <a href="events.html" class="btn btn-primary">Browse Events</a>
                </div>
            `;
            return;
        }
        
        calculatePaymentSummary(payments);
        
        listDiv.innerHTML = `
            <div class="table-responsive">
                <table class="table table-hover">
                    <thead>
                        <tr>
                            <th>Transaction ID</th>
                            <th>Event ID</th>
                            <th>Amount</th>
                            <th>Payment Method</th>
                            <th>Date</th>
                            <th>Status</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${payments.map(payment => `
                            <tr>
                                <td><code>${payment.transactionId}</code></td>
                                <td>#${payment.eventId}</td>
                                <td><strong class="text-success">₹${payment.amount}</strong></td>
                                <td>${payment.paymentMethod || 'N/A'}</td>
                                <td>${formatDateTime(payment.paymentDate)}</td>
                                <td>
                                    <span class="badge bg-success">
                                        <i class="fas fa-check"></i> ${payment.status}
                                    </span>
                                </td>
                                <td>
                                    <button class="btn btn-sm btn-info" onclick='downloadReceipt(${JSON.stringify(payment)})'>
                                        <i class="fas fa-download"></i> Receipt
                                    </button>
                                </td>
                            </tr>
                        `).join('')}
                    </tbody>
                </table>
            </div>
        `;
        
    } catch (error) {
        console.error('Error:', error);
        listDiv.innerHTML = `
            <div class="alert alert-danger">
                <i class="fas fa-exclamation-triangle"></i> Failed to load payment history.
            </div>
        `;
    }
}

function calculatePaymentSummary(payments) {
    let totalPaid = 0;
    let thisMonthPaid = 0;
    const currentMonth = new Date().getMonth();
    const currentYear = new Date().getFullYear();
    
    payments.forEach(payment => {
        if (payment.status === 'SUCCESS') {
            totalPaid += parseFloat(payment.amount);
            
            const paymentDate = new Date(payment.paymentDate);
            if (paymentDate.getMonth() === currentMonth && 
                paymentDate.getFullYear() === currentYear) {
                thisMonthPaid += parseFloat(payment.amount);
            }
        }
    });
    
    document.getElementById('totalPaid').textContent = `₹${totalPaid.toFixed(2)}`;
    document.getElementById('thisMonthPaid').textContent = `₹${thisMonthPaid.toFixed(2)}`;
    document.getElementById('totalPending').textContent = '₹0';
}

function downloadReceipt(payment) {
    try {
        const receiptHTML = `
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Payment Receipt</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        max-width: 800px;
                        margin: 50px auto;
                        padding: 20px;
                        border: 2px solid #333;
                    }
                    .header {
                        text-align: center;
                        border-bottom: 2px solid #333;
                        padding-bottom: 20px;
                        margin-bottom: 20px;
                    }
                    .header h1 {
                        margin: 0;
                        color: #2c3e50;
                    }
                    .receipt-info {
                        margin: 20px 0;
                    }
                    .receipt-info table {
                        width: 100%;
                        border-collapse: collapse;
                    }
                    .receipt-info td {
                        padding: 10px;
                        border-bottom: 1px solid #ddd;
                    }
                    .receipt-info td:first-child {
                        font-weight: bold;
                        width: 200px;
                    }
                    .amount {
                        font-size: 24px;
                        color: #27ae60;
                        font-weight: bold;
                    }
                    .footer {
                        text-align: center;
                        margin-top: 50px;
                        padding-top: 20px;
                        border-top: 2px solid #333;
                        color: #7f8c8d;
                    }
                    .status {
                        display: inline-block;
                        padding: 5px 15px;
                        background-color: #27ae60;
                        color: white;
                        border-radius: 5px;
                    }
                </style>
            </head>
            <body>
                <div class="header">
                    <h1>🎓 Event Management System</h1>
                    <h2>Payment Receipt</h2>
                </div>
                
                <div class="receipt-info">
                    <table>
                        <tr>
                            <td>Receipt Number:</td>
                            <td>#${payment.id.toString().padStart(6, '0')}</td>
                        </tr>
                        <tr>
                            <td>Transaction ID:</td>
                            <td>${payment.transactionId}</td>
                        </tr>
                        <tr>
                            <td>Student Email:</td>
                            <td>${payment.studentEmail}</td>
                        </tr>
                        <tr>
                            <td>Event ID:</td>
                            <td>#${payment.eventId}</td>
                        </tr>
                        <tr>
                            <td>Payment Date:</td>
                            <td>${formatDateTime(payment.paymentDate)}</td>
                        </tr>
                        <tr>
                            <td>Payment Method:</td>
                            <td>${payment.paymentMethod || 'N/A'}</td>
                        </tr>
                        <tr>
                            <td>Amount Paid:</td>
                            <td class="amount">₹${payment.amount}</td>
                        </tr>
                        <tr>
                            <td>Status:</td>
                            <td><span class="status">${payment.status}</span></td>
                        </tr>
                    </table>
                </div>
                
                <div class="footer">
                    <p><strong>Thank you for your payment!</strong></p>
                    <p>This is a computer-generated receipt and does not require a signature.</p>
                    <p>For any queries, please contact support@eventmanagement.com</p>
                </div>
            </body>
            </html>
        `;
        
        const blob = new Blob([receiptHTML], { type: 'text/html' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `receipt-${payment.transactionId}.html`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
        
        showAlert('✅ Receipt downloaded successfully!', 'success');
        
    } catch (error) {
        console.error('Error:', error);
        showAlert('❌ Failed to download receipt', 'danger');
    }
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