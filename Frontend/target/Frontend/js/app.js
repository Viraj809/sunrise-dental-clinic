const API_BASE = 'http://localhost:8080/Backend/resources';
let authToken = localStorage.getItem('token');
let userRole = localStorage.getItem('role');
let userName = localStorage.getItem('name');

function getHeaders() {
    const headers = { 'Content-Type': 'application/json' };
    if (authToken) headers['Authorization'] = 'Bearer ' + authToken;
    return headers;
}

async function apiGet(path) {
    const res = await fetch(API_BASE + path, { headers: getHeaders() });
    if (res.status === 401) { redirectToLogin(); throw new Error('Unauthorized'); }
    return res.json();
}

async function apiPost(path, data) {
    const res = await fetch(API_BASE + path, {
        method: 'POST',
        headers: getHeaders(),
        body: JSON.stringify(data)
    });
    if (res.status === 401) { redirectToLogin(); throw new Error('Unauthorized'); }
    return res.json();
}

function redirectToLogin() {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('name');
    window.location.href = '/Frontend/login.html';
}

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
    const cls = 'status-' + status.toLowerCase();
    return '<span class="status-badge ' + cls + '">' + status + '</span>';
}

function canAccess(role, allowed) {
    return allowed.includes(role);
}

function showNav() {
    const role = localStorage.getItem('role');
    const nav = document.getElementById('main-nav');
    if (!nav) return;
    let html = '<a href="/Frontend/dashboard">Dashboard</a>';
    if (role === 'ADMIN') {
        html += '<a href="/Frontend/api/staff">Manage Staff</a>';
        html += '<a href="/Frontend/api/reports">Reports</a>';
    }
    if (role === 'ADMIN' || role === 'RECEPTIONIST') {
        html += '<a href="/Frontend/appointments?action=register">Register Appointment</a>';
        html += '<a href="/Frontend/appointments">Appointments</a>';
        html += '<a href="/Frontend/patients">Patients</a>';
        html += '<a href="/Frontend/api/bills">Billing</a>';
    }
    if (role === 'DENTIST') {
        html += '<a href="/Frontend/appointments">My Schedule</a>';
    }
    html += '<a href="/Frontend/help">Help</a>';
    html += '<a href="/Frontend/logout">Logout</a>';
    nav.innerHTML = html;
}

document.addEventListener('DOMContentLoaded', function() {
    showNav();
    const userInfo = document.getElementById('user-info');
    if (userInfo && userName) {
        userInfo.textContent = userName + ' (' + (userRole || '') + ')';
    }
});
