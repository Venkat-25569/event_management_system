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
    loadClubs();
    
    const userRole = localStorage.getItem('role');
    
    if (userRole === 'ADMIN') {
        const createSection = document.getElementById('createClubSection');
        if (createSection) createSection.style.display = 'block';
    }
    
    if (userRole === 'STUDENT') {
        const myEventsLink = document.getElementById('myEventsLink');
        const paymentsLink = document.getElementById('paymentsLink');
        if (myEventsLink) myEventsLink.style.display = 'block';
        if (paymentsLink) paymentsLink.style.display = 'block';
        
        const reportsLink = document.getElementById('reportsLink');
        if (reportsLink) reportsLink.style.display = 'none';
    }
});

async function loadClubs() {
    const container = document.getElementById('clubsContainer');
    
    try {
        const response = await fetch(`${API_URL}/clubs`, {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) throw new Error('Failed to load clubs');
        
        const clubs = await response.json();
        console.log('Clubs loaded:', clubs);
        
        if (clubs.length === 0) {
            container.innerHTML = `
                <div class="col-12 text-center text-muted py-5">
                    <i class="fas fa-users fa-3x mb-3"></i>
                    <p>No clubs found.</p>
                </div>
            `;
            return;
        }
        
        const userRole = localStorage.getItem('role');
        
        container.innerHTML = clubs.map(club => `
            <div class="col-md-4 mb-4">
                <div class="card h-100 shadow-sm">
                    <div class="card-body">
                        <h5 class="card-title text-primary">
                            <i class="fas fa-users"></i> ${club.clubName || 'Unnamed Club'}
                        </h5>
                        <p class="card-text text-muted">${club.description || 'No description available'}</p>
                        <hr>
                        <p class="mb-1"><strong>Advisor:</strong> ${club.advisorName || 'N/A'}</p>
                        <p class="mb-1"><strong>Contact:</strong> ${club.contactEmail || 'N/A'}</p>
                    </div>
                    <div class="card-footer bg-white">
                        <div class="btn-group w-100">
                            <button class="btn btn-info btn-sm" onclick="viewClub(${club.id})">
                                <i class="fas fa-eye"></i> View
                            </button>
                            
                            ${userRole === 'ADMIN' ? `
                                <button class="btn btn-warning btn-sm" onclick="editClub(${club.id})">
                                    <i class="fas fa-edit"></i> Edit
                                </button>
                                <button class="btn btn-danger btn-sm" onclick="deleteClub(${club.id})">
                                    <i class="fas fa-trash"></i> Delete
                                </button>
                            ` : ''}
                        </div>
                    </div>
                </div>
            </div>
        `).join('');
        
    } catch (error) {
        console.error('Error:', error);
        container.innerHTML = `
            <div class="col-12">
                <div class="alert alert-danger">
                    <i class="fas fa-exclamation-triangle"></i> Failed to load clubs.
                </div>
            </div>
        `;
    }
}

async function viewClub(id) {
    try {
        const response = await fetch(`${API_URL}/clubs/${id}`, {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) throw new Error('Failed to load club');
        
        const club = await response.json();
        
        const modalHTML = `
            <div class="modal fade" id="viewClubModal" tabindex="-1">
                <div class="modal-dialog modal-lg">
                    <div class="modal-content">
                        <div class="modal-header bg-primary text-white">
                            <h5 class="modal-title">
                                <i class="fas fa-users"></i> ${club.clubName || 'Club Details'}
                            </h5>
                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <div class="row">
                                <div class="col-md-12">
                                    <h6 class="text-primary"><i class="fas fa-info-circle"></i> Club Information</h6>
                                    <hr>
                                    <div class="row mb-3">
                                        <div class="col-md-6">
                                            <p><strong>Club Name:</strong> ${club.clubName || 'N/A'}</p>
                                            <p><strong>Advisor:</strong> ${club.advisorName || 'N/A'}</p>
                                        </div>
                                        <div class="col-md-6">
                                            <p><strong>Contact Email:</strong> ${club.contactEmail || 'N/A'}</p>
                                            <p><strong>Club ID:</strong> #${club.id}</p>
                                        </div>
                                    </div>
                                    
                                    <h6 class="text-primary"><i class="fas fa-align-left"></i> Description</h6>
                                    <hr>
                                    <p class="text-muted">${club.description || 'No description available'}</p>
                                    
                                    <h6 class="text-primary mt-4"><i class="fas fa-calendar"></i> Upcoming Events</h6>
                                    <hr>
                                    <div id="clubEventsSection">
                                        <div class="spinner-border spinner-border-sm text-primary" role="status">
                                            <span class="visually-hidden">Loading...</span>
                                        </div>
                                        Loading events...
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
        
        const existingModal = document.getElementById('viewClubModal');
        if (existingModal) existingModal.remove();
        
        document.body.insertAdjacentHTML('beforeend', modalHTML);
        
        const modal = new bootstrap.Modal(document.getElementById('viewClubModal'));
        modal.show();
        
        loadClubEvents(club.clubName);
        
    } catch (error) {
        console.error('Error:', error);
        showAlert('Failed to load club details', 'danger');
    }
}

async function loadClubEvents(clubName) {
    const eventsSection = document.getElementById('clubEventsSection');
    
    try {
        const response = await fetch(`${API_URL}/events`, {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) throw new Error('Failed to load events');
        
        const allEvents = await response.json();
        const clubEvents = allEvents.filter(e => 
            e.clubName === clubName && e.status === 'APPROVED'
        );
        
        if (clubEvents.length === 0) {
            eventsSection.innerHTML = '<p class="text-muted">No upcoming events for this club.</p>';
            return;
        }
        
        eventsSection.innerHTML = clubEvents.map(event => `
            <div class="card mb-2">
                <div class="card-body p-2">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <strong>${event.title}</strong><br>
                            <small class="text-muted">
                                <i class="fas fa-calendar"></i> ${formatDate(event.eventDate)} | 
                                <i class="fas fa-map-marker-alt"></i> ${event.venue}
                            </small>
                        </div>
                        <a href="events.html" class="btn btn-sm btn-primary">View</a>
                    </div>
                </div>
            </div>
        `).join('');
        
    } catch (error) {
        console.error('Error:', error);
        eventsSection.innerHTML = '<p class="text-danger">Failed to load events</p>';
    }
}

function openCreateModal() {
    const userRole = localStorage.getItem('role');
    if (userRole !== 'ADMIN') {
        showAlert('Only ADMIN can create clubs!', 'danger');
        return;
    }
    
    document.getElementById('modalTitle').textContent = 'Create Club';
    document.getElementById('clubForm').reset();
    document.getElementById('clubId').value = '';
}

async function saveClub() {
    const userRole = localStorage.getItem('role');
    if (userRole !== 'ADMIN') {
        showAlert('Only ADMIN can create or edit clubs!', 'danger');
        return;
    }
    
    const id = document.getElementById('clubId').value;
    const clubName = document.getElementById('clubName').value.trim();
    const description = document.getElementById('description').value.trim();
    const advisorName = document.getElementById('advisorName').value.trim();
    const contactEmail = document.getElementById('contactEmail').value.trim();
    
    if (!clubName) {
        showAlert('Club name is required', 'warning');
        return;
    }
    
    const clubData = {
        clubName: clubName,
        description: description,
        advisorName: advisorName,
        contactEmail: contactEmail
    };
    
    console.log('Sending club data:', clubData);
    
    try {
        let response;
        if (id) {
            // Update existing club
            response = await fetch(`${API_URL}/clubs/${id}`, {
                method: 'PUT',
                headers: getAuthHeaders(),
                body: JSON.stringify(clubData)
            });
        } else {
            // Create new club
            response = await fetch(`${API_URL}/clubs`, {
                method: 'POST',
                headers: getAuthHeaders(),
                body: JSON.stringify(clubData)
            });
        }
        
        console.log('Response status:', response.status);
        
        if (response.ok) {
            const savedClub = await response.json();
            console.log('Club saved:', savedClub);
            
            showAlert(id ? 'Club updated successfully!' : 'Club created successfully!', 'success');
            
            const modalElement = document.getElementById('clubModal');
            const modal = bootstrap.Modal.getInstance(modalElement);
            if (modal) {
                modal.hide();
            }
            
            // Reload clubs list
            setTimeout(() => {
                loadClubs();
            }, 500);
        } else {
            const errorData = await response.json();
            console.error('Error response:', errorData);
            showAlert(errorData.error || 'Failed to save club', 'danger');
        }
    } catch (error) {
        console.error('Error saving club:', error);
        showAlert('Error saving club: ' + error.message, 'danger');
    }
}

async function editClub(id) {
    const userRole = localStorage.getItem('role');
    if (userRole !== 'ADMIN') {
        showAlert('Only ADMIN can edit clubs!', 'danger');
        return;
    }
    
    try {
        const response = await fetch(`${API_URL}/clubs/${id}`, {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) throw new Error('Failed to load club');
        
        const club = await response.json();
        
        document.getElementById('modalTitle').textContent = 'Edit Club';
        document.getElementById('clubId').value = club.id;
        document.getElementById('clubName').value = club.clubName || '';
        document.getElementById('description').value = club.description || '';
        document.getElementById('advisorName').value = club.advisorName || '';
        document.getElementById('contactEmail').value = club.contactEmail || '';
        
        new bootstrap.Modal(document.getElementById('clubModal')).show();
    } catch (error) {
        console.error('Error:', error);
        showAlert('Failed to load club', 'danger');
    }
}

async function deleteClub(id) {
    const userRole = localStorage.getItem('role');
    if (userRole !== 'ADMIN') {
        showAlert('Only ADMIN can delete clubs!', 'danger');
        return;
    }
    
    if (!confirm('Are you sure you want to delete this club?')) return;
    
    try {
        const response = await fetch(`${API_URL}/clubs/${id}`, {
            method: 'DELETE',
            headers: getAuthHeaders()
        });
        
        if (response.ok) {
            showAlert('Club deleted successfully', 'success');
            loadClubs();
        } else {
            showAlert('Failed to delete club', 'danger');
        }
    } catch (error) {
        console.error('Error:', error);
        showAlert('Error deleting club', 'danger');
    }
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