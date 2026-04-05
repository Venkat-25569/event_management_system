  const API_URL = 'http://localhost:8080/api';
let currentEventId = null;
let charts = {};

// ========================================
// AUTH & INITIALIZATION
// ========================================

function getAuthHeaders() {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
    };
}

function checkAuth() {
    const token = localStorage.getItem('token');
    const role = localStorage.getItem('role');
    if (!token || (role !== 'HOD' && role !== 'ADMIN')) {
        alert('⚠️ Access Denied! HOD access required.');
        window.location.href = '../login.html';
        return false;
    }
    return true;
}

function displayUserInfo() {
    const fullName = localStorage.getItem('fullName');
    const role = localStorage.getItem('role');
    const userNameElement = document.getElementById('userName');
    if (userNameElement && fullName) {
        userNameElement.textContent = `${fullName} (${role})`;
    }
}

function logout() {
    if (confirm('Are you sure you want to logout?')) {
        localStorage.clear();
        window.location.href = '../login.html';
    }
}

// ========================================
// PAGE LOAD
// ========================================

document.addEventListener('DOMContentLoaded', function() {
    if (!checkAuth()) return;
    
    displayUserInfo();
    loadDashboardStats();
    loadPendingEvents();
    loadRecentActivities();
    
    // Refresh data every 30 seconds
    setInterval(() => {
        loadDashboardStats();
        loadPendingEvents();
    }, 30000);
});

// ========================================
// LOAD DASHBOARD STATISTICS
// ========================================

async function loadDashboardStats() {
    try {
        const response = await fetch(`${API_URL}/dashboard/hod/stats`, {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) throw new Error('Failed to load stats');
        
        const stats = await response.json();
        
        // Update stat cards
        document.getElementById('pendingCount').textContent = stats.pendingApprovals || 0;
        document.getElementById('urgentCount').textContent = `${stats.urgentApprovals || 0} urgent`;
        document.getElementById('approvedCount').textContent = stats.approvedThisMonth || 0;
        document.getElementById('upcomingCount').textContent = stats.upcomingEvents || 0;
        document.getElementById('avgAttendance').textContent = Math.round(stats.averageAttendance || 0);
        
        // Update badges
        const pendingBadge = document.getElementById('pendingBadge');
        if (pendingBadge) {
            pendingBadge.textContent = stats.pendingApprovals || 0;
            if (stats.urgentApprovals > 0) {
                pendingBadge.classList.add('urgent-badge');
            }
        }
        
        // Load charts if available
        if (stats.eventsByClub) {
            loadDepartmentChart(stats.eventsByClub);
        }
        
    } catch (error) {
        console.error('Error loading stats:', error);
        showAlert('Failed to load dashboard statistics', 'danger');
    }
}

// ========================================
// LOAD PENDING EVENTS
// ========================================

async function loadPendingEvents() {
    const container = document.getElementById('pendingEventsList');
    const countBadge = document.getElementById('pendingListCount');
    
    try {
        const response = await fetch(`${API_URL}/dashboard/pending-events`, {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) throw new Error('Failed to load pending events');
        
        const events = await response.json();
        
        if (countBadge) countBadge.textContent = events.length;
        
        if (events.length === 0) {
            container.innerHTML = `
                <div class="text-center text-muted py-5">
                    <i class="fas fa-check-circle fa-3x mb-3"></i>
                    <p>No pending approvals! All events are processed.</p>
                </div>
            `;
            return;
        }
        
        container.innerHTML = events.map(event => `
            <div class="card mb-3 ${event.urgent ? 'urgent-event-card' : 'pending-event-card'}">
                <div class="card-body">
                    <div class="row align-items-center">
                        <div class="col-md-8">
                            <h5 class="card-title mb-2">
                                <i class="fas fa-calendar-alt text-primary"></i> ${event.title}
                                ${event.urgent ? '<span class="badge bg-danger ms-2">URGENT</span>' : ''}
                            </h5>
                            <div class="row">
                                <div class="col-md-6">
                                    <p class="mb-1">
                                        <i class="fas fa-calendar"></i> 
                                        <strong>Date:</strong> ${formatDate(event.eventDate)}
                                        ${event.daysUntilEvent !== undefined ? 
                                            `<span class="badge bg-info ms-2">${event.daysUntilEvent} days</span>` : ''}
                                    </p>
                                    <p class="mb-1">
                                        <i class="fas fa-map-marker-alt"></i> 
                                        <strong>Venue:</strong> ${event.venue || 'N/A'}
                                    </p>
                                    <p class="mb-1">
                                        <i class="fas fa-users"></i> 
                                        <strong>Club:</strong> ${event.clubName || 'N/A'}
                                    </p>
                                </div>
                                <div class="col-md-6">
                                    <p class="mb-1">
                                        <i class="fas fa-user"></i> 
                                        <strong>Organizer:</strong> ${event.organizerName || 'N/A'}
                                    </p>
                                    <p class="mb-1">
                                        <i class="fas fa-user-friends"></i> 
                                        <strong>Expected:</strong> ${event.participantCount || 0} participants
                                    </p>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-4 text-end">
                            <div class="btn-group-vertical w-100">
                                <button class="btn btn-info btn-sm mb-2" onclick="viewEventDetails(${event.id})">
                                    <i class="fas fa-eye"></i> View Details
                                </button>
                                <button class="btn btn-success btn-sm mb-2" onclick="quickApprove(${event.id})">
                                    <i class="fas fa-check"></i> Approve
                                </button>
                                <button class="btn btn-danger btn-sm" onclick="quickReject(${event.id})">
                                    <i class="fas fa-times"></i> Reject
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `).join('');
        
    } catch (error) {
        console.error('Error loading pending events:', error);
        container.innerHTML = `
            <div class="alert alert-danger">
                <i class="fas fa-exclamation-triangle"></i> Failed to load pending events.
            </div>
        `;
    }
}

// ========================================
// VIEW EVENT DETAILS
// ========================================

async function viewEventDetails(eventId) {
    currentEventId = eventId;
    
    try {
        const response = await fetch(`${API_URL}/events/${eventId}`, {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) throw new Error('Failed to load event');
        
        const event = await response.json();
        
        const modalBody = document.getElementById('modalBody');
        modalBody.innerHTML = `
            <div class="row">
                <div class="col-md-12">
                    <h4 class="text-primary">${event.title}</h4>
                    <hr>
                    
                    <div class="row mb-3">
                        <div class="col-md-6">
                            <p><strong><i class="fas fa-calendar"></i> Date:</strong> ${formatDate(event.eventDate)}</p>
                            <p><strong><i class="fas fa-map-marker-alt"></i> Venue:</strong> ${event.venue || 'N/A'}</p>
                            <p><strong><i class="fas fa-users"></i> Club:</strong> ${event.clubName || 'N/A'}</p>
                        </div>
                        <div class="col-md-6">
                            <p><strong><i class="fas fa-user"></i> Organizer:</strong> ${event.organizerName || 'N/A'}</p>
                            <p><strong><i class="fas fa-user-friends"></i> Participants:</strong> ${event.participantCount || 0}</p>
                            <p><strong><i class="fas fa-info-circle"></i> Status:</strong> 
                                <span class="badge bg-warning">PENDING</span>
                            </p>
                        </div>
                    </div>
                    
                    <h6><i class="fas fa-align-left"></i> Description:</h6>
                    <p class="text-muted">${event.description || 'No description provided'}</p>
                    
                    <hr>
                    
                    <div class="alert alert-info">
                        <strong><i class="fas fa-info-circle"></i> HOD Action Required:</strong>
                        <p class="mb-0">Please review this event and approve or reject it. Your decision will affect the event planning and student participation.</p>
                    </div>
                </div>
            </div>
        `;
        
        const modal = new bootstrap.Modal(document.getElementById('approvalModal'));
        modal.show();
        
    } catch (error) {
        console.error('Error:', error);
        showAlert('Failed to load event details', 'danger');
    }
}

// ========================================
// APPROVE EVENT
// ========================================

async function approveEvent() {
    if (!currentEventId) return;
    
    if (!confirm('Are you sure you want to approve this event?')) return;
    
    try {
        const response = await fetch(`${API_URL}/events/${currentEventId}/approve`, {
            method: 'PUT',
            headers: getAuthHeaders()
        });
        
        if (response.ok) {
            showAlert('✅ Event approved successfully!', 'success');
            
            // Close modal
            const modal = bootstrap.Modal.getInstance(document.getElementById('approvalModal'));
            if (modal) modal.hide();
            
            // Reload data
            loadDashboardStats();
            loadPendingEvents();
            loadRecentActivities();
            
        } else {
            showAlert('❌ Failed to approve event', 'danger');
        }
    } catch (error) {
        console.error('Error:', error);
        showAlert('❌ Error approving event', 'danger');
    }
}

// ========================================
// REJECT EVENT
// ========================================

async function rejectEvent() {
    if (!currentEventId) return;
    
    const reason = prompt('Please provide a reason for rejection (optional):');
    if (reason === null) return; // User cancelled
    
    if (!confirm('Are you sure you want to reject this event?')) return;
    
    try {
        const response = await fetch(`${API_URL}/events/${currentEventId}/reject`, {
            method: 'PUT',
            headers: getAuthHeaders()
        });
        
        if (response.ok) {
            showAlert('Event rejected', 'warning');
            
            // Close modal
            const modal = bootstrap.Modal.getInstance(document.getElementById('approvalModal'));
            if (modal) modal.hide();
            
            // Reload data
            loadDashboardStats();
            loadPendingEvents();
            loadRecentActivities();
            
        } else {
            showAlert('Failed to reject event', 'danger');
        }
    } catch (error) {
        console.error('Error:', error);
        showAlert('Error rejecting event', 'danger');
    }
}

// ========================================
// QUICK APPROVE/REJECT
// ========================================

async function quickApprove(eventId) {
    if (!confirm('Are you sure you want to approve this event?')) return;
    
    try {
        const response = await fetch(`${API_URL}/events/${eventId}/approve`, {
            method: 'PUT',
            headers: getAuthHeaders()
        });
        
        if (response.ok) {
            showAlert('✅ Event approved successfully!', 'success');
            loadDashboardStats();
            loadPendingEvents();
        } else {
            showAlert('❌ Failed to approve event', 'danger');
        }
    } catch (error) {
        console.error('Error:', error);
        showAlert('❌ Error approving event', 'danger');
    }
}

async function quickReject(eventId) {
    if (!confirm('Are you sure you want to reject this event?')) return;
    
    try {
        const response = await fetch(`${API_URL}/events/${eventId}/reject`, {
            method: 'PUT',
            headers: getAuthHeaders()
        });
        
        if (response.ok) {
            showAlert('Event rejected', 'warning');
            loadDashboardStats();
            loadPendingEvents();
        } else {
            showAlert('Failed to reject event', 'danger');
        }
    } catch (error) {
        console.error('Error:', error);
        showAlert('Error rejecting event', 'danger');
    }
}

// ========================================
// LOAD RECENT ACTIVITIES
// ========================================

async function loadRecentActivities() {
    const container = document.getElementById('recentActivities');
    
    try {
        const response = await fetch(`${API_URL}/dashboard/recent-activities?limit=10`, {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) throw new Error('Failed to load activities');
        
        const activities = await response.json();
        
        if (activities.length === 0) {
            container.innerHTML = '<p class="text-muted">No recent activities</p>';
            return;
        }
        
        container.innerHTML = `
            <ul class="list-group">
                ${activities.map(activity => `
                    <li class="list-group-item">
                        <i class="fas fa-${getActivityIcon(activity.action)} me-2 text-${getActivityColor(activity.action)}"></i>
                        <strong>${activity.title}</strong> - ${activity.action}
                        <small class="text-muted float-end">${formatDateTime(activity.timestamp)}</small>
                    </li>
                `).join('')}
            </ul>
        `;
        
    } catch (error) {
        console.error('Error:', error);
        container.innerHTML = '<p class="text-danger">Failed to load activities</p>';
    }
}

function getActivityIcon(action) {
    const icons = {
        'APPROVED': 'check-circle',
        'REJECTED': 'times-circle',
        'PENDING': 'clock',
        'event': 'calendar'
    };
    return icons[action] || 'info-circle';
}

function getActivityColor(action) {
    const colors = {
        'APPROVED': 'success',
        'REJECTED': 'danger',
        'PENDING': 'warning',
        'event': 'info'
    };
    return colors[action] || 'secondary';
}

// ========================================
// CHARTS
// ========================================

function loadDepartmentChart(eventsByClub) {
    const ctx = document.getElementById('departmentChart');
    if (!ctx) return;
    
    // Destroy existing chart
    if (charts.department) {
        charts.department.destroy();
    }
    
    const labels = Object.keys(eventsByClub);
    const data = Object.values(eventsByClub);
    
    charts.department = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: [
                    '#3498db',
                    '#27ae60',
                    '#f39c12',
                    '#e74c3c',
                    '#9b59b6',
                    '#1abc9c'
                ]
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: 'bottom'
                },
                title: {
                    display: true,
                    text: 'Events by Club'
                }
            }
        }
    });
}

function loadTrendChart() {
    const ctx = document.getElementById('trendChart');
    if (!ctx) return;
    
    // Destroy existing chart
    if (charts.trend) {
        charts.trend.destroy();
    }
    
    // Mock data - replace with actual API call
    charts.trend = new Chart(ctx, {
        type: 'line',
        data: {
            labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
            datasets: [{
                label: 'Events',
                data: [12, 19, 15, 25, 22, 30],
                borderColor: '#3498db',
                backgroundColor: 'rgba(52, 152, 219, 0.1)',
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    display: false
                },
                title: {
                    display: true,
                    text: 'Monthly Event Trend'
                }
            },
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}

// ========================================
// UTILITY FUNCTIONS
// ========================================

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
    alertDiv.style.minWidth = '300px';
    alertDiv.innerHTML = `
        <i class="fas fa-${type === 'success' ? 'check-circle' : type === 'danger' ? 'exclamation-circle' : 'info-circle'}"></i>
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    document.body.appendChild(alertDiv);
    setTimeout(() => alertDiv.remove(), 3000);
}