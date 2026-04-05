const API_URL = 'http://localhost:8080/api';

// Get auth headers
function getAuthHeaders() {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
    };
}

// Load analytics on page load
document.addEventListener('DOMContentLoaded', function() {
    const token = localStorage.getItem('token');
    if (!token) {
        alert('Please login first');
        window.location.href = '../login.html';
        return;
    }
    loadAnalytics();
});

async function loadAnalytics() {
    try {
        // Load summary
        const summaryResponse = await fetch(`${API_URL}/analytics/summary`, {
            headers: getAuthHeaders()
        });
        
        if (!summaryResponse.ok) {
            throw new Error('Failed to fetch summary');
        }
        
        const summary = await summaryResponse.json();
        
        // Update summary cards
        document.getElementById('totalEvents').textContent = summary.totalEvents;
        document.getElementById('pendingEvents').textContent = summary.pendingEvents;
        document.getElementById('totalParticipants').textContent = summary.totalParticipants;
        document.getElementById('totalClubs').textContent = summary.totalClubs;
        
        // Load charts data
        await loadMonthChart();
        await loadStatusChart();
        await loadClubChart();
        
    } catch (error) {
        console.error('Error loading analytics:', error);
        alert('Failed to load analytics data');
    }
}

async function loadMonthChart() {
    try {
        const response = await fetch(`${API_URL}/analytics/events-by-month`, {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) {
            throw new Error('Failed to fetch month data');
        }
        
        const data = await response.json();
        
        const ctx = document.getElementById('monthChart').getContext('2d');
        new Chart(ctx, {
            type: 'line',
            data: {
                labels: data.labels,
                datasets: [{
                    label: 'Events',
                    data: data.data,
                    backgroundColor: 'rgba(54, 162, 235, 0.2)',
                    borderColor: 'rgba(54, 162, 235, 1)',
                    borderWidth: 2,
                    tension: 0.4,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        display: true,
                        position: 'top'
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 1
                        }
                    }
                }
            }
        });
    } catch (error) {
        console.error('Error loading month chart:', error);
    }
}

async function loadStatusChart() {
    try {
        const response = await fetch(`${API_URL}/analytics/events-by-status`, {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) {
            throw new Error('Failed to fetch status data');
        }
        
        const data = await response.json();
        
        const ctx = document.getElementById('statusChart').getContext('2d');
        new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: data.labels,
                datasets: [{
                    data: data.data,
                    backgroundColor: [
                        'rgba(255, 206, 86, 0.8)',
                        'rgba(75, 192, 192, 0.8)',
                        'rgba(255, 99, 132, 0.8)'
                    ],
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        position: 'bottom'
                    }
                }
            }
        });
    } catch (error) {
        console.error('Error loading status chart:', error);
    }
}

async function loadClubChart() {
    try {
        const response = await fetch(`${API_URL}/analytics/events-by-club`, {
            headers: getAuthHeaders()
        });
        
        if (!response.ok) {
            throw new Error('Failed to fetch club data');
        }
        
        const data = await response.json();
        
        const ctx = document.getElementById('clubChart').getContext('2d');
        new Chart(ctx, {
            type: 'bar',
            data: {
                labels: data.labels,
                datasets: [{
                    label: 'Number of Events',
                    data: data.data,
                    backgroundColor: [
                        'rgba(255, 99, 132, 0.7)',
                        'rgba(54, 162, 235, 0.7)',
                        'rgba(255, 206, 86, 0.7)',
                        'rgba(75, 192, 192, 0.7)',
                        'rgba(153, 102, 255, 0.7)',
                        'rgba(255, 159, 64, 0.7)'
                    ],
                    borderColor: [
                        'rgba(255, 99, 132, 1)',
                        'rgba(54, 162, 235, 1)',
                        'rgba(255, 206, 86, 1)',
                        'rgba(75, 192, 192, 1)',
                        'rgba(153, 102, 255, 1)',
                        'rgba(255, 159, 64, 1)'
                    ],
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: {
                        display: false
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            stepSize: 1
                        }
                    }
                }
            }
        });
    } catch (error) {
        console.error('Error loading club chart:', error);
    }
    // Show My Events link for STUDENT role
document.addEventListener('DOMContentLoaded', function() {
    const userRole = localStorage.getItem('role');
    const myEventsLink = document.getElementById('myEventsLink');
    
    if (myEventsLink && userRole === 'STUDENT') {
        myEventsLink.style.display = 'block';
    }
});
}