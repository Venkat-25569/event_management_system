 const API_URL = 'http://localhost:8080/api';

// Get auth headers
function getAuthHeaders() {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
    };
}

// Check if user is logged in
function checkAuth() {
    const token = localStorage.getItem('token');
    if (!token) {
        alert('Please login first');
        window.location.href = '../login.html';
        return false;
    }
    return true;
}

// Display user info
function displayUserInfo() {
    const fullName = localStorage.getItem('fullName');
    const role = localStorage.getItem('role');
    const username = localStorage.getItem('username');
    
    const userInfoElement = document.getElementById('userInfo');
    if (userInfoElement && fullName) {
        userInfoElement.innerHTML = `<i class="fas fa-user"></i> ${fullName} (${role})`;
    }
}

// Logout function
function logout() {
    if (confirm('Are you sure you want to logout?')) {
        localStorage.clear();
        window.location.href = '../login.html';
    }
}

// Load dashboard data on page load
document.addEventListener('DOMContentLoaded', function() {
    if (!checkAuth()) return;
    
    displayUserInfo();
    loadDashboardData();
});

async function loadDashboardData() {
    try {
        // Load events with auth token
        const eventsResponse = await fetch(`${API_URL}/events`, {
            headers: getAuthHeaders()
        });
        
        if (!eventsResponse.ok) {
            throw new Error('Failed to fetch events');
        }
        
        const events = await eventsResponse.json();
        
        // Load clubs with auth token
        const clubsResponse = await fetch(`${API_URL}/clubs`, {
            headers: getAuthHeaders()
        });
        
        if (!clubsResponse.ok) {
            throw new Error('Failed to fetch clubs');
        }
        
        const clubs = await clubsResponse.json();
        
        // Update statistics
        updateStatistics(events, clubs);
        
        // Display recent events
        displayRecentEvents(events);
        
    } catch (error) {
        console.error('Error loading dashboard:', error);
        alert('Failed to load dashboard data. Please try logging in again.');
        // Optionally redirect to login
        // window.location.href = '../login.html';
    }
}

function updateStatistics(events, clubs) {
    // Total events
    document.getElementById('totalEvents').textContent = events.length;
    
    // Pending events
    const pending = events.filter(e => e.status === 'PENDING').length;
    document.getElementById('pendingEvents').textContent = pending;
    
    // Approved events
    const approved = events.filter(e => e.status === 'APPROVED').length;
    document.getElementById('approvedEvents').textContent = approved;
    
    // Total clubs
    document.getElementById('totalClubs').textContent = clubs.length;
}

function displayRecentEvents(events) {
    const tbody = document.getElementById('eventsTableBody');
    
    if (events.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center text-muted">
                    <i class="fas fa-inbox fa-3x mb-3"></i>
                    <p>No events found. Create your first event!</p>
                </td>
            </tr>
        `;
        return;
    }
    
    // Show last 5 events
    const recentEvents = events.slice(-5).reverse();
    
    tbody.innerHTML = recentEvents.map(event => `
        <tr>
            <td><strong>${event.title}</strong></td>
            <td>${formatDate(event.eventDate)}</td>
            <td>${event.venue || 'N/A'}</td>
            <td>${event.clubName || 'N/A'}</td>
            <td>${getStatusBadge(event.status)}</td>
            <td>
                <button class="btn btn-sm btn-info" onclick="viewEvent(${event.id})">
                    <i class="fas fa-eye"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

function getStatusBadge(status) {
    const badges = {
        'PENDING': '<span class="badge bg-warning">Pending</span>',
        'APPROVED': '<span class="badge bg-success">Approved</span>',
        'REJECTED': '<span class="badge bg-danger">Rejected</span>'
    };
    return badges[status] || '<span class="badge bg-secondary">Unknown</span>';
}

function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { 
        year: 'numeric', 
        month: 'short', 
        day: 'numeric' 
    });
}

function viewEvent(id) {
    window.location.href = `events.html?id=${id}`;
}
// Show My Events link for STUDENT role
document.addEventListener('DOMContentLoaded', function() {
    const userRole = localStorage.getItem('role');
    const myEventsLink = document.getElementById('myEventsLink');
    
    if (myEventsLink && userRole === 'STUDENT') {
        myEventsLink.style.display = 'block';
    }
});