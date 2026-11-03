window.App = (() => {
    const STORAGE_KEY = 'daws_auth';

    const saveCredentials = (email, password) => {
        const encodedToken = btoa(`${email}:${password}`);
        sessionStorage.setItem(STORAGE_KEY, JSON.stringify({ email, token: encodedToken }));
    };

    const getCredentials = () => {
        const stored = sessionStorage.getItem(STORAGE_KEY);
        if (!stored) {
            return null;
        }
        try {
            return JSON.parse(stored);
        } catch (error) {
            sessionStorage.removeItem(STORAGE_KEY);
            return null;
        }
    };

    const clearCredentials = () => sessionStorage.removeItem(STORAGE_KEY);

    const headersWithAuth = () => {
        const credentials = getCredentials();
        if (!credentials?.token) {
            throw new Error('User not authenticated');
        }
        return {
            'Content-Type': 'application/json',
            'Authorization': `Basic ${credentials.token}`
        };
    };

    const handleResponse = async (response) => {
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'An error occurred');
        }
        if (response.status === 204) {
            return null;
        }
        return response.json();
    };

    const login = async (email, password) => {
        const credentials = btoa(`${email}:${password}`);
        const response = await fetch('/requests', {
            method: 'GET',
            headers: {
                'Authorization': `Basic ${credentials}`
            }
        });
        if (!response.ok) {
            throw new Error('Invalid email or password');
        }
        saveCredentials(email, password);
        return true;
    };

    const isAuthenticated = () => Boolean(getCredentials());

    const fetchRequests = () => fetch('/requests', {
        method: 'GET',
        headers: headersWithAuth()
    }).then(handleResponse);

    const createRequest = (payload) => fetch('/requests', {
        method: 'POST',
        headers: headersWithAuth(),
        body: JSON.stringify(payload)
    }).then(handleResponse);

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

    const escapeHtml = (unsafe) => {
        if (unsafe == null) {
            return '';
        }
        return unsafe
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    };

    return {
        login,
        fetchRequests,
        createRequest,
        isAuthenticated,
        formatDate,
        escapeHtml,
        clearCredentials
    };
})();
