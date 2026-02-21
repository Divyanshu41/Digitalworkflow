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
        if (!raw) {
            return null;
        }
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
        const token = btoa(`${credentials.email}:${credentials.password}`);
        return { Authorization: `Basic ${token}` };
    };

    const parseError = async (response) => {
        const text = await response.text();
        return text || `Request failed with status ${response.status}`;
    };

    const publicApi = async (url, options = {}) => {
        const headers = {
            ...(options.body ? { 'Content-Type': 'application/json' } : {}),
            ...(options.headers || {})
        };

        const response = await fetch(url, { ...options, headers });
        if (!response.ok) {
            throw new Error(await parseError(response));
        }
        if (response.status === 204) {
            return null;
        }
        return response.json();
    };

    const api = async (url, options = {}) => {
        const headers = {
            ...(options.body ? { 'Content-Type': 'application/json' } : {}),
            ...authHeader(),
            ...(options.headers || {})
        };

        const response = await fetch(url, { ...options, headers });
        if (!response.ok) {
            throw new Error(await parseError(response));
        }
        if (response.status === 204) {
            return null;
        }
        return response.json();
    };

    const escapeHtml = (value) => {
        if (value == null) {
            return '';
        }
        return String(value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    };

    const formatDate = (value) => {
        if (!value) {
            return '-';
        }
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) {
            return value;
        }
        return date.toLocaleString();
    };

    const loadCurrentUser = () => api('/auth/me');

    const setupLogin = () => {
        const form = document.getElementById('loginForm');
        const registerForm = document.getElementById('registerForm');
        const errorBox = document.getElementById('loginError');
        const registerSuccess = document.getElementById('registerSuccess');
        const registerError = document.getElementById('registerError');
        form.addEventListener('submit', async (event) => {
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

        registerForm.addEventListener('submit', async (event) => {
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
                registerSuccess.textContent = response?.message || 'Registration successful. Please login.';
                registerSuccess.hidden = false;
            } catch (error) {
                registerError.textContent = error.message || 'Registration failed';
                registerError.hidden = false;
            }
        });
    };

    const setupRegisterOnly = () => {
        const registerForm = document.getElementById('registerForm');
        const registerSuccess = document.getElementById('registerSuccess');
        const registerError = document.getElementById('registerError');

        registerForm?.addEventListener('submit', async (event) => {
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
                registerSuccess.textContent = response?.message || 'Registration successful. Please login.';
                registerSuccess.hidden = false;
            } catch (error) {
                registerError.textContent = error.message || 'Registration failed';
                registerError.hidden = false;
            }
        });
    };

    const createActionCell = (request, refresh) => {
        const wrapper = document.createElement('div');
        wrapper.className = 'action-row';

        const remarksInput = document.createElement('input');
        remarksInput.type = 'text';
        remarksInput.placeholder = 'Remarks';
        remarksInput.className = 'remarks-input';

        const approveBtn = document.createElement('button');
        approveBtn.type = 'button';
        approveBtn.className = 'small';
        approveBtn.textContent = 'Approve';

        const rejectBtn = document.createElement('button');
        rejectBtn.type = 'button';
        rejectBtn.className = 'small danger';
        rejectBtn.textContent = 'Reject';

        const runDecision = async (decision) => {
            const endpoint = `/requests/${request.id}/${decision}`;
            await api(endpoint, {
                method: 'PUT',
                body: JSON.stringify({ remarks: remarksInput.value.trim() || null })
            });
            await refresh();
        };

        approveBtn.addEventListener('click', async () => {
            try {
                await runDecision('approve');
            } catch (error) {
                alert(error.message || 'Approve failed');
            }
        });

        rejectBtn.addEventListener('click', async () => {
            try {
                await runDecision('reject');
            } catch (error) {
                alert(error.message || 'Reject failed');
            }
        });

        wrapper.appendChild(remarksInput);
        wrapper.appendChild(approveBtn);
        wrapper.appendChild(rejectBtn);
        return wrapper;
    };

    const setupDashboard = async () => {
        const credentials = getCredentials();
        if (!credentials) {
            window.location.href = '/index.html';
            return;
        }

        const userInfo = document.getElementById('userInfo');
        const logoutBtn = document.getElementById('logoutBtn');
        const createSection = document.getElementById('createSection');
        const createForm = document.getElementById('createForm');
        const createSuccess = document.getElementById('createSuccess');
        const createError = document.getElementById('createError');
        const requestsBody = document.getElementById('requestsBody');
        const tableTitle = document.getElementById('tableTitle');
        const actionHeader = document.getElementById('actionHeader');

        logoutBtn.addEventListener('click', () => {
            clearCredentials();
            window.location.href = '/index.html';
        });

        let currentUser;
        try {
            currentUser = await loadCurrentUser();
        } catch (error) {
            clearCredentials();
            window.location.href = '/index.html';
            return;
        }

        const role = String(currentUser.role || '').toUpperCase();
        userInfo.textContent = `${currentUser.email} (${role})`;

        const loadRequests = async () => {
            try {
                let requests = [];

                if (role === 'USER') {
                    tableTitle.textContent = 'My Requests';
                    createSection.hidden = false;
                    requests = await api('/requests');
                    requests = requests.filter((item) => item.userId === currentUser.id);
                } else if (role === 'APPROVER') {
                    tableTitle.textContent = 'Pending Requests';
                    actionHeader.hidden = false;
                    requests = await api('/requests/pending');
                } else if (role === 'ADMIN') {
                    tableTitle.textContent = 'All Requests';
                    requests = await api('/requests');
                } else {
                    requestsBody.innerHTML = '<tr><td colspan="8">Unknown role.</td></tr>';
                    return;
                }

                if (!Array.isArray(requests) || requests.length === 0) {
                    requestsBody.innerHTML = '<tr><td colspan="8">No requests found.</td></tr>';
                    return;
                }

                requestsBody.innerHTML = '';
                for (const request of requests) {
                    const tr = document.createElement('tr');
                    tr.innerHTML = `
                        <td>${escapeHtml(request.id)}</td>
                        <td>${escapeHtml(request.title)}</td>
                        <td>${escapeHtml(request.description || '-')}</td>
                        <td>${escapeHtml(request.status)}</td>
                        <td>${escapeHtml(formatDate(request.submittedAt))}</td>
                        <td>${escapeHtml(request.userId)}</td>
                        <td>${escapeHtml(request.remarks || '-')}</td>
                        <td class="action-cell"></td>
                    `;

                    if (role === 'APPROVER') {
                        const actionCell = tr.querySelector('.action-cell');
                        actionCell.appendChild(createActionCell(request, loadRequests));
                    }

                    requestsBody.appendChild(tr);
                }
            } catch (error) {
                requestsBody.innerHTML = `<tr><td colspan="8">${escapeHtml(error.message || 'Unable to load data')}</td></tr>`;
            }
        };

        createForm.addEventListener('submit', async (event) => {
            event.preventDefault();
            createSuccess.hidden = true;
            createError.hidden = true;

            const payload = {
                title: document.getElementById('title').value.trim(),
                description: document.getElementById('description').value.trim(),
                userId: currentUser.id
            };

            try {
                await api('/requests', {
                    method: 'POST',
                    body: JSON.stringify(payload)
                });
                createForm.reset();
                createSuccess.hidden = false;
                await loadRequests();
            } catch (error) {
                createError.textContent = error.message || 'Unable to create request';
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
