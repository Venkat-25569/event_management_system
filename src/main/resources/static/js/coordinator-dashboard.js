  const API_URL = 'http://localhost:8080/api';
let currentEventId = null;

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
    if (!token || (role !== 'EVENT_COORDINATOR' && role !== 'ADMIN')) {
        alert('⚠️ Access Denied! Event Coordinator access required.');
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
    loadRegistrationStatus();
    loadEventTimeline();
    initializeTasks();
    
    // Auto-refresh every 30 seconds
    setInterval(() => {
        loadDashboardStats();
        loadRegistrationStatus();
    }, 30000);
});

// ========================================
// LOAD DASHBOARD STATISTICS
// ========================================

async function loadDashboardStats() {
    try {
        console.log('Loading coordinator stats...');
        
        const response = await fetch(`${API_URL}/dashboard/coordinator/stats`, {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) {
            console.warn('Dashboard stats API not available, using events API');
            await loadStatsFromEvents();
            return;
        }
        
        const stats = await response.json();
        console.log('Stats loaded:', stats);
        
        // Update stat cards
        document.getElementById('pendingCount').textContent = stats.pendingApprovals || 0;
        document.getElementById('approvedCount').textContent = stats.approvedEvents || 0;
        document.getElementById('upcomingCount').textContent = stats.upcomingEvents || 0;
        document.getElementById('registrationsCount').textContent = stats.activeRegistrations || 0;
        
        // Update pending badge
        const pendingBadge = document.getElementById('pendingBadge');
        if (pendingBadge) {
            pendingBadge.textContent = stats.pendingApprovals || 0;
        }
        
    } catch (error) {
        console.error('Error loading stats:', error);
        await loadStatsFromEvents();
    }
}

async function loadStatsFromEvents() {
    try {
        const response = await fetch(`${API_URL}/events`, {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) throw new Error('Failed to load events');
        
        const events = await response.json();
        const today = new Date();
        const weekFromNow = new Date(today.getTime() + 7 * 24 * 60 * 60 * 1000);
        
        const pending = events.filter(e => e.status === 'PENDING').length;
        const approved = events.filter(e => e.status === 'APPROVED').length;
        const upcoming = events.filter(e => {
            const eventDate = new Date(e.eventDate);
            return e.status === 'APPROVED' && eventDate >= today && eventDate <= weekFromNow;
        }).length;
        
        document.getElementById('pendingCount').textContent = pending;
        document.getElementById('approvedCount').textContent = approved;
        document.getElementById('upcomingCount').textContent = upcoming;
        document.getElementById('registrationsCount').textContent = events.length;
        
        const pendingBadge = document.getElementById('pendingBadge');
        if (pendingBadge) {
            pendingBadge.textContent = pending;
        }
        
    } catch (error) {
        console.error('Error loading events for stats:', error);
        showAlert('Failed to load dashboard statistics', 'danger');
    }
}

// ========================================
// LOAD PENDING EVENTS
// ========================================

async function loadPendingEvents() {
    const container = document.getElementById('pendingEventsList');
    
    try {
        console.log('Loading pending events...');
        
        // Try dashboard API first
        let response = await fetch(`${API_URL}/dashboard/pending-events`, {
            headers: getAuthHeaders()
        });
        
        let events;
        if (!response.ok) {
            console.warn('Pending events API not available, using events API');
            response = await fetch(`${API_URL}/events`, {
                headers: getAuthHeaders()
            });
            
            if (!response.ok) throw new Error('Failed to load events');
            
            const allEvents = await response.json();
            events = allEvents.filter(e => e.status === 'PENDING');
        } else {
            events = await response.json();
        }
        
        console.log('Pending events loaded:', events.length);
        
        if (events.length === 0) {
            container.innerHTML = `
                <div class="text-center text-muted py-5">
                    <i class="fas fa-check-circle fa-3x mb-3"></i>
                    <p>No pending approvals! All events are processed.</p>
                </div>
            `;
            return;
        }
        
        container.innerHTML = events.map(event => {
            const eventDate = event.eventDate ? new Date(event.eventDate) : null;
            const today = new Date();
            const daysUntilEvent = eventDate ? Math.ceil((eventDate - today) / (1000 * 60 * 60 * 24)) : null;
            const isUrgent = daysUntilEvent !== null && daysUntilEvent <= 3;
            
            return `
                <div class="card mb-3 border-start border-${isUrgent ? 'danger' : 'warning'} border-4">
                    <div class="card-body">
                        <div class="row align-items-center">
                            <div class="col-md-9">
                                <h5 class="card-title">
                                    <i class="fas fa-calendar-alt text-primary"></i> ${event.title}
                                    ${isUrgent ? '<span class="badge bg-danger ms-2">URGENT</span>' : ''}
                                </h5>
                                <div class="row">
                                    <div class="col-md-6">
                                        <p class="mb-1 text-muted">
                                            <i class="fas fa-calendar"></i> ${formatDate(event.eventDate)}
                                            ${daysUntilEvent !== null ? 
                                                `<span class="badge bg-info ms-2">${daysUntilEvent} days away</span>` : ''}
                                        </p>
                                        <p class="mb-1 text-muted">
                                            <i class="fas fa-map-marker-alt"></i> ${event.venue || 'N/A'}
                                        </p>
                                    </div>
                                    <div class="col-md-6">
                                        <p class="mb-1 text-muted">
                                            <i class="fas fa-users"></i> ${event.clubName || 'N/A'}
                                        </p>
                                        <p class="mb-1 text-muted">
                                            <i class="fas fa-user-friends"></i> ${event.participantCount || 0} expected
                                        </p>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-3 text-end">
                                <button class="btn btn-info btn-sm mb-2 w-100" onclick="viewEventDetails(${event.id})">
                                    <i class="fas fa-eye"></i> View
                                </button>
                                <button class="btn btn-success btn-sm mb-2 w-100" onclick="quickApprove(${event.id})">
                                    <i class="fas fa-check"></i> Approve
                                </button>
                                <button class="btn btn-danger btn-sm w-100" onclick="quickReject(${event.id})">
                                    <i class="fas fa-times"></i> Reject
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        }).join('');
        
    } catch (error) {
        console.error('Error loading pending events:', error);
        container.innerHTML = `
            <div class="alert alert-danger">
                <i class="fas fa-exclamation-triangle"></i> Failed to load pending events.
                <br><small>${error.message}</small>
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
                    <h4 class="text-primary mb-3">${event.title}</h4>
                    
                    <div class="row mb-3">
                        <div class="col-md-6">
                            <p><strong><i class="fas fa-calendar text-primary"></i> Date:</strong> ${formatDate(event.eventDate)}</p>
                            <p><strong><i class="fas fa-map-marker-alt text-danger"></i> Venue:</strong> ${event.venue || 'N/A'}</p>
                            <p><strong><i class="fas fa-users text-success"></i> Club:</strong> ${event.clubName || 'N/A'}</p>
                        </div>
                        <div class="col-md-6">
                            <p><strong><i class="fas fa-user text-info"></i> Organizer:</strong> ${event.organizerName || 'N/A'}</p>
                            <p><strong><i class="fas fa-user-friends text-warning"></i> Expected:</strong> ${event.participantCount || 0} participants</p>
                            <p><strong><i class="fas fa-info-circle text-secondary"></i> Status:</strong> 
                                <span class="badge bg-warning">PENDING</span>
                            </p>
                        </div>
                    </div>
                    
                    <hr>
                    
                    <h6 class="text-secondary"><i class="fas fa-align-left"></i> Description:</h6>
                    <div class="bg-light p-3 rounded">
                        <p class="mb-0">${event.description || 'No description provided'}</p>
                    </div>
                    
                    <hr>
                    
                    <div class="alert alert-info mb-0">
                        <strong><i class="fas fa-info-circle"></i> Coordinator Action:</strong>
                        <p class="mb-0">Review and approve/reject this event. Consider venue availability, timing, and resource requirements.</p>
                    </div>
                </div>
            </div>
        `;
        
        const modal = new bootstrap.Modal(document.getElementById('eventModal'));
        modal.show();
        
    } catch (error) {
        console.error('Error:', error);
        showAlert('Failed to load event details', 'danger');
    }
}

// ========================================
// APPROVE/REJECT EVENTS
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
            
            const modal = bootstrap.Modal.getInstance(document.getElementById('eventModal'));
            if (modal) modal.hide();
            
            loadDashboardStats();
            loadPendingEvents();
            loadEventTimeline();
        } else {
            showAlert('❌ Failed to approve event', 'danger');
        }
    } catch (error) {
        console.error('Error:', error);
        showAlert('❌ Error approving event', 'danger');
    }
}

async function rejectEvent() {
    if (!currentEventId) return;
    
    const reason = prompt('Please provide a reason for rejection (optional):');
    if (reason === null) return;
    
    if (!confirm('Are you sure you want to reject this event?')) return;
    
    try {
        const response = await fetch(`${API_URL}/events/${currentEventId}/reject`, {
            method: 'PUT',
            headers: getAuthHeaders()
        });
        
        if (response.ok) {
            showAlert('Event rejected', 'warning');
            
            const modal = bootstrap.Modal.getInstance(document.getElementById('eventModal'));
            if (modal) modal.hide();
            
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

async function quickApprove(eventId) {
    if (!confirm('Are you sure you want to approve this event?')) return;
    
    try {
        const response = await fetch(`${API_URL}/events/${eventId}/approve`, {
            method: 'PUT',
            headers: getAuthHeaders()
        });
        
        if (response.ok) {
            showAlert('✅ Event approved!', 'success');
            loadDashboardStats();
            loadPendingEvents();
        } else {
            showAlert('❌ Failed to approve', 'danger');
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
            showAlert('Failed to reject', 'danger');
        }
    } catch (error) {
        console.error('Error:', error);
        showAlert('Error rejecting event', 'danger');
    }
}

// ========================================
// LOAD REGISTRATION STATUS
// ========================================

async function loadRegistrationStatus() {
    const container = document.getElementById('registrationStatus');
    
    try {
        const eventsResponse = await fetch(`${API_URL}/events`, {
            headers: getAuthHeaders()
        });
        
        if (!eventsResponse.ok) throw new Error('Failed to load events');
        
        const allEvents = await eventsResponse.json();
        const approvedEvents = allEvents.filter(e => e.status === 'APPROVED').slice(0, 5);
        
        if (approvedEvents.length === 0) {
            container.innerHTML = '<p class="text-muted">No active registrations</p>';
            return;
        }
        
        // Mock registration data - replace with actual API call
        container.innerHTML = approvedEvents.map(event => {
            const registered = Math.floor(Math.random() * (event.participantCount || 100));
            const total = event.participantCount || 100;
            const percentage = Math.round((registered / total) * 100);
            
            return `
                <div class="mb-3">
                    <div class="d-flex justify-content-between mb-1">
                        <span class="text-truncate" style="max-width: 60%;">${event.title}</span>
                        <span class="text-muted">${registered}/${total}</span>
                    </div>
                    <div class="progress" style="height: 25px;">
                        <div class="progress-bar ${percentage > 80 ? 'bg-success' : percentage > 50 ? 'bg-info' : 'bg-warning'}" 
                             role="progressbar" 
                             style="width: ${percentage}%">
                            ${percentage}%
                        </div>
                    </div>
                </div>
            `;
        }).join('');
        
    } catch (error) {
        console.error('Error:', error);
        container.innerHTML = '<p class="text-danger">Failed to load registration data</p>';
    }
}

// ========================================
// LOAD EVENT TIMELINE
// ========================================

async function loadEventTimeline() {
    const container = document.getElementById('eventTimeline');
    
    try {
        const response = await fetch(`${API_URL}/events`, {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) throw new Error('Failed to load events');
        
        const allEvents = await response.json();
        const today = new Date();
        const upcoming = allEvents
            .filter(e => e.status === 'APPROVED' && new Date(e.eventDate) >= today)
            .sort((a, b) => new Date(a.eventDate) - new Date(b.eventDate))
            .slice(0, 5);
        
        if (upcoming.length === 0) {
            container.innerHTML = '<p class="text-muted">No upcoming events</p>';
            return;
        }
        
        container.innerHTML = `
            <div class="timeline">
                ${upcoming.map(event => {
                    const eventDate = new Date(event.eventDate);
                    const daysUntil = Math.ceil((eventDate - today) / (1000 * 60 * 60 * 24));
                    
                    return `
                        <div class="timeline-item mb-3 p-3 border-start border-primary border-4 bg-light">
                            <div class="row">
                                <div class="col-md-2">
                                    <div class="badge bg-primary">${formatDate(event.eventDate)}</div>
                                    <div class="text-muted small">${daysUntil} days</div>
                                </div>
                                <div class="col-md-10">
                                    <h6 class="mb-1">${event.title}</h6>
                                    <p class="mb-1 text-muted small">
                                        <i class="fas fa-map-marker-alt"></i> ${event.venue || 'N/A'} | 
                                        <i class="fas fa-users"></i> ${event.clubName || 'N/A'}
                                    </p>
                                    <button class="btn btn-sm btn-outline-primary" onclick="window.location.href='events.html'">
                                        <i class="fas fa-eye"></i> View Details
                                    </button>
                                </div>
                            </div>
                        </div>
                    `;
                }).join('')}
            </div>
        `;
        
    } catch (error) {
        console.error('Error:', error);
        container.innerHTML = '<p class="text-danger">Failed to load timeline</p>';
    }
}

// ========================================
// TASK MANAGEMENT
// ========================================

function initializeTasks() {
    const checkboxes = document.querySelectorAll('#tasksList input[type="checkbox"]');
    
    checkboxes.forEach(checkbox => {
        checkbox.addEventListener('change', function() {
            const taskItem = this.closest('.task-item');
            if (this.checked) {
                taskItem.classList.add('completed');
            } else {
                taskItem.classList.remove('completed');
            }
        });
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
 