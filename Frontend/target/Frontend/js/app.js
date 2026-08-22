const API_BASE = 'http://localhost:8080/Backend/resources';
let authToken = localStorage.getItem('token');
let userRole  = localStorage.getItem('role');
let userName  = localStorage.getItem('name');

// ── Auth guard: call this at the top of every dashboard page ──────────────
function requireAuth(allowedRoles) {
    if (!authToken) {
        window.location.href = '/Frontend/login.html';
        return false;
    }
    if (allowedRoles && allowedRoles.length > 0 && !allowedRoles.includes(userRole)) {
        // Wrong role – send to their own dashboard
        redirectToDashboard(userRole);
        return false;
    }
    return true;
}

function redirectToDashboard(role) {
    if (role === 'ADMIN') {
        window.location.href = '/Frontend/admin-dashboard.html';
    } else if (role === 'RECEPTIONIST') {
        window.location.href = '/Frontend/receptionist-dashboard.html';
    } else if (role === 'DENTIST') {
        window.location.href = '/Frontend/dentist-dashboard.html';
    } else if (role === 'PATIENT') {
        window.location.href = '/Frontend/patient-dashboard.html';
    } else {
        window.location.href = '/Frontend/login.html';
    }
}

// ── Logout ────────────────────────────────────────────────────────────────
function logout() {
    const token = localStorage.getItem('token');
    if (token) {
        // Best-effort server-side session invalidation.
        fetch(API_BASE + '/auth/logout', {
            method: 'POST',
            headers: { 'Authorization': 'Bearer ' + token }
        }).catch(() => {});
    }
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('name');
    localStorage.removeItem('staffId');
    localStorage.removeItem('patientId');
    localStorage.removeItem('email');
    window.location.href = '/Frontend/login.html';
}

function getHeaders() {
    const headers = { 'Content-Type': 'application/json' };
    if (authToken) headers['Authorization'] = 'Bearer ' + authToken;
    return headers;
}

async function apiGet(path) {
    const res = await fetch(API_BASE + path, { headers: getHeaders() });
    if (res.status === 401) { logout(); throw new Error('Unauthorized'); }
    return res.json();
}

async function apiPost(path, data) {
    const res = await fetch(API_BASE + path, {
        method: 'POST',
        headers: getHeaders(),
        body: JSON.stringify(data)
    });
    if (res.status === 401) { logout(); throw new Error('Unauthorized'); }
    return res.json();
}

async function apiPut(path, data) {
    const res = await fetch(API_BASE + path, {
        method: 'PUT',
        headers: getHeaders(),
        body: JSON.stringify(data)
    });
    if (res.status === 401) { logout(); throw new Error('Unauthorized'); }
    return res.json();
}

async function apiDelete(path) {
    const res = await fetch(API_BASE + path, {
        method: 'DELETE',
        headers: getHeaders()
    });
    if (res.status === 401) { logout(); throw new Error('Unauthorized'); }
    return res.json();
}

function redirectToLogin() { logout(); }

function showToast(message, type = 'success') {
    const container = document.getElementById('toast-container');
    if (!container) return;
    const toast = document.createElement('div');
    toast.className = 'toast toast-' + type;
    toast.textContent = message;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 3000);
}

function formatDate(dateStr) {
    if (!dateStr) return '-';
    return dateStr;
}

function getStatusBadge(status) {
    const cls = 'status-' + String(status).toLowerCase().replace(/[^a-z0-9]+/g, '-');
    return '<span class="status-badge ' + cls + '">' + status + '</span>';
}

function canAccess(role, allowed) {
    return allowed.includes(role);
}

function showNav() {
    const role = localStorage.getItem('role');
    const nav  = document.getElementById('main-nav');
    if (!nav) return;

    let html = '';
    if (role === 'ADMIN') {
        html += '<a href="/Frontend/admin-dashboard.html">Dashboard</a>';
        html += '<a href="/Frontend/manage-staff.html">Manage Staff</a>';
        html += '<a href="/Frontend/manage-dentists.html">Dentists</a>';
        html += '<a href="/Frontend/manage-patients.html">Patients</a>';
        html += '<a href="/Frontend/view-appointment.html">Appointments</a>';
        html += '<a href="/Frontend/manage-treatments.html">Treatments</a>';
        html += '<a href="/Frontend/billing.html">Billing</a>';
        html += '<a href="/Frontend/reports.html">Reports</a>';
        html += '<a href="/Frontend/notifications.html">Notifications</a>';
        html += '<a href="/Frontend/audit-log.html">Audit Log</a>';
    } else if (role === 'RECEPTIONIST') {
        html += '<a href="/Frontend/receptionist-dashboard.html">Dashboard</a>';
        html += '<a href="/Frontend/register-appointment.html">New Appointment</a>';
        html += '<a href="/Frontend/view-appointment.html">Appointments</a>';
        html += '<a href="/Frontend/manage-patients.html">Patients</a>';
        html += '<a href="/Frontend/billing.html">Billing</a>';
        html += '<a href="/Frontend/queue.html">Queue</a>';
    } else if (role === 'DENTIST') {
        html += '<a href="/Frontend/dentist-dashboard.html">Dashboard</a>';
        html += '<a href="/Frontend/view-appointment.html">Appointments</a>';
        html += '<a href="/Frontend/dentist-schedule.html">My Schedule</a>';
        html += '<a href="/Frontend/patient-history.html">Patient History</a>';
    } else if (role === 'PATIENT') {
        html += '<a href="/Frontend/patient-dashboard.html">Dashboard</a>';
        html += '<a href="/Frontend/patient-appointments.html">Appointments</a>';
        html += '<a href="/Frontend/patient-history.html">Treatment History</a>';
        html += '<a href="/Frontend/patient-billing.html">Billing</a>';
        html += '<a href="/Frontend/patient-notifications.html">Notifications</a>';
        html += '<a href="/Frontend/patient-profile.html">Profile</a>';
    }
    html += '<a href="/Frontend/help.html">Help</a>';
    html += '<a href="#" onclick="logout(); return false;" style="color:#f87171;">Logout</a>';
    nav.innerHTML = html;
}

document.addEventListener('DOMContentLoaded', function() {
    showNav();
    const userInfo = document.getElementById('user-info');
    if (userInfo && userName) {
        userInfo.textContent = userName + ' (' + (userRole || '') + ')';
    }
});

