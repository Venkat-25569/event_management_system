const API_URL = 'http://localhost:8080/api';
let eventId = null;
let stream = null;
let photoBlob = null;

// PAGE LOAD
document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    eventId = urlParams.get('eventId');
    
    if (!eventId) {
        showError('No event specified. Please scan a valid QR code.');
        return;
    }
    
    loadEventDetails();
    initCamera();
    
    document.getElementById('studentForm').addEventListener('submit', markAttendance);
});

// INITIALIZE CAMERA
async function initCamera() {
    try {
        stream = await navigator.mediaDevices.getUserMedia({ 
            video: { 
                facingMode: 'user',
                width: { ideal: 640 },
                height: { ideal: 480 }
            } 
        });
        
        const video = document.getElementById('video');
        video.srcObject = stream;
        video.play();
        
    } catch (error) {
        console.error('Camera error:', error);
        alert('⚠️ Camera access denied. Please allow camera access to mark attendance.');
        showError('Camera permission required. Please allow camera access and refresh the page.');
    }
}

// CAPTURE PHOTO
function capturePhoto() {
    const video = document.getElementById('video');
    const canvas = document.getElementById('canvas');
    const capturedImage = document.getElementById('capturedImage');
    
    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    
    const context = canvas.getContext('2d');
    context.drawImage(video, 0, 0, canvas.width, canvas.height);
    
    canvas.toBlob(function(blob) {
        photoBlob = blob;
        
        const imageUrl = URL.createObjectURL(blob);
        capturedImage.src = imageUrl;
        capturedImage.style.display = 'block';
        
        video.style.display = 'none';
        document.getElementById('captureBtn').style.display = 'none';
        document.getElementById('retakeBtn').style.display = 'inline-block';
        
        document.getElementById('studentForm').style.display = 'block';
        
    }, 'image/jpeg', 0.8);
}

// RETAKE PHOTO
function retakePhoto() {
    const video = document.getElementById('video');
    const capturedImage = document.getElementById('capturedImage');
    
    video.style.display = 'block';
    capturedImage.style.display = 'none';
    
    document.getElementById('captureBtn').style.display = 'inline-block';
    document.getElementById('retakeBtn').style.display = 'none';
    
    document.getElementById('studentForm').style.display = 'none';
    
    photoBlob = null;
}

 // LOAD EVENT DETAILS
async function loadEventDetails() {
    try {
        // No authentication needed - endpoint is public
        const response = await fetch(`${API_URL}/events/${eventId}`);
        
        if (!response.ok) {
            console.error('Event fetch failed:', response.status);
            throw new Error('Event not found');
        }
        
        const event = await response.json();
        console.log('Event loaded:', event); // Debug log
        
        if (event.status !== 'APPROVED') {
            showError('This event is not approved yet. Attendance marking is not available.');
            return;
        }
        
        document.getElementById('eventId').value = event.id;
        document.getElementById('eventTitle').textContent = event.title;
        document.getElementById('eventDate').textContent = formatDate(event.eventDate);
        document.getElementById('eventVenue').textContent = event.venue || 'N/A';
        
    } catch (error) {
        console.error('Error loading event:', error);
        showError('Unable to load event details. Event ID: ' + eventId + '. Please check if the event exists.');
    }
}

// MARK ATTENDANCE
async function markAttendance(e) {
    e.preventDefault();
    
    if (!photoBlob) {
        alert('Please capture your photo first!');
        return;
    }
    
    const studentName = document.getElementById('studentName').value.trim();
    const studentEmail = document.getElementById('studentEmail').value.trim();
    const rollNumber = document.getElementById('rollNumber').value.trim();
    const department = document.getElementById('department').value.trim();
    
    if (!studentName || !studentEmail) {
        alert('Please fill in all required fields (Name and Email)');
        return;
    }
    
    const submitBtn = document.querySelector('button[type="submit"]');
    submitBtn.disabled = true;
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Uploading...';
    
    try {
        const formData = new FormData();
        formData.append('eventId', eventId);
        formData.append('studentName', studentName);
        formData.append('studentEmail', studentEmail);
        formData.append('rollNumber', rollNumber);
        formData.append('department', department);
        formData.append('checkInMethod', 'QR_SCAN');
        formData.append('photo', photoBlob, 'attendance-photo.jpg');
        
        const response = await fetch(`${API_URL}/attendance/mark-with-photo`, {
            method: 'POST',
            body: formData
        });
        
        const result = await response.json();
        
        if (result.success) {
            stopCamera();
            showSuccess(result);
        } else {
            alert('⚠️ ' + result.message);
            submitBtn.disabled = false;
            submitBtn.innerHTML = '<i class="fas fa-check-circle"></i> Submit Attendance';
        }
        
    } catch (error) {
        console.error('Error marking attendance:', error);
        alert('❌ Failed to mark attendance. Please try again.');
        submitBtn.disabled = false;
        submitBtn.innerHTML = '<i class="fas fa-check-circle"></i> Submit Attendance';
    }
}

// STOP CAMERA
function stopCamera() {
    if (stream) {
        stream.getTracks().forEach(track => track.stop());
    }
}

// SHOW SUCCESS MESSAGE
function showSuccess(result) {
    document.getElementById('attendanceForm').style.display = 'none';
    
    const successDiv = document.getElementById('successMessage');
    successDiv.style.display = 'block';
    
    const capturedImage = document.getElementById('capturedImage');
    document.getElementById('successPhoto').src = capturedImage.src;
    
    document.getElementById('confirmedEvent').textContent = result.eventTitle || 'Event';
    document.getElementById('checkInTime').textContent = formatDateTime(result.checkInTime);
}

// SHOW ERROR MESSAGE
function showError(message) {
    stopCamera();
    document.getElementById('attendanceForm').style.display = 'none';
    document.getElementById('errorMessage').style.display = 'block';
    document.getElementById('errorText').textContent = message;
}

// UTILITY FUNCTIONS
function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    });
}

function formatDateTime(dateTimeString) {
    if (!dateTimeString) return 'N/A';
    const date = new Date(dateTimeString);
    return date.toLocaleString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// Clean up camera on page unload
window.addEventListener('beforeunload', stopCamera);