
(() => {
    const STORAGE_KEY = 'daws_credentials';

    const path = window.location.pathname;
    const isDashboard = path.endsWith('/dashboard.html');
    const isLogin = path === '/' || path.endsWith('/index.html');
    const isRegister = path.endsWith('/register.html');

    const saveCredentials = (email, password) => {
        sessionStorage.setItem(STORAGE_KEY, JSON.stringify({ email, password }));
    };

    const getCredentials = () => {
        const raw = sessionStorage.getItem(STORAGE_KEY);
        if (!raw) return null;
        try {
            return JSON.parse(raw);
        } catch (_) {
            sessionStorage.removeItem(STORAGE_KEY);
            return null;
        }
    };

    const clearCredentials = () => sessionStorage.removeItem(STORAGE_KEY);

    const authHeader = () => {
        const credentials = getCredentials();
        if (!credentials?.email || !credentials?.password) {
            throw new Error('Please login again');
        }
        return {
            Authorization: `Basic ${btoa(`${credentials.email}:${credentials.password}`)}`
        };
    };

    const parseError = async response => {
        const text = await response.text();
        return text || `Request failed with status ${response.status}`;
    };

    const publicApi = async (url, options = {}) => {
        const headers = {
            ...(options.body ? { 'Content-Type': 'application/json' } : {}),
            ...(options.headers || {})
        };

        const response = await fetch(url, { ...options, headers });
        if (!response.ok) throw new Error(await parseError(response));
        if (response.status === 204) return null;
        return response.json();
    };

    const api = async (url, options = {}) => {
        const headers = {
            ...(options.body ? { 'Content-Type': 'application/json' } : {}),
            ...authHeader(),
            ...(options.headers || {})
        };

        const response = await fetch(url, { ...options, headers });
        if (!response.ok) throw new Error(await parseError(response));
        if (response.status === 204) return null;
        return response.json();
    };

    const escapeHtml = value => {
        if (value == null) return '';
        return String(value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    };

    const formatDate = value => {
        if (!value) return '-';
        const date = new Date(value);
        return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
    };

    const loadCurrentUser = () => api('/auth/me');

    // LOGIN
    const setupLogin = () => {
        const form = document.getElementById('loginForm');
        const registerForm = document.getElementById('registerForm');
        const errorBox = document.getElementById('loginError');
        const registerSuccess = document.getElementById('registerSuccess');
        const registerError = document.getElementById('registerError');

        form?.addEventListener('submit', async event => {
            event.preventDefault();
            errorBox.hidden = true;

            const email = document.getElementById('email').value.trim();
            const password = document.getElementById('password').value;

            try {
                saveCredentials(email, password);
                await loadCurrentUser();
                window.location.href = '/dashboard.html';
            } catch (error) {
                clearCredentials();
                errorBox.textContent = error.message || 'Login failed';
                errorBox.hidden = false;
            }
        });

        registerForm?.addEventListener('submit', async event => {
            event.preventDefault();
            registerSuccess.hidden = true;
            registerError.hidden = true;

            const payload = {
                firstName: document.getElementById('firstName').value.trim(),
                lastName: document.getElementById('lastName').value.trim(),
                email: document.getElementById('registerEmail').value.trim(),
                password: document.getElementById('registerPassword').value
            };

            try {
                const response = await publicApi('/auth/register', {
                    method: 'POST',
                    body: JSON.stringify(payload)
                });

                registerForm.reset();
                registerSuccess.textContent =
                    response?.message || 'Registration successful. Please login.';
                registerSuccess.hidden = false;
            } catch (error) {
                registerError.textContent = error.message || 'Registration failed';
                registerError.hidden = false;
            }
        });
    };

    // REGISTER
    const setupRegisterOnly = () => {
        const form = document.getElementById('registerForm');
        const success = document.getElementById('registerSuccess');
        const errorBox = document.getElementById('registerError');

        form?.addEventListener('submit', async event => {
            event.preventDefault();
            success.hidden = true;
            errorBox.hidden = true;

            const payload = {
                firstName: document.getElementById('firstName').value.trim(),
                lastName: document.getElementById('lastName').value.trim(),
                email: document.getElementById('registerEmail').value.trim(),
                password: document.getElementById('registerPassword').value
            };

            try {
                const response = await publicApi('/auth/register', {
                    method: 'POST',
                    body: JSON.stringify(payload)
                });
                form.reset();
                success.textContent =
                    response?.message || 'Registration successful. Please login.';
                success.hidden = false;
            } catch (error) {
                errorBox.textContent = error.message || 'Registration failed';
                errorBox.hidden = false;
            }
        });
    };

    // APPROVER ACTIONS
    const createActionCell = (request, refresh) => {
        const wrapper = document.createElement('div');
        wrapper.className = 'action-row';

        const remarks = document.createElement('input');
        remarks.type = 'text';
        remarks.placeholder = 'Remarks';
        remarks.className = 'remarks-input';

        const approve = document.createElement('button');
        approve.type = 'button';
        approve.className = 'small';
        approve.textContent = 'Approve';

        const reject = document.createElement('button');
        reject.type = 'button';
        reject.className = 'small danger';
        reject.textContent = 'Reject';

        const decide = async decision => {
            if (decision === 'reject' && !remarks.value.trim()) {
                alert('Please enter a reason before rejecting.');
                remarks.focus();
                return;
            }

            approve.disabled = true;
            reject.disabled = true;

            try {
                await api(`/requests/${request.id}/${decision}`, {
                    method: 'PUT',
                    body: JSON.stringify({
                        remarks: remarks.value.trim() || null
                    })
                });
                await refresh();
            } catch (error) {
                alert(error.message || 'Unable to update request');
                approve.disabled = false;
                reject.disabled = false;
            }
        };

        approve.addEventListener('click', () => decide('approve'));
        reject.addEventListener('click', () => decide('reject'));

        wrapper.append(remarks, approve, reject);
        return wrapper;
    };

    // DASHBOARD
    const setupDashboard = async () => {
        if (!getCredentials()) {
            window.location.href = '/index.html';
            return;
        }

        const $ = id => document.getElementById(id);

        const userInfo = $('userInfo');
        const logoutBtn = $('logoutBtn');
        const createSection = $('createSection');
        const createForm = $('createForm');
        const createSuccess = $('createSuccess');
        const createError = $('createError');
        const requestsBody = $('requestsBody');
        const tableTitle = $('tableTitle');
        const actionHeader = $('actionHeader');

        const searchInput = $('searchInput');
        const statusFilter = $('statusFilter');
        const pagination = $('pagination');
        const resultCount = $('resultCount');
        const paginationInfo = $('paginationInfo');

        const PAGE_SIZE = 5;
        let currentPage = 1;
        let allRequests = [];
        let currentUser;
        let role = '';

        logoutBtn.addEventListener('click', () => {
            clearCredentials();
            window.location.href = '/index.html';
        });

        // Mobile sidebar
        const sidebar = $('sidebar');
        const overlay = $('sidebarOverlay');
        const menuToggle = $('menuToggle');

        const closeSidebar = () => {
            sidebar.classList.remove('open');
            overlay.classList.remove('visible');
        };

        menuToggle.addEventListener('click', () => {
            sidebar.classList.toggle('open');
            overlay.classList.toggle('visible');
        });

        overlay.addEventListener('click', closeSidebar);

        // Sidebar navigation
        document.querySelectorAll('.nav-link').forEach(link => {
            link.addEventListener('click', event => {
                event.preventDefault();

                document.querySelectorAll('.nav-link').forEach(item => {
                    item.classList.remove('active');
                });
                link.classList.add('active');

                const section = link.dataset.section;
                if (section === 'create') {
                    createSection.scrollIntoView({ behavior: 'smooth' });
                } else if (section === 'requests') {
                    $('requests').scrollIntoView({ behavior: 'smooth' });
                } else if (section === 'dashboard') {
                    $('dashboard').scrollIntoView({ behavior: 'smooth' });
                } else if (link.id === 'profileNav') {
                    alert(`Logged in as: ${currentUser.email}\nRole: ${role}`);
                }

                closeSidebar();
            });
        });

        try {
            currentUser = await loadCurrentUser();
        } catch (error) {
            clearCredentials();
            window.location.href = '/index.html';
            return;
        }

        role = String(currentUser.role || '').toUpperCase();

        userInfo.textContent = `${currentUser.email} (${role})`;
        $('sidebarEmail').textContent = currentUser.email;
        $('sidebarRole').textContent = role;
        $('userAvatar').textContent =
            (currentUser.email || 'U').charAt(0).toUpperCase();

        // Role-specific dashboard
        if (role === 'USER') {
            tableTitle.textContent = 'My Requests';
            createSection.hidden = false;
            $('createNav').hidden = false;
            $('actionHeader').hidden = true;
            document.querySelectorAll('.user-id-column').forEach(el => {
                el.hidden = true;
            });
        } else if (role === 'APPROVER') {
            tableTitle.textContent = 'All Requests & Approval History';
            actionHeader.hidden = false;
        } else if (role === 'ADMIN') {
            tableTitle.textContent = 'All Requests';
            actionHeader.hidden = true;
        } else {
            requestsBody.innerHTML =
                '<tr><td colspan="8" class="empty-cell">Unknown role.</td></tr>';
            return;
        }

        const getStatus = request =>
            String(request.status || '').toUpperCase();

        const updateStats = requests => {
            const total = requests.length;
            const pending = requests.filter(r => getStatus(r) === 'PENDING').length;
            const approved = requests.filter(r => getStatus(r) === 'APPROVED').length;
            const rejected = requests.filter(r => getStatus(r) === 'REJECTED').length;

            $('totalCount').textContent = total;
            $('pendingCount').textContent = pending;
            $('approvedCount').textContent = approved;
            $('rejectedCount').textContent = rejected;
        };

        const statusBadge = status => {
            const value = String(status || 'UNKNOWN').toUpperCase();
            let css = 'status-unknown';

            if (value === 'PENDING') css = 'status-pending';
            if (value === 'APPROVED') css = 'status-approved';
            if (value === 'REJECTED') css = 'status-rejected';

            return `<span class="status-badge ${css}">${escapeHtml(value)}</span>`;
        };

        const getFilteredRequests = () => {
            const search = searchInput.value.trim().toLowerCase();
            const selectedStatus = statusFilter.value;

            return allRequests.filter(request => {
                const status = getStatus(request);

                const matchesStatus =
                    selectedStatus === 'ALL' || status === selectedStatus;

                const searchable = [
                    request.id,
                    request.title,
                    request.description,
                    request.status,
                    request.remarks,
                    request.userId
                ].join(' ').toLowerCase();

                return matchesStatus && searchable.includes(search);
            });
        };

        const renderPagination = (totalPages, totalItems) => {
            pagination.innerHTML = '';

            const start = totalItems === 0 ? 0 : (currentPage - 1) * PAGE_SIZE + 1;
            const end = Math.min(currentPage * PAGE_SIZE, totalItems);

            paginationInfo.textContent =
                `Showing ${start}-${end} of ${totalItems} records`;

            const previous = document.createElement('button');
            previous.className = 'page-button';
            previous.textContent = '‹';
            previous.disabled = currentPage <= 1;
            previous.addEventListener('click', () => {
                currentPage--;
                renderTable();
            });
            pagination.appendChild(previous);

            for (let page = 1; page <= totalPages; page++) {
                const button = document.createElement('button');
                button.className = 'page-button' +
                    (page === currentPage ? ' active' : '');
                button.textContent = page;

                button.addEventListener('click', () => {
                    currentPage = page;
                    renderTable();
                });
                pagination.appendChild(button);
            }

            const next = document.createElement('button');
            next.className = 'page-button';
            next.textContent = '›';
            next.disabled = currentPage >= totalPages;
            next.addEventListener('click', () => {
                currentPage++;
                renderTable();
            });
            pagination.appendChild(next);
        };

        const renderTable = () => {
            const filtered = getFilteredRequests();
            const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));

            currentPage = Math.min(currentPage, totalPages);

            const startIndex = (currentPage - 1) * PAGE_SIZE;
            const pageItems = filtered.slice(startIndex, startIndex + PAGE_SIZE);

            resultCount.textContent = `${filtered.length} records`;

            if (filtered.length === 0) {
                requestsBody.innerHTML =
                    '<tr><td colspan="8" class="empty-cell">No matching requests found.</td></tr>';
                renderPagination(1, 0);
                return;
            }

            requestsBody.innerHTML = '';

            for (const request of pageItems) {
                const tr = document.createElement('tr');

                tr.innerHTML = `
                    <td>#${escapeHtml(request.id)}</td>
                    <td><strong>${escapeHtml(request.title)}</strong></td>
                    <td>${escapeHtml(request.description || '-')}</td>
                    <td>${statusBadge(request.status)}</td>
                    <td>${escapeHtml(formatDate(request.submittedAt))}</td>
                    <td class="user-id-column">${escapeHtml(request.userId)}</td>
                    <td>${escapeHtml(request.remarks || '-')}</td>
                    <td class="action-cell"></td>
                `;

                if (role === 'APPROVER') {
                    const actionCell = tr.querySelector('.action-cell');

                    if (getStatus(request) === 'PENDING') {
                        actionCell.appendChild(createActionCell(request, loadRequests));
                    } else {
                        actionCell.textContent = 'Decision recorded';
                    }
                }

                if (role === 'USER') {
                    tr.querySelector('.user-id-column').hidden = true;
                }

                requestsBody.appendChild(tr);
            }

            renderPagination(totalPages, filtered.length);
        };

        // Load role-specific requests
        const loadRequests = async () => {
            requestsBody.innerHTML =
                '<tr><td colspan="8" class="empty-cell">Loading requests...</td></tr>';

            try {
                let requests;

                requests = await api('/requests');

                if (!Array.isArray(requests)) {
                    throw new Error('Unexpected response from server');
                }

                // Existing API currently returns all requests.
                // This client-side filter is for display only, not security.
                if (role === 'USER') {
                    requests = requests.filter(
                        item => Number(item.userId) === Number(currentUser.id)
                    );
                }

                allRequests = requests;
                currentPage = 1;

                updateStats(allRequests);
                renderTable();

            } catch (error) {
                requestsBody.innerHTML =
                    `<tr><td colspan="8" class="empty-cell">${escapeHtml(error.message || 'Unable to load data')}</td></tr>`;
                updateStats([]);
            }
        };

        searchInput.addEventListener('input', () => {
            currentPage = 1;
            renderTable();
        });

        statusFilter.addEventListener('change', () => {
            currentPage = 1;
            renderTable();
        });

        $('refreshBtn').addEventListener('click', loadRequests);

        // Create request
        createForm.addEventListener('submit', async event => {
            event.preventDefault();
            createSuccess.hidden = true;
            createError.hidden = true;

            const payload = {
                title: $('title').value.trim(),
                description: $('description').value.trim(),
                userId: currentUser.id
            };

            if (!payload.title) {
                createError.textContent = 'Please enter a request title.';
                createError.hidden = false;
                return;
            }

            try {
                await api('/requests', {
                    method: 'POST',
                    body: JSON.stringify(payload)
                });

                createForm.reset();
                createSuccess.hidden = false;
                await loadRequests();

            } catch (error) {
                createError.textContent =
                    error.message || 'Unable to create request';
                createError.hidden = false;
            }
        });

        await loadRequests();
    };

    document.addEventListener('DOMContentLoaded', async () => {
        if (isLogin) {
            setupLogin();
            return;
        }

        if (isRegister) {
            setupRegisterOnly();
            return;
        }

        if (isDashboard) {
            await setupDashboard();
        }
    });
})();
