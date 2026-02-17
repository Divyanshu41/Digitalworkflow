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

function formatDate(value) {
    if (!value) {
        return '-';
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }
    return date.toLocaleDateString();
}

function formatDateTime(value) {
    if (!value) {
        return '-';
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return value;
    }
    return date.toLocaleString();
}

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
        actions.push({ label: 'Generate Report', handler: showReportExportForm });
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

function showReportExportForm() {
    const content = document.getElementById('content');
    const today = new Date().toISOString().split('T')[0];
    content.innerHTML = `
        <h3>Generate Request Report</h3>
        <form id="reportForm">
            <div class="form-row">
                <label>From Date</label>
                <input type="date" id="reportFrom">
            </div>
            <div class="form-row">
                <label>To Date</label>
                <input type="date" id="reportTo" value="${today}">
            </div>
            <div class="form-row">
                <label>Status</label>
                <select id="reportStatus">
                    <option value="">All</option>
                    <option value="PENDING">Pending</option>
                    <option value="APPROVED">Approved</option>
                    <option value="REJECTED">Rejected</option>
                </select>
            </div>
            <button type="submit">Download CSV</button>
        </form>
    `;
    document.getElementById('reportForm').addEventListener('submit', downloadReportCsv);
}

async function downloadReportCsv(e) {
    e.preventDefault();
    const params = new URLSearchParams();
    const from = document.getElementById('reportFrom').value;
    const to = document.getElementById('reportTo').value;
    const status = document.getElementById('reportStatus').value;

    if (from) {
        params.append('from', from);
    }
    if (to) {
        params.append('to', to);
    }
    if (status) {
        params.append('status', status);
    }

    try {
        const response = await authorizedFetch(`${API_BASE}/admin/reports/requests/export?${params.toString()}`, {
            method: 'GET'
        });
        const blob = await response.blob();
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = 'approval-report.csv';
        document.body.appendChild(link);
        link.click();
        link.remove();
        setTimeout(() => URL.revokeObjectURL(url), 1000);
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

    let html = `<h3>${title}</h3><table><tr><th>Title</th><th>Status</th><th>Category</th><th>Requested</th><th>Updated</th><th>Attachment</th><th>Actions</th></tr>`;
    pageData.content.forEach(req => {
        const attachmentControls = req.attachmentDownloadUrl
            ? `<button data-action="download" data-id="${req.id}" data-name="${req.attachmentOriginalName || 'attachment'}">Download</button>`
            : '-';
        html += `
            <tr data-request-id="${req.id}">
                <td>${req.title}</td>
                <td>${req.status}</td>
                <td>${req.category}</td>
                <td>${formatDate(req.requestedDate)}</td>
                <td>${formatDateTime(req.updatedAt)}</td>
                <td>${attachmentControls}</td>
                <td><button data-action="details" data-id="${req.id}">View Details</button></td>
            </tr>`;
    });
    html += '</table>';
    content.innerHTML = html;

    content.querySelectorAll('button[data-action="download"]').forEach(btn => {
        btn.addEventListener('click', () => downloadAttachment(btn.dataset.id, btn.dataset.name));
    });

    content.querySelectorAll('button[data-action="details"]').forEach(btn => {
        btn.addEventListener('click', () => viewRequestDetails(btn.dataset.id));
    });

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
        const attachmentButton = req.attachmentDownloadUrl
            ? `<button data-action="download" data-id="${req.id}" data-name="${req.attachmentOriginalName || 'attachment'}">Download Attachment</button>`
            : '';
        html += `
            <div class="card">
                <h4>${req.title}</h4>
                <p><strong>Status:</strong> ${req.status}</p>
                <p><strong>Description:</strong> ${req.description}</p>
                <p><strong>Requested By:</strong> ${req.username}</p>
                <p><strong>Requested Date:</strong> ${formatDate(req.requestedDate)}</p>
                <div class="actions">
                    <button data-action="approve" data-id="${req.id}">Approve</button>
                    <button data-action="reject" data-id="${req.id}">Reject</button>
                    <button data-action="details" data-id="${req.id}">View Details</button>
                    ${attachmentButton}
                </div>
            </div>
        `;
    });
    html += '</div>';
    content.innerHTML = html;

    content.querySelectorAll('.actions button').forEach(btn => {
        btn.addEventListener('click', () => {
            const { action, id, name } = btn.dataset;
            if (action === 'approve' || action === 'reject') {
                handleApprovalAction(id, action);
            } else if (action === 'details') {
                viewRequestDetails(id);
            } else if (action === 'download') {
                downloadAttachment(id, name || 'attachment');
            }
        });
    });

    renderPagination(content, pageData, onPageChange);
}

function renderAdminRequests(title, pageData, onPageChange) {
    const content = document.getElementById('content');
    if (pageData.content.length === 0) {
        content.innerHTML = `<h3>${title}</h3><p>No requests found.</p>`;
        return;
    }

    let html = `<h3>${title}</h3><table><tr><th>ID</th><th>Title</th><th>Status</th><th>Requester</th><th>Approver</th><th>Requested</th><th>Attachment</th><th>Assign Approver</th><th>Actions</th></tr>`;
    pageData.content.forEach(req => {
        html += `
            <tr>
                <td>${req.id}</td>
                <td>${req.title}</td>
                <td>${req.status}</td>
                <td>${req.username}</td>
                <td>${req.approverName || '-'}</td>
                <td>${formatDate(req.requestedDate)}</td>
                <td>${req.attachmentDownloadUrl ? `<button data-action="download" data-id="${req.id}" data-name="${req.attachmentOriginalName || 'attachment'}">Download</button>` : '-'}</td>
                <td>
                    <input type="number" min="1" placeholder="Approver ID" data-request-id="${req.id}" />
                    <button data-action="assign" data-id="${req.id}">Assign</button>
                </td>
                <td>
                    <button data-action="details" data-id="${req.id}">View Details</button>
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

    content.querySelectorAll('button[data-action="download"]').forEach(btn => {
        btn.addEventListener('click', () => downloadAttachment(btn.dataset.id, btn.dataset.name));
    });

    content.querySelectorAll('button[data-action="details"]').forEach(btn => {
        btn.addEventListener('click', () => viewRequestDetails(btn.dataset.id));
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

async function viewRequestDetails(requestId) {
    try {
        const [request, approvals] = await Promise.all([
            authorizedFetchJson(`${API_BASE}/user/requests/${requestId}`),
            authorizedFetchJson(`${API_BASE}/user/requests/${requestId}/approvals`)
        ]);
        renderRequestDetails(request, approvals);
    } catch (err) {
        alert(err.message);
    }
}

function renderRequestDetails(request, approvals) {
    const content = document.getElementById('content');
    let html = `
        <h3>Request Details</h3>
        <div class="card">
            <h4>${request.title}</h4>
            <p><strong>Status:</strong> ${request.status}</p>
            <p><strong>Category:</strong> ${request.category}</p>
            <p><strong>Requested Date:</strong> ${formatDate(request.requestedDate)}</p>
            <p><strong>Submitted By:</strong> ${request.username}</p>
            <p><strong>Assigned Approver:</strong> ${request.approverName || '-'}</p>
            <p><strong>Description:</strong> ${request.description}</p>
    `;

    if (request.attachmentDownloadUrl) {
        html += `
            <button id="detailDownload" data-id="${request.id}" data-name="${request.attachmentOriginalName || 'attachment'}">Download Attachment</button>
        `;
    }

    html += '</div>';

    html += '<h4>Approval History</h4>';
    if (!approvals || approvals.length === 0) {
        html += '<p>No approval actions recorded yet.</p>';
    } else {
        html += '<table><tr><th>Approver</th><th>Status</th><th>Remarks</th><th>Timestamp</th></tr>';
        approvals.forEach(approval => {
            html += `<tr><td>${approval.approverName}</td><td>${approval.status}</td><td>${approval.remarks || '-'}</td><td>${formatDateTime(approval.approvedAt)}</td></tr>`;
        });
        html += '</table>';
    }

    html += '<button id="detailBack">Back</button>';

    content.innerHTML = html;

    const downloadBtn = document.getElementById('detailDownload');
    if (downloadBtn) {
        downloadBtn.addEventListener('click', () => downloadAttachment(downloadBtn.dataset.id, downloadBtn.dataset.name));
    }

    document.getElementById('detailBack').addEventListener('click', showDashboard);
}

async function downloadAttachment(requestId, filename) {
    try {
        const response = await authorizedFetch(`${API_BASE}/user/requests/${requestId}/attachment`, {
            method: 'GET'
        });
        if (response.status === 204) {
            alert('No attachment available for this request.');
            return;
        }
        const blob = await response.blob();
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = filename || 'attachment';
        document.body.appendChild(link);
        link.click();
        link.remove();
        setTimeout(() => URL.revokeObjectURL(url), 1000);
    } catch (err) {
        alert(err.message);
    }
}

function showSubmitRequestForm() {
    const content = document.getElementById('content');
    const today = new Date().toISOString().split('T')[0];
    content.innerHTML = `
        <h3>Submit New Request</h3>
        <form id="requestForm">
            <input type="text" id="title" placeholder="Title" required>
            <textarea id="description" placeholder="Description" required></textarea>
            <input type="text" id="category" placeholder="Category" required>
            <label>Requested Date</label>
            <input type="date" id="requestedDate" value="${today}" required>
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
    const requestedDate = document.getElementById('requestedDate').value;
    const attachment = document.getElementById('attachment').files[0];

    try {
        const formData = new FormData();
        formData.append('title', title);
        formData.append('description', description);
        formData.append('category', category);
        formData.append('requestedDate', requestedDate);
        if (attachment) {
            formData.append('attachment', attachment);
        }
        await authorizedFetch(`${API_BASE}/user/requests`, {
            method: 'POST',
            body: formData
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
