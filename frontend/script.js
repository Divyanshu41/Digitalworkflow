const API_BASE = 'http://localhost:8080/api';
let token = localStorage.getItem('token');
let user = JSON.parse(localStorage.getItem('user'));

const PAGE_SIZE = 5;

const state = {
    userRequestsPage: 0,
    approverRequestsPage: 0,
    adminRequestsPage: 0,
    auditPage: 0
};

document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');

    loginForm.addEventListener('submit', handleLogin);
    registerForm.addEventListener('submit', handleRegister);

    document.getElementById('showRegister').addEventListener('click', showRegister);
    document.getElementById('showLogin').addEventListener('click', showLogin);
    document.getElementById('logoutBtn').addEventListener('click', logout);

    if (token && user) {
        showDashboard();
    } else {
        showAuth();
    }
});

async function handleLogin(e) {
    e.preventDefault();
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value.trim();

    try {
        const response = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        const data = await response.json();
        if (!response.ok) {
            throw new Error(data.message || 'Login failed');
        }
        token = data.token;
        user = { id: data.id, username: data.username, email: data.email, role: data.role };
        localStorage.setItem('token', token);
        localStorage.setItem('user', JSON.stringify(user));
        showDashboard();
    } catch (err) {
        alert(err.message);
    }
}

async function handleRegister(e) {
    e.preventDefault();
    const username = document.getElementById('regUsername').value.trim();
    const email = document.getElementById('regEmail').value.trim();
    const password = document.getElementById('regPassword').value.trim();

    try {
        const response = await fetch(`${API_BASE}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, email, password })
        });
        if (!response.ok) {
            const message = await response.text();
            throw new Error(message || 'Registration failed');
        }
        alert('Registration successful! Please login.');
        showLogin();
    } catch (err) {
        alert(err.message);
    }
}

function showAuth() {
    document.getElementById('auth-section').style.display = 'block';
    document.getElementById('dashboard').style.display = 'none';
}

function showDashboard() {
    document.getElementById('auth-section').style.display = 'none';
    document.getElementById('dashboard').style.display = 'block';
    document.getElementById('userInfo').textContent = `${user.username} (${user.role})`;
    renderNav();
    if (user.role === 'USER') {
        viewMyRequests();
    } else if (user.role === 'APPROVER') {
        viewAssignedRequests();
    } else {
        viewAllRequests();
    }
}

function renderNav() {
    const nav = document.getElementById('navButtons');
    nav.innerHTML = '';

    const actions = [];

    if (user.role === 'USER') {
        actions.push({ label: 'View My Requests', handler: () => viewMyRequests() });
        actions.push({ label: 'Submit Request', handler: showSubmitRequestForm });
    } else if (user.role === 'APPROVER') {
        actions.push({ label: 'Assigned Requests', handler: () => viewAssignedRequests() });
        actions.push({ label: 'My History', handler: () => viewApproverHistory() });
    } else if (user.role === 'ADMIN') {
        actions.push({ label: 'All Requests', handler: () => viewAllRequests() });
        actions.push({ label: 'Manage Users', handler: showUserManagement });
        actions.push({ label: 'Audit Logs', handler: () => viewAuditLogs() });
        actions.push({ label: 'Summary Report', handler: viewSummary });
    }

    actions.forEach(action => {
        const btn = document.createElement('button');
        btn.textContent = action.label;
        btn.addEventListener('click', action.handler);
        nav.appendChild(btn);
    });
}

function showLogin() {
    document.getElementById('login-form').style.display = 'block';
    document.getElementById('register-form').style.display = 'none';
}

function showRegister() {
    document.getElementById('login-form').style.display = 'none';
    document.getElementById('register-form').style.display = 'block';
}

async function viewMyRequests(page = 0) {
    state.userRequestsPage = page;
    try {
        const data = await authorizedFetchJson(`${API_BASE}/user/requests?page=${page}&size=${PAGE_SIZE}`);
        renderRequests('My Requests', data, viewMyRequests);
    } catch (err) {
        alert(err.message);
    }
}

async function viewAssignedRequests(page = 0) {
    state.approverRequestsPage = page;
    try {
        const data = await authorizedFetchJson(`${API_BASE}/approver/requests?page=${page}&size=${PAGE_SIZE}`);
        renderApproverRequests('Assigned Requests', data, viewAssignedRequests);
    } catch (err) {
        alert(err.message);
    }
}

async function viewApproverHistory(page = 0) {
    state.approverRequestsPage = page;
    try {
        const data = await authorizedFetchJson(`${API_BASE}/user/requests?page=${page}&size=${PAGE_SIZE}`);
        renderRequests('My Submitted Requests', data, viewApproverHistory);
    } catch (err) {
        alert(err.message);
    }
}

async function viewAllRequests(page = 0) {
    state.adminRequestsPage = page;
    try {
        const data = await authorizedFetchJson(`${API_BASE}/admin/requests?page=${page}&size=${PAGE_SIZE}`);
        renderAdminRequests('All Requests', data, viewAllRequests);
    } catch (err) {
        alert(err.message);
    }
}

async function viewAuditLogs(page = 0) {
    state.auditPage = page;
    try {
        const data = await authorizedFetchJson(`${API_BASE}/admin/audit?page=${page}&size=${PAGE_SIZE}`);
        renderAuditLog(data, viewAuditLogs);
    } catch (err) {
        alert(err.message);
    }
}

async function showUserManagement() {
    try {
        const users = await authorizedFetchJson(`${API_BASE}/admin/users`);
        renderUserManagement(users);
    } catch (err) {
        alert(err.message);
    }
}

async function viewSummary() {
    try {
        const summary = await authorizedFetchJson(`${API_BASE}/admin/reports/summary`);
        const content = document.getElementById('content');
        content.innerHTML = `
            <h3>Request Summary</h3>
            <ul>
                <li>Total Requests: ${summary.totalRequests}</li>
                <li>Pending: ${summary.pendingRequests}</li>
                <li>Approved: ${summary.approvedRequests}</li>
                <li>Rejected: ${summary.rejectedRequests}</li>
            </ul>
        `;
    } catch (err) {
        alert(err.message);
    }
}

function renderRequests(title, pageData, onPageChange) {
    const content = document.getElementById('content');
    if (pageData.content.length === 0) {
        content.innerHTML = `<h3>${title}</h3><p>No requests found.</p>`;
        return;
    }

    let html = `<h3>${title}</h3><table><tr><th>Title</th><th>Status</th><th>Category</th><th>Updated</th></tr>`;
    pageData.content.forEach(req => {
        html += `<tr><td>${req.title}</td><td>${req.status}</td><td>${req.category}</td><td>${new Date(req.updatedAt).toLocaleString()}</td></tr>`;
    });
    html += '</table>';
    content.innerHTML = html;
    renderPagination(content, pageData, onPageChange);
}

function renderApproverRequests(title, pageData, onPageChange) {
    const content = document.getElementById('content');
    if (pageData.content.length === 0) {
        content.innerHTML = `<h3>${title}</h3><p>No requests assigned.</p>`;
        return;
    }

    let html = `<h3>${title}</h3><div class="cards">`;
    pageData.content.forEach(req => {
        html += `
            <div class="card">
                <h4>${req.title}</h4>
                <p><strong>Status:</strong> ${req.status}</p>
                <p><strong>Description:</strong> ${req.description}</p>
                <p><strong>Requested By:</strong> ${req.username}</p>
                <div class="actions">
                    <button data-action="approve" data-id="${req.id}">Approve</button>
                    <button data-action="reject" data-id="${req.id}">Reject</button>
                </div>
            </div>
        `;
    });
    html += '</div>';
    content.innerHTML = html;

    content.querySelectorAll('.actions button').forEach(btn => {
        btn.addEventListener('click', () => handleApprovalAction(btn.dataset.id, btn.dataset.action));
    });

    renderPagination(content, pageData, onPageChange);
}

function renderAdminRequests(title, pageData, onPageChange) {
    const content = document.getElementById('content');
    if (pageData.content.length === 0) {
        content.innerHTML = `<h3>${title}</h3><p>No requests found.</p>`;
        return;
    }

    let html = `<h3>${title}</h3><table><tr><th>ID</th><th>Title</th><th>Status</th><th>Requester</th><th>Approver</th><th>Assign Approver</th></tr>`;
    pageData.content.forEach(req => {
        html += `
            <tr>
                <td>${req.id}</td>
                <td>${req.title}</td>
                <td>${req.status}</td>
                <td>${req.username}</td>
                <td>${req.approverName || '-'}</td>
                <td>
                    <input type="number" min="1" placeholder="Approver ID" data-request-id="${req.id}" />
                    <button data-action="assign" data-id="${req.id}">Assign</button>
                </td>
            </tr>
        `;
    });
    html += '</table>';
    content.innerHTML = html;

    content.querySelectorAll('button[data-action="assign"]').forEach(btn => {
        btn.addEventListener('click', async () => {
            const requestId = btn.dataset.id;
            const input = content.querySelector(`input[data-request-id="${requestId}"]`);
            const approverId = input.value;
            if (!approverId) {
                alert('Enter approver id');
                return;
            }
            try {
                await authorizedFetch(`${API_BASE}/admin/requests/${requestId}/assign?approverId=${approverId}`, { method: 'POST' });
                alert('Approver assigned');
                viewAllRequests(state.adminRequestsPage);
            } catch (err) {
                alert(err.message);
            }
        });
    });

    renderPagination(content, pageData, onPageChange);
}

function renderAuditLog(pageData, onPageChange) {
    const content = document.getElementById('content');
    if (pageData.content.length === 0) {
        content.innerHTML = '<h3>Audit Logs</h3><p>No audit data.</p>';
        return;
    }

    let html = '<h3>Audit Logs</h3><table><tr><th>ID</th><th>User</th><th>Action</th><th>Details</th><th>Timestamp</th></tr>';
    pageData.content.forEach(log => {
        html += `<tr><td>${log.id}</td><td>${log.actorId || '-'}</td><td>${log.action}</td><td>${log.details}</td><td>${new Date(log.timestamp).toLocaleString()}</td></tr>`;
    });
    html += '</table>';
    content.innerHTML = html;
    renderPagination(content, pageData, onPageChange);
}

function renderUserManagement(users) {
    const content = document.getElementById('content');
    let html = '<h3>User Management</h3><table><tr><th>ID</th><th>Username</th><th>Email</th><th>Role</th><th>Actions</th></tr>';
    users.forEach(u => {
        html += `
            <tr>
                <td>${u.id}</td>
                <td>${u.username}</td>
                <td>${u.email}</td>
                <td>
                    <select data-user-id="${u.id}">
                        <option value="USER" ${u.role === 'USER' ? 'selected' : ''}>User</option>
                        <option value="APPROVER" ${u.role === 'APPROVER' ? 'selected' : ''}>Approver</option>
                        <option value="ADMIN" ${u.role === 'ADMIN' ? 'selected' : ''}>Admin</option>
                    </select>
                </td>
                <td>
                    <button data-action="update" data-id="${u.id}">Update</button>
                    <button data-action="delete" data-id="${u.id}">Delete</button>
                </td>
            </tr>
        `;
    });
    html += '</table>';
    content.innerHTML = html;

    content.querySelectorAll('button[data-action="update"]').forEach(btn => {
        btn.addEventListener('click', async () => {
            const userId = btn.dataset.id;
            const select = content.querySelector(`select[data-user-id="${userId}"]`);
            try {
                await authorizedFetch(`${API_BASE}/admin/users/${userId}/role?role=${select.value}`, { method: 'PUT' });
                alert('Role updated');
                showUserManagement();
            } catch (err) {
                alert(err.message);
            }
        });
    });

    content.querySelectorAll('button[data-action="delete"]').forEach(btn => {
        btn.addEventListener('click', async () => {
            const userId = btn.dataset.id;
            if (!confirm('Delete user?')) {
                return;
            }
            try {
                await authorizedFetch(`${API_BASE}/admin/users/${userId}`, { method: 'DELETE' });
                alert('User deleted');
                showUserManagement();
            } catch (err) {
                alert(err.message);
            }
        });
    });
}

function renderPagination(container, pageData, onPageChange) {
    const controls = document.createElement('div');
    controls.className = 'pagination';

    const prev = document.createElement('button');
    prev.textContent = 'Previous';
    prev.disabled = pageData.number === 0;
    prev.addEventListener('click', () => onPageChange(pageData.number - 1));

    const info = document.createElement('span');
    info.textContent = `Page ${pageData.number + 1} of ${pageData.totalPages || 1}`;

    const next = document.createElement('button');
    next.textContent = 'Next';
    next.disabled = pageData.number + 1 >= pageData.totalPages;
    next.addEventListener('click', () => onPageChange(pageData.number + 1));

    controls.appendChild(prev);
    controls.appendChild(info);
    controls.appendChild(next);

    container.appendChild(controls);
}

function showSubmitRequestForm() {
    const content = document.getElementById('content');
    content.innerHTML = `
        <h3>Submit New Request</h3>
        <form id="requestForm">
            <input type="text" id="title" placeholder="Title" required>
            <textarea id="description" placeholder="Description" required></textarea>
            <input type="text" id="category" placeholder="Category" required>
            <input type="file" id="attachment">
            <button type="submit">Submit</button>
        </form>
    `;
    document.getElementById('requestForm').addEventListener('submit', submitRequest);
}

async function submitRequest(e) {
    e.preventDefault();
    const title = document.getElementById('title').value.trim();
    const description = document.getElementById('description').value.trim();
    const category = document.getElementById('category').value.trim();
    const attachment = document.getElementById('attachment').files[0];

    try {
        await authorizedFetch(`${API_BASE}/user/requests`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ title, description, category, attachment: attachment ? attachment.name : null })
        });
        alert('Request submitted successfully!');
        viewMyRequests(state.userRequestsPage);
    } catch (err) {
        alert(err.message);
    }
}

async function handleApprovalAction(requestId, action) {
    const remarks = prompt(`Enter remarks to ${action} request:`);
    if (remarks === null) {
        return;
    }
    const endpoint = `${API_BASE}/approver/requests/${requestId}/${action}?remarks=${encodeURIComponent(remarks)}`;
    try {
        await authorizedFetch(endpoint, { method: 'POST' });
        alert(`Request ${action}d.`);
        viewAssignedRequests(state.approverRequestsPage);
    } catch (err) {
        alert(err.message);
    }
}

async function authorizedFetch(url, options = {}) {
    if (!token) {
        throw new Error('Not authenticated');
    }
    const headers = options.headers ? { ...options.headers } : {};
    headers['Authorization'] = `Bearer ${token}`;
    options.headers = headers;

    const response = await fetch(url, options);
    if (!response.ok) {
        const message = await response.text();
        throw new Error(message || 'Request failed');
    }
    return response;
}

async function authorizedFetchJson(url, options = {}) {
    const response = await authorizedFetch(url, options);
    return response.json();
}

function logout() {
    token = null;
    user = null;
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    document.getElementById('navButtons').innerHTML = '';
    document.getElementById('content').innerHTML = '';
    showAuth();
}
