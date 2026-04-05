    const API_URL = 'http://localhost:8080/api';
let allEvents = [];

// ========================================
// AUTH FUNCTIONS
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
    if (!token) {
        alert('Please login first');
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

// ========================================
// PAGE LOAD
// ========================================

document.addEventListener('DOMContentLoaded', function () {
    console.log('Page loaded, checking auth...');
    
    if (!checkAuth()) return;

    displayUserInfo();
    
    const userRole = localStorage.getItem('role');
    console.log('User role:', userRole);
    
    // Show Create Event button only for ADMIN
    const createBtn = document.getElementById('createEventBtn');
    if (createBtn && userRole === 'ADMIN') {
        createBtn.style.display = 'inline-block';
    }
    
    // Show My Events and Payments links for STUDENT
    if (userRole === 'STUDENT') {
        const myEventsLink = document.getElementById('myEventsLink');
        const paymentsLink = document.getElementById('paymentsLink');
        if (myEventsLink) myEventsLink.style.display = 'block';
        if (paymentsLink) paymentsLink.style.display = 'block';
        if (myScoresLink) myScoresLink.style.display = 'block';
        
        // Hide Reports for students
        const reportsLink = document.getElementById('reportsLink');
        if (reportsLink) reportsLink.style.display = 'none';
    }
    
    // Load events
    loadEvents();
});

// ========================================
// LOAD EVENTS
// ========================================

async function loadEvents() {
    console.log('Loading events...');
    const tbody = document.getElementById('eventsTableBody');
    
    try {
        const response = await fetch(`${API_URL}/events`, {
            headers: getAuthHeaders()
        });

        console.log('Response status:', response.status);

        if (!response.ok) {
            throw new Error('Failed to fetch events');
        }

        allEvents = await response.json();
        console.log('Events loaded:', allEvents.length);
        displayEvents(allEvents);
    } catch (error) {
        console.error('Error loading events:', error);
        tbody.innerHTML = `
            <tr>
                <td colspan="8" class="text-center">
                    <div class="alert alert-danger">
                        <i class="fas fa-exclamation-triangle"></i> 
                        Failed to load events. Please check if the server is running.
                        <br><small>Error: ${error.message}</small>
                    </div>
                </td>
            </tr>
        `;
    }
}

// ========================================
// DISPLAY EVENTS IN TABLE
// ========================================

function displayEvents(events) {
    const tbody = document.getElementById('eventsTableBody');
    const userRole = localStorage.getItem('role');

    if (events.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8" class="text-center text-muted py-5">
                    <i class="fas fa-calendar-times fa-3x mb-3 d-block"></i>
                    <p>No events found.</p>
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = events.map(event => `
        <tr>
            <td>${event.id}</td>
            <td><strong>${event.title}</strong></td>
            <td>${formatDate(event.eventDate)}</td>
            <td>${event.venue || 'N/A'}</td>
            <td>${event.clubName || 'N/A'}</td>
            <td>${event.participantCount || 0}</td>
            <td>${getStatusBadge(event.status)}</td>
            <td>
                <div class="btn-group btn-group-sm">
                    <!-- Everyone can view -->
                    <button class="btn btn-info" onclick="viewEvent(${event.id})" title="View Details">
                        <i class="fas fa-eye"></i>
                    </button>
                    
                    <!-- STUDENT: Register button for APPROVED events -->
                    ${userRole === 'STUDENT' && event.status === 'APPROVED' ? `
                        <button class="btn btn-success" onclick="showRegisterModal(${event.id}, '${event.title.replace(/'/g, "\\\'")}')" title="Register">
                            <i class="fas fa-user-plus"></i>
                        </button>
                    ` : ''}
                    
                    <!-- ADMIN ONLY: Edit and Delete -->
                    ${userRole === 'ADMIN' ? `
                        <button class="btn btn-warning" onclick="editEvent(${event.id})" title="Edit Event">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn btn-danger" onclick="deleteEvent(${event.id})" title="Delete Event">
                            <i class="fas fa-trash"></i>
                        </button>
                    ` : ''}
                    
                    <!-- RUBRICS BUTTON (ADMIN & HOD only) -->
                    ${(userRole === 'ADMIN' || userRole === 'HOD') ? `
                        <button class="btn btn-sm" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; border: none;" 
                                onclick="manageRubrics(${event.id}, '${event.title.replace(/'/g, "\\\'")}', event)" 
                                title="Manage Rubrics">
                            🎯
                        </button>
                    ` : ''}
                    
                    <!-- Certificate for APPROVED events - NOT for students -->
                    ${event.status === 'APPROVED' && userRole !== 'STUDENT' ? `
                        <button class="btn btn-success" onclick="generateCertificate(${event.id})" title="Generate Certificate">
                            <i class="fas fa-certificate"></i>
                        </button>
                    ` : ''}
                    
                    <!-- ADMIN, HOD, EVENT_COORDINATOR: Approve/Reject -->
                    ${(userRole === 'ADMIN' || userRole === 'HOD' || userRole === 'EVENT_COORDINATOR') && event.status === 'PENDING' ? `
                        <button class="btn btn-success" onclick="approveEvent(${event.id})" title="Approve">
                            <i class="fas fa-check"></i>
                        </button>
                        <button class="btn btn-danger" onclick="rejectEvent(${event.id})" title="Reject">
                            <i class="fas fa-times"></i>
                        </button>
                    ` : ''}
                </div>
            </td>
        </tr>
    `).join('');
}

// ========================================
// RUBRICS MANAGEMENT FUNCTIONS
// ========================================

function manageRubrics(eventId, eventTitle, e) {
    e.stopPropagation();
    
    const userRole = localStorage.getItem('role');
    
    if (userRole !== 'ADMIN' && userRole !== 'HOD') {
        showAlert('Only ADMIN and HOD can manage rubrics!', 'danger');
        return;
    }
    
    localStorage.setItem('selectedEventId', eventId);
    localStorage.setItem('selectedEventTitle', eventTitle);
    
    showRubricsOptionsModal(eventId, eventTitle);
}

function showRubricsOptionsModal(eventId, eventTitle) {
    const modalHtml = `
        <div class="modal fade" id="rubricsOptionsModal" tabindex="-1">
            <div class="modal-dialog modal-lg">
                <div class="modal-content">
                    <div class="modal-header" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white;">
                        <h5 class="modal-title">
                            <i class="fas fa-bullseye"></i> Rubrics Management
                        </h5>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                    </div>
                    <div class="modal-body">
                        <div class="alert alert-info mb-4">
                            <strong>Event:</strong> ${eventTitle} <small>(ID: ${eventId})</small>
                        </div>
                        
                        <div class="row g-3">
                            <div class="col-md-6">
                                <div class="card h-100 border-primary">
                                    <div class="card-body text-center">
                                        <div class="mb-3" style="font-size: 3rem;">📝</div>
                                        <h5 class="card-title">Create Rubric</h5>
                                        <p class="card-text text-muted">Build evaluation criteria for this event</p>
                                        <button class="btn btn-primary w-100" onclick="openRubricBuilder(${eventId})">
                                            <i class="fas fa-plus"></i> Create New Rubric
                                        </button>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="col-md-6">
                                <div class="card h-100 border-success">
                                    <div class="card-body text-center">
                                        <div class="mb-3" style="font-size: 3rem;">⚙️</div>
                                        <h5 class="card-title">Admin Panel</h5>
                                        <p class="card-text text-muted">Manage participants and judges</p>
                                        <button class="btn btn-success w-100" onclick="openRubricsAdmin(${eventId})">
                                            <i class="fas fa-cog"></i> Open Admin Panel
                                        </button>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="col-md-6">
                                <div class="card h-100 border-warning">
                                    <div class="card-body text-center">
                                        <div class="mb-3" style="font-size: 3rem;">⚖️</div>
                                        <h5 class="card-title">Judge Scoring</h5>
                                        <p class="card-text text-muted">Score participants (judge access)</p>
                                        <button class="btn btn-warning w-100" onclick="openJudgeScoring(${eventId})">
                                            <i class="fas fa-balance-scale"></i> Judge Interface
                                        </button>
                                    </div>
                                </div>
                            </div>
                            
                            <div class="col-md-6">
                                <div class="card h-100 border-info">
                                    <div class="card-body text-center">
                                        <div class="mb-3" style="font-size: 3rem;">🏆</div>
                                        <h5 class="card-title">Live Scoreboard</h5>
                                        <p class="card-text text-muted">View results and analytics</p>
                                        <button class="btn btn-info w-100" onclick="openLiveScoreboard(${eventId})">
                                            <i class="fas fa-trophy"></i> View Scoreboard
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                    </div>
                </div>
            </div>
        </div>
    `;
    
    const existingModal = document.getElementById('rubricsOptionsModal');
    if (existingModal) {
        existingModal.remove();
    }
    
    document.body.insertAdjacentHTML('beforeend', modalHtml);
    
    const modal = new bootstrap.Modal(document.getElementById('rubricsOptionsModal'));
    modal.show();
}

function openRubricBuilder(eventId) {
    window.location.href = `../rubric-builder.html?event_id=${eventId}`;
}

function openRubricsAdmin(eventId) {
    window.location.href = `../rubrics-admin.html?event_id=${eventId}`;
}

function openJudgeScoring(eventId) {
    window.location.href = `../judge-scoring.html?event_id=${eventId}`;
}

function openLiveScoreboard(eventId) {
    window.location.href = `../live-scoreboard.html?event_id=${eventId}`;
}

// ========================================
// FILTER EVENTS
// ========================================

function filterEvents() {
    const status = document.getElementById('statusFilter').value;
    const search = document.getElementById('searchInput').value.toLowerCase();

    let filtered = allEvents;

    if (status !== 'ALL') {
        filtered = filtered.filter(e => e.status === status);
    }

    if (search) {
        filtered = filtered.filter(e =>
            e.title.toLowerCase().includes(search) ||
            (e.clubName && e.clubName.toLowerCase().includes(search)) ||
            (e.organizerName && e.organizerName.toLowerCase().includes(search)) ||
            (e.venue && e.venue.toLowerCase().includes(search))
        );
    }

    displayEvents(filtered);
}

// ========================================
// CREATE / EDIT MODAL
// ========================================

function openCreateModal() {
    const userRole = localStorage.getItem('role');
    
    if (userRole !== 'ADMIN') {
        showAlert('Only ADMIN can create events!', 'danger');
        return;
    }
    
    document.getElementById('modalTitle').textContent = 'Create Event';
    document.getElementById('eventForm').reset();
    document.getElementById('eventId').value = '';

    const fileSection = document.getElementById('fileUploadSection');
    if (fileSection) {
        fileSection.style.display = 'none';
    }
}

// ========================================
// SAVE EVENT
// ========================================

async function saveEvent() {
    const userRole = localStorage.getItem('role');
    const id = document.getElementById('eventId').value;
    
    if (userRole !== 'ADMIN') {
        showAlert('Only ADMIN can create or edit events!', 'danger');
        return;
    }
    
    const eventData = {
        title: document.getElementById('title').value,
        description: document.getElementById('description').value,
        eventDate: document.getElementById('eventDate').value,
        venue: document.getElementById('venue').value,
        clubName: document.getElementById('clubName').value,
        organizerName: document.getElementById('organizerName').value,
        participantCount: parseInt(document.getElementById('participantCount').value) || 0,
        status: id ? undefined : 'PENDING'
    };

    if (!eventData.title || !eventData.eventDate || !eventData.venue) {
        showAlert('Please fill all required fields (Title, Date, Venue)', 'warning');
        return;
    }

    try {
        let response;
        if (id) {
            response = await fetch(`${API_URL}/events/${id}`, {
                method: 'PUT',
                headers: getAuthHeaders(),
                body: JSON.stringify(eventData)
            });
        } else {
            response = await fetch(`${API_URL}/events`, {
                method: 'POST',
                headers: getAuthHeaders(),
                body: JSON.stringify(eventData)
            });
        }

        if (response.ok) {
            showAlert(id ? 'Event updated successfully!' : 'Event created successfully!', 'success');
            bootstrap.Modal.getInstance(document.getElementById('eventModal')).hide();
            loadEvents();
        } else if (response.status === 403) {
            showAlert('Access Denied! Only ADMIN can create or edit events.', 'danger');
        } else {
            const error = await response.json();
            showAlert(error.error || 'Failed to save event', 'danger');
        }
    } catch (error) {
        console.error('Error saving event:', error);
        showAlert('Error saving event. Please try again.', 'danger');
    }
}

// ========================================
// EDIT EVENT
// ========================================

async function editEvent(id) {
    const userRole = localStorage.getItem('role');
    
    if (userRole !== 'ADMIN') {
        showAlert('Only ADMIN can edit events!', 'danger');
        return;
    }
    
    try {
        const response = await fetch(`${API_URL}/events/${id}`, {
            headers: getAuthHeaders()
        });

        if (!response.ok) {
            throw new Error('Failed to load event');
        }

        const event = await response.json();

        document.getElementById('modalTitle').textContent = 'Edit Event';
        document.getElementById('eventId').value = event.id;
        document.getElementById('title').value = event.title;
        document.getElementById('description').value = event.description || '';
        document.getElementById('eventDate').value = event.eventDate;
        document.getElementById('venue').value = event.venue;
        document.getElementById('clubName').value = event.clubName || '';
        document.getElementById('organizerName').value = event.organizerName || '';
        document.getElementById('participantCount').value = event.participantCount || '';

        const fileSection = document.getElementById('fileUploadSection');
        if (fileSection) {
            fileSection.style.display = 'block';
        }

        new bootstrap.Modal(document.getElementById('eventModal')).show();
    } catch (error) {
        console.error('Error loading event:', error);
        showAlert('Failed to load event details', 'danger');
    }
}

// ========================================
// VIEW EVENT DETAILS
// ========================================

async function viewEvent(id) {
    try {
        const response = await fetch(`${API_URL}/events/${id}`, {
            headers: getAuthHeaders()
        });

        if (!response.ok) {
            throw new Error('Failed to load event');
        }

        const event = await response.json();
        const userRole = localStorage.getItem('role');

        const modalBody = document.getElementById('viewModalBody');
        modalBody.innerHTML = `
            <div class="row">
                <div class="col-md-12">
                    <div class="row">
                        <div class="col-md-6">
                            <p><strong><i class="fas fa-hashtag text-primary"></i> Event ID:</strong> ${event.id}</p>
                            <p><strong><i class="fas fa-heading text-primary"></i> Title:</strong> ${event.title}</p>
                            <p><strong><i class="fas fa-calendar text-primary"></i> Date:</strong> ${formatDate(event.eventDate)}</p>
                            <p><strong><i class="fas fa-map-marker-alt text-primary"></i> Venue:</strong> ${event.venue || 'N/A'}</p>
                        </div>
                        <div class="col-md-6">
                            <p><strong><i class="fas fa-users text-primary"></i> Club:</strong> ${event.clubName || 'N/A'}</p>
                            <p><strong><i class="fas fa-user text-primary"></i> Organizer:</strong> ${event.organizerName || 'N/A'}</p>
                            <p><strong><i class="fas fa-user-friends text-primary"></i> Participants:</strong> ${event.participantCount || 0}</p>
                            <p><strong><i class="fas fa-info-circle text-primary"></i> Status:</strong> ${getStatusBadge(event.status)}</p>
                        </div>
                    </div>
                    <hr>
                    <p><strong><i class="fas fa-align-left text-primary"></i> Description:</strong></p>
                    <p class="text-muted">${event.description || 'No description provided'}</p>
                    <hr>
                    
                    <div class="d-flex gap-2 mt-3 flex-wrap">
                        ${(userRole === 'ADMIN' || userRole === 'HOD' || userRole === 'EVENT_COORDINATOR') && event.status === 'PENDING' ? `
                            <button class="btn btn-success btn-sm" onclick="approveEvent(${event.id}); bootstrap.Modal.getInstance(document.getElementById('viewModal')).hide();">
                                <i class="fas fa-check"></i> Approve
                            </button>
                            <button class="btn btn-danger btn-sm" onclick="rejectEvent(${event.id}); bootstrap.Modal.getInstance(document.getElementById('viewModal')).hide();">
                                <i class="fas fa-times"></i> Reject
                            </button>
                        ` : ''}
                        ${userRole === 'ADMIN' ? `
                            <button class="btn btn-warning btn-sm" onclick="bootstrap.Modal.getInstance(document.getElementById('viewModal')).hide(); editEvent(${event.id});">
                                <i class="fas fa-edit"></i> Edit
                            </button>
                        ` : ''}
                        ${(userRole === 'ADMIN' || userRole === 'HOD') ? `
                            <button class="btn btn-sm" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white;" 
                                    onclick="bootstrap.Modal.getInstance(document.getElementById('viewModal')).hide(); manageRubrics(${event.id}, '${event.title.replace(/'/g, "\\\'").replace(/"/g, "&quot;")}', event);">
                                <i class="fas fa-bullseye"></i> Manage Rubrics
                            </button>
                        ` : ''}
                        <!-- Certificate - NOT for students -->
                        ${event.status === 'APPROVED' && userRole !== 'STUDENT' ? `
                            <button class="btn btn-success btn-sm" onclick="generateCertificate(${event.id})">
                                <i class="fas fa-certificate"></i> Get Certificate
                            </button>
                        ` : ''}
                    </div>
                </div>
            </div>
        `;

        new bootstrap.Modal(document.getElementById('viewModal')).show();
    } catch (error) {
        console.error('Error loading event:', error);
        showAlert('Failed to load event details', 'danger');
    }
}

// ========================================
// APPROVE EVENT
// ========================================

async function approveEvent(id) {
    const userRole = localStorage.getItem('role');
    
    if (userRole !== 'ADMIN' && userRole !== 'HOD' && userRole !== 'EVENT_COORDINATOR') {
        showAlert('You do not have permission to approve events!', 'danger');
        return;
    }
    
    if (!confirm('Are you sure you want to approve this event?')) return;

    try {
        const response = await fetch(`${API_URL}/events/${id}/approve`, {
            method: 'PUT',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            showAlert('Event approved successfully!', 'success');
            loadEvents();
        } else if (response.status === 403) {
            showAlert('Access Denied! You do not have permission to approve events.', 'danger');
        } else {
            showAlert('Failed to approve event', 'danger');
        }
    } catch (error) {
        console.error('Error approving event:', error);
        showAlert('Error approving event', 'danger');
    }
}

// ========================================
// REJECT EVENT
// ========================================

async function rejectEvent(id) {
    const userRole = localStorage.getItem('role');
    
    if (userRole !== 'ADMIN' && userRole !== 'HOD' && userRole !== 'EVENT_COORDINATOR') {
        showAlert('You do not have permission to reject events!', 'danger');
        return;
    }
    
    if (!confirm('Are you sure you want to reject this event?')) return;

    try {
        const response = await fetch(`${API_URL}/events/${id}/reject`, {
            method: 'PUT',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            showAlert('Event rejected successfully', 'warning');
            loadEvents();
        } else if (response.status === 403) {
            showAlert('Access Denied! You do not have permission to reject events.', 'danger');
        } else {
            showAlert('Failed to reject event', 'danger');
        }
    } catch (error) {
        console.error('Error rejecting event:', error);
        showAlert('Error rejecting event', 'danger');
    }
}

// ========================================
// DELETE EVENT
// ========================================

async function deleteEvent(id) {
    const userRole = localStorage.getItem('role');
    
    if (userRole !== 'ADMIN') {
        showAlert('Only ADMIN can delete events!', 'danger');
        return;
    }
    
    if (!confirm('Are you sure you want to delete this event? This action cannot be undone.')) return;

    try {
        const response = await fetch(`${API_URL}/events/${id}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });

        if (response.ok) {
            showAlert('Event deleted successfully', 'info');
            loadEvents();
        } else if (response.status === 403) {
            showAlert('Access Denied! Only ADMIN can delete events.', 'danger');
        } else {
            showAlert('Failed to delete event', 'danger');
        }
    } catch (error) {
        console.error('Error deleting event:', error);
        showAlert('Error deleting event', 'danger');
    }
}

// ========================================
// REGISTRATION FUNCTIONS
// ========================================

function showRegisterModal(eventId, eventTitle) {
    document.getElementById('registerEventId').value = eventId;
    document.getElementById('registerEventTitle').textContent = eventTitle;
    
    const fullName = localStorage.getItem('fullName');
    const email = localStorage.getItem('email');
    
    document.getElementById('registerStudentName').value = fullName || '';
    document.getElementById('registerStudentEmail').value = email || '';
    
    const modal = new bootstrap.Modal(document.getElementById('registerModal'));
    modal.show();
}

async function registerForEvent() {
    const eventId = document.getElementById('registerEventId').value;
    const studentName = document.getElementById('registerStudentName').value.trim();
    const studentEmail = document.getElementById('registerStudentEmail').value.trim();
    const rollNumber = document.getElementById('registerRollNumber').value.trim();
    const department = document.getElementById('registerDepartment').value.trim();
    
    if (!studentName || !studentEmail) {
        showAlert('Please fill all required fields', 'warning');
        return;
    }
    
    try {
        const response = await fetch(`${API_URL}/registrations/register`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify({
                eventId: parseInt(eventId),
                studentName: studentName,
                studentEmail: studentEmail,
                rollNumber: rollNumber,
                department: department
            })
        });
        
        const data = await response.json();
        
        if (response.ok && data.success) {
            showAlert('✅ ' + data.message, 'success');
            
            const modal = bootstrap.Modal.getInstance(document.getElementById('registerModal'));
            modal.hide();
            
            document.getElementById('registerForm').reset();
            
            setTimeout(() => {
                loadEvents();
            }, 1000);
        } else {
            showAlert('❌ ' + (data.message || 'Registration failed'), 'danger');
        }
    } catch (error) {
        console.error('Registration error:', error);
        showAlert('❌ Failed to register. Please try again.', 'danger');
    }
}

// ========================================
// GENERATE CERTIFICATE
// ========================================

async function generateCertificate(eventId) {
    const userRole = localStorage.getItem('role');
    const email = localStorage.getItem('email');
    
    if (userRole === 'STUDENT') {
        try {
            const attendanceResponse = await fetch(`${API_URL}/attendance/student/${email}`, {
                headers: getAuthHeaders()
            });
            
            if (attendanceResponse.ok) {
                const attendanceRecords = await attendanceResponse.json();
                const attended = attendanceRecords.some(record => record.eventId === eventId);
                
                if (!attended) {
                    showAlert('❌ You can only download certificates for events you attended!', 'danger');
                    return;
                }
            } else {
                showAlert('❌ Unable to verify attendance. Please try again.', 'danger');
                return;
            }
        } catch (error) {
            console.error('Error checking attendance:', error);
            showAlert('❌ Error verifying attendance', 'danger');
            return;
        }
    }
    
    const participantName = prompt(
        'Enter Participant Name for Certificate:',
        localStorage.getItem('fullName') || ''
    );

    if (!participantName || participantName.trim() === '') {
        showAlert('Please enter a participant name', 'warning');
        return;
    }

    try {
        showAlert('Generating Certificate...', 'info');

        const token = localStorage.getItem('token');
        const response = await fetch(
            `${API_URL}/certificate/generate/${eventId}?participantName=${encodeURIComponent(participantName)}`,
            {
                headers: {
                    'Authorization': token ? `Bearer ${token}` : ''
                }
            }
        );

        if (response.status === 400) {
            showAlert('Certificate only available for APPROVED events!', 'warning');
            return;
        }

        if (!response.ok) {
            throw new Error('Failed to generate certificate');
        }

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `certificate-${participantName.replace(/\s+/g, '_')}-event${eventId}.pdf`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);

        showAlert('Certificate downloaded successfully! 🎓', 'success');
    } catch (error) {
        console.error('Error generating certificate:', error);
        showAlert('Failed to generate certificate. Please try again.', 'danger');
    }
}

// ========================================
// EXPORT TO EXCEL
// ========================================

async function exportToExcel() {
    try {
        showAlert('Preparing Excel file...', 'info');

        const token = localStorage.getItem('token');
        const response = await fetch(`${API_URL}/export/events/excel`, {
            method: 'GET',
            headers: {
                'Authorization': token ? `Bearer ${token}` : ''
            }
        });

        if (!response.ok) {
            throw new Error('Export failed');
        }

        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Events_Report_${new Date().toISOString().slice(0, 10)}.xlsx`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);

        showAlert('Events exported successfully!', 'success');
    } catch (error) {
        console.error('Error exporting:', error);
        showAlert('Failed to export events. Please try again.', 'danger');
    }
}

// ========================================
// UTILITY FUNCTIONS
// ========================================

function getStatusBadge(status) {
    const badges = {
        'PENDING': '<span class="badge bg-warning text-dark">⏳ Pending</span>',
        'APPROVED': '<span class="badge bg-success">✅ Approved</span>',
        'REJECTED': '<span class="badge bg-danger">❌ Rejected</span>'
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

function showAlert(message, type) {
    const existingAlerts = document.querySelectorAll('.custom-alert');
    existingAlerts.forEach(alert => alert.remove());

    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show position-fixed top-0 start-50 translate-middle-x mt-3 custom-alert`;
    alertDiv.style.zIndex = '9999';
    alertDiv.style.minWidth = '300px';
    alertDiv.innerHTML = `
        <i class="fas fa-${type === 'success' ? 'check-circle' : type === 'danger' ? 'exclamation-circle' : type === 'warning' ? 'exclamation-triangle' : 'info-circle'}"></i>
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

    document.body.appendChild(alertDiv);

    setTimeout(() => {
        if (alertDiv.parentNode) {
            alertDiv.remove();
        }
    }, 3000);
}