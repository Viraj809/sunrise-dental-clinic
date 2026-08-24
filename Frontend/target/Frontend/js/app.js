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
    if (role === 'ADMIN' || role === 'SYSTEM_ADMIN') {
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

    const current = window.location.pathname;
    let html = '';
    if (role === 'ADMIN' || role === 'SYSTEM_ADMIN') {
        html += '<a href="/Frontend/admin-dashboard.html" class="' + (current.includes('admin-dashboard') ? 'active' : '') + '">Dashboard</a>';
        html += '<a href="/Frontend/manage-staff.html" class="' + (current.includes('manage-staff') ? 'active' : '') + '">Manage Staff</a>';
        html += '<a href="/Frontend/manage-dentists.html" class="' + (current.includes('manage-dentists') ? 'active' : '') + '">Dentists</a>';
        html += '<a href="/Frontend/manage-patients.html" class="' + (current.includes('manage-patients') ? 'active' : '') + '">Patients</a>';
        html += '<a href="/Frontend/view-appointment.html" class="' + (current.includes('view-appointment') ? 'active' : '') + '">Appointments</a>';
        html += '<a href="/Frontend/manage-treatments.html" class="' + (current.includes('manage-treatments') ? 'active' : '') + '">Treatments</a>';
        html += '<a href="/Frontend/billing.html" class="' + (current.includes('billing') ? 'active' : '') + '">Billing</a>';
        html += '<a href="/Frontend/reports.html" class="' + (current.includes('reports') ? 'active' : '') + '">Reports</a>';
        html += '<a href="/Frontend/notifications.html" class="' + (current.includes('notifications') ? 'active' : '') + '">Notifications</a>';
        // Audit Log has been completely removed from here
    } else if (role === 'RECEPTIONIST') {
        html += '<a href="/Frontend/receptionist-dashboard.html" class="' + (current.includes('receptionist-dashboard') ? 'active' : '') + '">Dashboard</a>';
        html += '<a href="/Frontend/register-appointment.html" class="' + (current.includes('register-appointment') ? 'active' : '') + '">New Appointment</a>';
        html += '<a href="/Frontend/view-appointment.html" class="' + (current.includes('view-appointment') ? 'active' : '') + '">Appointments</a>';
        html += '<a href="/Frontend/manage-patients.html" class="' + (current.includes('manage-patients') ? 'active' : '') + '">Patients</a>';
        html += '<a href="/Frontend/billing.html" class="' + (current.includes('billing') ? 'active' : '') + '">Billing</a>';    
    } else if (role === 'DENTIST') {
        html += '<a href="/Frontend/dentist-dashboard.html" class="' + (current.includes('dentist-dashboard') ? 'active' : '') + '">Dashboard</a>';
        html += '<a href="/Frontend/view-appointment.html" class="' + (current.includes('view-appointment') ? 'active' : '') + '">Appointments</a>';
        html += '<a href="/Frontend/dentist-schedule.html" class="' + (current.includes('dentist-schedule') ? 'active' : '') + '">My Schedule</a>';
        html += '<a href="/Frontend/patient-history.html" class="' + (current.includes('patient-history') ? 'active' : '') + '">Patient History</a>';
    } else if (role === 'PATIENT') {
        html += '<a href="/Frontend/patient-dashboard.html" class="' + (current.includes('patient-dashboard') ? 'active' : '') + '">Dashboard</a>';
        html += '<a href="/Frontend/patient-appointments.html" class="' + (current.includes('patient-appointments') ? 'active' : '') + '">Appointments</a>';
        html += '<a href="/Frontend/patient-history.html" class="' + (current.includes('patient-history') ? 'active' : '') + '">Treatment History</a>';
        html += '<a href="/Frontend/patient-billing.html" class="' + (current.includes('patient-billing') ? 'active' : '') + '">Billing</a>';
        html += '<a href="/Frontend/patient-notifications.html" class="' + (current.includes('patient-notifications') ? 'active' : '') + '">Notifications</a>';
        html += '<a href="/Frontend/patient-profile.html" class="' + (current.includes('patient-profile') ? 'active' : '') + '">Profile</a>';
    }
    html += '<a href="/Frontend/help.html" class="' + (current.includes('help') ? 'active' : '') + '">Help</a>';
    html += '<a href="#" onclick="logout(); return false;" class="logout-link">Logout</a>';
    nav.innerHTML = html;
}

document.addEventListener('DOMContentLoaded', function() {
    showNav();
    const userInfo = document.getElementById('user-info');
    if (userInfo && userName) {
        userInfo.textContent = userName + ' (' + (userRole || '') + ')';
    }
});