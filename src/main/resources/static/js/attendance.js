 const API_URL = 'http://localhost:8080/api';
let allAttendance = [];

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
    const email = localStorage.getItem('email');
    
    // Show manual attendance ONLY for ADMIN
    if (userRole === 'ADMIN') {
        const manualSection = document.getElementById('manualAttendanceSection');
        if (manualSection) {
            manualSection.style.display = 'block';
        }
        loadEvents(); // Load events for manual attendance form
        loadAllAttendance(); // Load all attendance records
    } else if (userRole === 'STUDENT') {
        // Students see only their own attendance
        loadStudentAttendance(email);
        
        // Show student links
        const myEventsLink = document.getElementById('myEventsLink');
        const paymentsLink = document.getElementById('paymentsLink');
        if (myEventsLink) myEventsLink.style.display = 'block';
        if (paymentsLink) paymentsLink.style.display = 'block';
        
        // Hide reports
        const reportsLink = document.getElementById('reportsLink');
        if (reportsLink) reportsLink.style.display = 'none';
    } else {
        // HOD, EVENT_COORDINATOR can see all attendance
        loadEvents();
        loadAllAttendance();
    }
});

// Load events for dropdowns
async function loadEvents() {
    try {
        const response = await fetch(`${API_URL}/events`, {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) throw new Error('Failed to load events');
        
        const events = await response.json();
        const approvedEvents = events.filter(e => e.status === 'APPROVED');
        
        // Populate event select dropdowns
        const eventSelect = document.getElementById('eventSelect');
        const filterEventSelect = document.getElementById('filterEventSelect');
        
        if (eventSelect) {
            eventSelect.innerHTML = '<option value="">-- Select Event --</option>' +
                approvedEvents.map(event => 
                    `<option value="${event.id}">${event.title} (${formatDate(event.eventDate)})</option>`
                ).join('');
        }
        
        if (filterEventSelect) {
            filterEventSelect.innerHTML = '<option value="">-- All Events --</option>' +
                approvedEvents.map(event => 
                    `<option value="${event.id}">${event.title}</option>`
                ).join('');
        }
    } catch (error) {
        console.error('Error loading events:', error);
    }
}

// Load all attendance (ADMIN/HOD/EVENT_COORDINATOR)
async function loadAllAttendance() {
    const listDiv = document.getElementById('attendanceList');
    
    try {
        const response = await fetch(`${API_URL}/attendance`, {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) throw new Error('Failed to load attendance');
        
        allAttendance = await response.json();
        displayAttendance(allAttendance);
        
    } catch (error) {
        console.error('Error:', error);
        listDiv.innerHTML = `
            <div class="alert alert-danger">
                <i class="fas fa-exclamation-triangle"></i> Failed to load attendance records.
            </div>
        `;
    }
}

// Load student's own attendance
async function loadStudentAttendance(email) {
    const listDiv = document.getElementById('attendanceList');
    
    try {
        const response = await fetch(`${API_URL}/attendance/student/${email}`, {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) throw new Error('Failed to load attendance');
        
        allAttendance = await response.json();
        displayAttendance(allAttendance);
        
    } catch (error) {
        console.error('Error:', error);
        listDiv.innerHTML = `
            <div class="alert alert-danger">
                <i class="fas fa-exclamation-triangle"></i> Failed to load your attendance records.
            </div>
        `;
    }
}

// Display attendance records
function displayAttendance(records) {
    const listDiv = document.getElementById('attendanceList');
    
    if (records.length === 0) {
        listDiv.innerHTML = `
            <div class="text-center text-muted py-5">
                <i class="fas fa-clipboard-check fa-3x mb-3"></i>
                <p>No attendance records found.</p>
            </div>
        `;
        return;
    }
    
    const userRole = localStorage.getItem('role');
    
    listDiv.innerHTML = `
        <div class="table-responsive">
            <table class="table table-hover">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Event ID</th>
                        <th>Student Name</th>
                        <th>Email</th>
                        <th>Roll Number</th>
                        <th>Check-in Time</th>
                        <th>Method</th>
                        ${userRole === 'ADMIN' ? '<th>Actions</th>' : ''}
                    </tr>
                </thead>
                <tbody>
                    ${records.map(att => `
                        <tr>
                            <td>${att.id}</td>
                            <td>${att.eventId}</td>
                            <td>${att.studentName}</td>
                            <td>${att.studentEmail}</td>
                            <td>${att.rollNumber || 'N/A'}</td>
                            <td>${formatDateTime(att.checkInTime)}</td>
                            <td>
                                <span class="badge bg-${att.checkInMethod === 'QR' ? 'primary' : 'secondary'}">
                                    ${att.checkInMethod}
                                </span>
                            </td>
                            ${userRole === 'ADMIN' ? `
                                <td>
                                    <button class="btn btn-danger btn-sm" onclick="deleteAttendance(${att.id})">
                                        <i class="fas fa-trash"></i>
                                    </button>
                                </td>
                            ` : ''}
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        </div>
    `;
}

// Filter attendance
function filterAttendance() {
    const eventId = document.getElementById('filterEventSelect').value;
    const search = document.getElementById('searchAttendance').value.toLowerCase();
    
    let filtered = allAttendance;
    
    if (eventId) {
        filtered = filtered.filter(att => att.eventId == eventId);
    }
    
    if (search) {
        filtered = filtered.filter(att =>
            att.studentName.toLowerCase().includes(search) ||
            att.studentEmail.toLowerCase().includes(search) ||
            (att.rollNumber && att.rollNumber.toLowerCase().includes(search))
        );
    }
    
    displayAttendance(filtered);
}

// Mark manual attendance (ADMIN ONLY)
async function markManualAttendance() {
    const userRole = localStorage.getItem('role');
    
    if (userRole !== 'ADMIN') {
        showAlert('Only ADMIN can manually mark attendance!', 'danger');
        return;
    }
    
    const eventId = document.getElementById('eventSelect').value;
    const studentName = document.getElementById('studentName').value;
    const studentEmail = document.getElementById('studentEmail').value;
    const rollNumber = document.getElementById('rollNumber').value;
    const department = document.getElementById('department').value;
    
    if (!eventId || !studentName || !studentEmail) {
        showAlert('Please fill all required fields', 'warning');
        return;
    }
    
    try {
        const response = await fetch(`${API_URL}/attendance/mark`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({
                eventId: parseInt(eventId),
                studentName: studentName,
                studentEmail: studentEmail,
                rollNumber: rollNumber,
                department: department,
                checkInMethod: 'MANUAL'
            })
        });
        
        const data = await response.json();
        
        if (response.ok && data.success) {
            showAlert('✅ ' + data.message, 'success');
            document.getElementById('manualAttendanceForm').reset();
            loadAllAttendance();
        } else {
            showAlert('❌ ' + (data.message || 'Failed to mark attendance'), 'danger');
        }
    } catch (error) {
        console.error('Error:', error);
        showAlert('❌ Failed to mark attendance', 'danger');
    }
}

// Delete attendance (ADMIN ONLY)
async function deleteAttendance(id) {
    const userRole = localStorage.getItem('role');
    
    if (userRole !== 'ADMIN') {
        showAlert('Only ADMIN can delete attendance records!', 'danger');
        return;
    }
    
    if (!confirm('Are you sure you want to delete this attendance record?')) return;
    
    try {
        const response = await fetch(`${API_URL}/attendance/${id}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        
        if (response.ok) {
            showAlert('Attendance record deleted', 'success');
            loadAllAttendance();
        } else {
            showAlert('Failed to delete attendance', 'danger');
        }
    } catch (error) {
        console.error('Error:', error);
        showAlert('Error deleting attendance', 'danger');
    }
}

// Utility functions
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