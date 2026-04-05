const API_URL = 'http://localhost:8080/api';
let calendar;

// Check authentication
document.addEventListener('DOMContentLoaded', function() {
    const token = localStorage.getItem('token');
    if (!token) {
        alert('Please login first');
        window.location.href = '../login.html';
        return;
    }
    
    displayUserInfo();
    initializeCalendar();
});

// Display user info
function displayUserInfo() {
    const fullName = localStorage.getItem('fullName');
    const role = localStorage.getItem('role');
    
    if (fullName) {
        document.getElementById('userName').textContent = `${fullName} (${role})`;
    }
}

// Logout function
function logout() {
    if (confirm('Are you sure you want to logout?')) {
        localStorage.clear();
        window.location.href = '../login.html';
    }
}

// Initialize FullCalendar
function initializeCalendar() {
    const calendarEl = document.getElementById('calendar');
    
    calendar = new FullCalendar.Calendar(calendarEl, {
        initialView: 'dayGridMonth',
        headerToolbar: {
            left: 'prev,next today',
            center: 'title',
            right: 'dayGridMonth,timeGridWeek,timeGridDay,listMonth'
        },
        height: 'auto',
        events: async function(info, successCallback, failureCallback) {
            try {
                const response = await fetch(`${API_URL}/calendar/events`, {
                    headers: {
                        'Authorization': 'Bearer ' + localStorage.getItem('token')
                    }
                });
                
                if (!response.ok) {
                    throw new Error('Failed to fetch events');
                }
                
                const events = await response.json();
                successCallback(events);
            } catch (error) {
                console.error('Error loading calendar events:', error);
                failureCallback(error);
            }
        },
        eventClick: function(info) {
            showEventDetails(info.event);
        },
        eventMouseEnter: function(info) {
            info.el.style.cursor = 'pointer';
        }
    });
    
    calendar.render();
}

// Show event details in modal
function showEventDetails(event) {
    const modal = new bootstrap.Modal(document.getElementById('eventModal'));
    
    document.getElementById('eventModalTitle').textContent = event.title;
    
    const statusBadge = getStatusBadge(event.extendedProps.status || 'PENDING');
    
    document.getElementById('eventModalBody').innerHTML = `
        <div class="row">
            <div class="col-md-6">
                <p><strong>📅 Date:</strong> ${formatDate(event.start)}</p>
                <p><strong>📍 Venue:</strong> ${event.extendedProps.venue || 'N/A'}</p>
                <p><strong>🏢 Club:</strong> ${event.extendedProps.club || 'N/A'}</p>
            </div>
            <div class="col-md-6">
                <p><strong>👤 Organizer:</strong> ${event.extendedProps.organizer || 'N/A'}</p>
                <p><strong>👥 Participants:</strong> ${event.extendedProps.participants || 0}</p>
                <p><strong>📊 Status:</strong> ${statusBadge}</p>
            </div>
        </div>
        <hr>
        <p><strong>📝 Description:</strong></p>
        <p>${event.extendedProps.description || 'No description available'}</p>
    `;
    
    modal.show();
}

// Get status badge HTML
function getStatusBadge(status) {
    const badges = {
        'PENDING': '<span class="badge bg-warning">Pending</span>',
        'APPROVED': '<span class="badge bg-success">Approved</span>',
        'REJECTED': '<span class="badge bg-danger">Rejected</span>'
    };
    return badges[status] || '<span class="badge bg-secondary">Unknown</span>';
}

// Format date
function formatDate(date) {
    if (!date) return 'N/A';
    const d = new Date(date);
    return d.toLocaleDateString('en-US', { 
        weekday: 'long',
        year: 'numeric', 
        month: 'long', 
        day: 'numeric' 
    });
    // Show My Events link for STUDENT role
document.addEventListener('DOMContentLoaded', function() {
    const userRole = localStorage.getItem('role');
    const myEventsLink = document.getElementById('myEventsLink');
    
    if (myEventsLink && userRole === 'STUDENT') {
        myEventsLink.style.display = 'block';
    }
});
}