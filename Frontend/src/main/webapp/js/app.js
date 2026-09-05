const API_BASE = 'http://localhost:8080/Backend/resources';
let authToken = localStorage.getItem('token');
let userRole  = localStorage.getItem('role');
let userName  = localStorage.getItem('name');
let sessionStart = Date.now();
const SESSION_TIMEOUT = 30 * 60 * 1000;

// ── Auth guard ─────────────────────────────────────────────────────────────
function requireAuth(allowedRoles) {
    if (!authToken) {
        window.location.href = '/Frontend/login.html';
        return false;
    }
    if (Date.now() - sessionStart > SESSION_TIMEOUT) {
        logout();
        showToast('Your session has expired. Please login again.', 'error');
        window.location.href = '/Frontend/login.html';
        return false;
    }
    if (allowedRoles && allowedRoles.length > 0 && !allowedRoles.includes(userRole)) {
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
        showToast('Patient portal is not available. Please contact the clinic.', 'error');
        setTimeout(() => logout(), 1500);
    } else {
        window.location.href = '/Frontend/login.html';
    }
}

// ── Session timeout handler ────────────────────────────────────────────────
function resetSessionTimer() {
    sessionStart = Date.now();
}
['click', 'keypress', 'mousemove', 'scroll', 'touchstart'].forEach(evt =>
    document.addEventListener(evt, resetSessionTimer)
);

// ── Logout ─────────────────────────────────────────────────────────────────
function logout() {
    const token = localStorage.getItem('token');
    if (token) {
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

// ── API Helpers ────────────────────────────────────────────────────────────
async function apiGet(path) {
    const res = await fetch(API_BASE + path, { headers: getHeaders() });
    if (res.status === 401) { logout(); throw new Error('Unauthorized'); }
    if (res.status === 403) { showToast('Access Denied', 'error'); throw new Error('Forbidden'); }
    return res.json();
}

async function apiPost(path, data) {
    const res = await fetch(API_BASE + path, {
        method: 'POST',
        headers: getHeaders(),
        body: JSON.stringify(data)
    });
    if (res.status === 401) { logout(); throw new Error('Unauthorized'); }
    if (res.status === 403) { showToast('Access Denied', 'error'); throw new Error('Forbidden'); }
    return res.json();
}

async function apiPut(path, data) {
    const res = await fetch(API_BASE + path, {
        method: 'PUT',
        headers: getHeaders(),
        body: JSON.stringify(data)
    });
    if (res.status === 401) { logout(); throw new Error('Unauthorized'); }
    if (res.status === 403) { showToast('Access Denied', 'error'); throw new Error('Forbidden'); }
    return res.json();
}

async function apiDelete(path) {
    const res = await fetch(API_BASE + path, {
        method: 'DELETE',
        headers: getHeaders()
    });
    if (res.status === 401) { logout(); throw new Error('Unauthorized'); }
    if (res.status === 403) { showToast('Access Denied', 'error'); throw new Error('Forbidden'); }
    return res.json();
}

// ── UI Helpers ─────────────────────────────────────────────────────────────
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

// ── Navigation ─────────────────────────────────────────────────────────────
function showNav() {
    const role = localStorage.getItem('role');
    const nav  = document.getElementById('main-nav');
    if (!nav) return;

    const current = window.location.pathname;
    let html = '';

    if (role === 'ADMIN' || role === 'SYSTEM_ADMIN') {
        html += '<a href="/Frontend/admin-dashboard.html" class="' + (current.includes('admin-dashboard') ? 'active' : '') + '">Dashboard</a>';
        html += '<a href="/Frontend/manage-patients.html" class="' + (current.includes('manage-patients') ? 'active' : '') + '">Patients</a>';
        html += '<a href="/Frontend/manage-dentists.html" class="' + (current.includes('manage-dentists') ? 'active' : '') + '">Dentists</a>';
        html += '<a href="/Frontend/view-appointment.html" class="' + (current.includes('view-appointment') ? 'active' : '') + '">Appointments</a>';
        html += '<a href="/Frontend/manage-treatments.html" class="' + (current.includes('manage-treatments') ? 'active' : '') + '">Treatments</a>';
        html += '<a href="/Frontend/billing.html" class="' + (current.includes('billing') ? 'active' : '') + '">Billing</a>';
        html += '<a href="/Frontend/reports.html" class="' + (current.includes('reports') ? 'active' : '') + '">Reports</a>';
        html += '<a href="/Frontend/manage-staff.html" class="' + (current.includes('manage-staff') ? 'active' : '') + '">Staff</a>';
        html += '<a href="/Frontend/manage-notices.html" class="' + (current.includes('manage-notices') ? 'active' : '') + '">Notices</a>';
        html += '<a href="/Frontend/help.html" class="' + (current.includes('help') ? 'active' : '') + '">Help</a>';
        html += '<a href="/Frontend/profile.html" class="' + (current.includes('profile') ? 'active' : '') + '">Profile</a>';
        html += '<a href="#" onclick="logout(); return false;" class="logout-link">Logout</a>';
    } else if (role === 'RECEPTIONIST') {
        html += '<a href="/Frontend/receptionist-dashboard.html" class="' + (current.includes('receptionist-dashboard') ? 'active' : '') + '">Dashboard</a>';
        html += '<a href="/Frontend/manage-patients.html" class="' + (current.includes('manage-patients') ? 'active' : '') + '">Patients</a>';
        html += '<a href="/Frontend/view-appointment.html" class="' + (current.includes('view-appointment') ? 'active' : '') + '">Appointments</a>';
        html += '<a href="/Frontend/billing.html" class="' + (current.includes('billing') ? 'active' : '') + '">Billing</a>';
        html += '<a href="/Frontend/notices.html" class="' + (current.includes('notices') ? 'active' : '') + '">Notices</a>';
        html += '<a href="/Frontend/help.html" class="' + (current.includes('help') ? 'active' : '') + '">Help</a>';
        html += '<a href="/Frontend/profile.html" class="' + (current.includes('profile') ? 'active' : '') + '">Profile</a>';
        html += '<a href="#" onclick="logout(); return false;" class="logout-link">Logout</a>';
    } else if (role === 'DENTIST') {
        html += '<a href="/Frontend/dentist-dashboard.html" class="' + (current.includes('dentist-dashboard') ? 'active' : '') + '">Dashboard</a>';
        html += '<a href="/Frontend/dentist-schedule.html" class="' + (current.includes('dentist-schedule') ? 'active' : '') + '">My Appointments</a>';
        html += '<a href="/Frontend/patient-history.html" class="' + (current.includes('patient-history') ? 'active' : '') + '">Patient History</a>';
        html += '<a href="/Frontend/dentist-reports.html" class="' + (current.includes('dentist-reports') ? 'active' : '') + '">Reports</a>';
        html += '<a href="/Frontend/notices.html" class="' + (current.includes('notices') ? 'active' : '') + '">Notices</a>';
        html += '<a href="/Frontend/help.html" class="' + (current.includes('help') ? 'active' : '') + '">Help</a>';
        html += '<a href="/Frontend/profile.html" class="' + (current.includes('profile') ? 'active' : '') + '">Profile</a>';
        html += '<a href="#" onclick="logout(); return false;" class="logout-link">Logout</a>';
    } else if (role === 'PATIENT') {
        showToast('Patient portal is not available. Please contact the clinic.', 'error');
        setTimeout(() => logout(), 1500);
    }

    nav.innerHTML = html;
}

document.addEventListener('DOMContentLoaded', function() {
    showNav();
    const userInfo = document.getElementById('user-info');
    if (userInfo && userName) {
        userInfo.textContent = userName + ' (' + (userRole || '') + ')';
    }
    resetSessionTimer();

    const headerTitle = document.querySelector('.header h1');
    if (headerTitle) {
        headerTitle.style.cursor = 'pointer';
        headerTitle.title = 'Go to Dashboard';
        headerTitle.addEventListener('click', function() {
            redirectToDashboard(userRole);
        });
    }
});
