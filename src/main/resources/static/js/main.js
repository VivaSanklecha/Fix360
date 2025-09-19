document.addEventListener('DOMContentLoaded', function () {
    const token = localStorage.getItem('token');
    if (!token) {
        // If no token and not on a public page, redirect to login
        if (!window.location.pathname.endsWith('login.html') && !window.location.pathname.endsWith('register.html')) {
            window.location.href = '/login.html';
        }
        return;
    }

    // Decode token to get user info
    const decodedToken = JSON.parse(atob(token.split('.')[1]));
    const user = {
        email: decodedToken.sub,
        // roles not included in token; will infer from backend user record
    };

    // Load current user data including ID
    loadCurrentUser();

    // Logout functionality
    const logoutButton = document.getElementById('logout-button');
    if (logoutButton) {
        logoutButton.addEventListener('click', () => {
            localStorage.removeItem('token');
            window.location.href = '/login.html';
        });
    }

    // Page-specific logic
    if (window.location.pathname.endsWith('dashboard.html')) {
        loadDashboard(user);
        const btn = document.getElementById('view-bookings');
        if (btn) btn.addEventListener('click', loadMyBookings);
        const reqBtn = document.getElementById('view-requests');
        if (reqBtn) reqBtn.addEventListener('click', loadProviderRequests);
        // Show provider requests section if user can manage services
        userCanManageServices().then(can => {
            const sec = document.getElementById('provider-requests-section');
            if (sec) sec.style.display = can ? 'block' : 'none';
        });
    } else if (window.location.pathname.endsWith('services.html')) {
        setupProviderTools();
        loadServices();
    }
});

let cachedUserId = null;

async function ensureUserId() {
    if (cachedUserId) {
        return cachedUserId;
    }
    const token = localStorage.getItem('token');
    if (!token) {
        throw new Error('Not authenticated');
    }
    const decoded = JSON.parse(atob(token.split('.')[1]));
    const email = decoded.sub;

    const resp = await fetch('/users/', {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });
    if (!resp.ok) {
        throw new Error('Failed to load user profile');
    }
    const users = await resp.json();
    const me = users.find(u => u.email === email);
    if (!me) {
        throw new Error('Current user not found');
    }
    cachedUserId = me.id;
    return cachedUserId;
}

// Load bookings for services owned by current provider
async function loadProviderRequests() {
    try {
        const res = await fetch('/bookings/provider/requests/mine', {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });
        const tbody = document.getElementById('provider-requests-body');
        const card = document.getElementById('provider-requests-card');
        const empty = document.getElementById('provider-requests-empty');
        if (!res.ok) {
            tbody.innerHTML = '';
            empty.classList.remove('d-none');
            card.classList.remove('d-none');
            return;
        }
        const bookings = await res.json();
        if (!bookings || bookings.length === 0) {
            tbody.innerHTML = '';
            empty.classList.remove('d-none');
            card.classList.remove('d-none');
            return;
        }
        empty.classList.add('d-none');
        card.classList.remove('d-none');
        tbody.innerHTML = bookings.map((b, idx) => `
            <tr>
                <td>${idx + 1}</td>
                <td>${b.users?.name || '-'}</td>
                <td>${b.serviceProvider?.name || '-'}</td>
                <td>${b.bookingDate}</td>
                <td>${b.status}</td>
            </tr>
        `).join('');
    } catch (e) {
        alert('Failed to load service requests');
    }
}

function loadDashboard(user) {
    document.getElementById('welcome-message').textContent = `Welcome, ${user.email}!`;
    
    // Check role and show admin panel if applicable
    // This requires the role to be in the JWT payload
    // const decodedToken = JSON.parse(atob(localStorage.getItem('token').split('.')[1]));
    // if (decodedToken.roles.includes('ROLE_ADMIN')) {
    //     document.getElementById('admin-panel').style.display = 'block';
    // }
}

// Load bookings for current user (uses JWT) and show in dashboard table
async function loadMyBookings() {
    try {
        const res = await fetch('/bookings/me', {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });
        const tbody = document.getElementById('my-bookings-body');
        const card = document.getElementById('my-bookings-card');
        const empty = document.getElementById('my-bookings-empty');
        if (!res.ok) {
            tbody.innerHTML = '';
            empty.classList.remove('d-none');
            card.classList.remove('d-none');
            return;
        }
        const bookings = await res.json();
        if (!bookings || bookings.length === 0) {
            tbody.innerHTML = '';
            empty.classList.remove('d-none');
            card.classList.remove('d-none');
            return;
        }
        empty.classList.add('d-none');
        card.classList.remove('d-none');
        tbody.innerHTML = bookings.map((b, idx) => `
            <tr>
                <td>${idx + 1}</td>
                <td>${b.serviceProvider?.name || '-'}</td>
                <td>${b.bookingDate}</td>
                <td>${b.status}</td>
            </tr>
        `).join('');
    } catch (e) {
        alert('Failed to load bookings');
    }
}

async function loadServices() {
    const servicesList = document.getElementById('services-list');
    try {
        const response = await fetch('/services/paged', {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });
        if (!response.ok) throw new Error('Failed to fetch services');
        
        const page = await response.json();
        const services = page.content;

        if (services.length === 0) {
            servicesList.innerHTML = '<p class="text-center">No services available right now.</p>';
            return;
        }

        const canManage = await userCanManageServices();
        const owned = canManage ? await myOwnedServiceIds() : new Set();
        servicesList.innerHTML = services.map(service => `
            <div class="col-md-4 mb-4">
                <div class="card service-card h-100">
                    <div class="card-body d-flex flex-column">
                        <h5 class="card-title">${service.name}</h5>
                        <p class="card-text text-muted">${service.serviceType}</p>
                        <p class="card-text fw-bold">₹${service.price.toFixed(2)}</p>
                        <div class="mt-auto">
                           <button class="btn btn-primary me-2" onclick="openBookingModal(${service.id}, '${service.name}')">Book Now</button>
                           ${owned.has(service.id) ? `<button class="btn btn-outline-danger" onclick="deleteService(${service.id})">Delete</button>` : ''}
                        </div>
                    </div>
                </div>
            </div>
        `).join('');
    } catch (error) {
        servicesList.innerHTML = `<p class="text-center text-danger">${error.message}</p>`;
    }
}

// Get IDs of services owned by current provider (or all if admin)
async function myOwnedServiceIds() {
    try {
        const res = await fetch('/services/paged?mine=true', { headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }});
        if (!res.ok) return new Set();
        const page = await res.json();
        const set = new Set(page.content.map(s => s.id));
        return set;
    } catch {
        return new Set();
    }
}

function openBookingModal(serviceId, serviceName) {
    document.getElementById('serviceProviderId').value = serviceId;
    document.getElementById('service-name-modal').textContent = serviceName;
    const bookingModal = new bootstrap.Modal(document.getElementById('bookingModal'));
    bookingModal.show();
}

// Event listener for the confirm booking button
document.getElementById('confirm-booking-button')?.addEventListener('click', handleBooking);

async function handleBooking() {
    const serviceProviderId = document.getElementById('serviceProviderId').value;
    const bookingDate = document.getElementById('bookingDate').value;
    const userId = await ensureUserId();

    if (!bookingDate) {
        alert('Please select a date.');
        return;
    }

    try {
        const response = await fetch('/bookings/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify({
                userId,
                serviceProviderId: parseInt(serviceProviderId),
                bookingDate,
                status: 'PENDING'
            })
        });

        if (response.ok) {
            alert('Booking successful!');
            const modal = bootstrap.Modal.getInstance(document.getElementById('bookingModal'));
            modal.hide();
        } else {
            const errorData = await response.json();
            alert(`Booking failed: ${errorData.message || 'Unknown error'}`);
        }
    } catch (error) {
        alert(`An error occurred: ${error.message}`);
    }
}

// Determine if current user can manage services (SERVICE_PROVIDER or ADMIN)
async function userCanManageServices() {
    try {
        const res = await fetch('/users/', { headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }});
        if (!res.ok) return false;
        const users = await res.json();
        const decodedToken = JSON.parse(atob(localStorage.getItem('token').split('.')[1]));
        const current = users.find(u => u.email === decodedToken.sub);
        return current && (current.role === 'SERVICE_PROVIDER' || current.role === 'ADMIN');
    } catch {
        return false;
    }
}

// Show provider tools section based on role
async function setupProviderTools() {
    const canManage = await userCanManageServices();
    const tools = document.getElementById('provider-tools');
    if (!tools) return;
    if (canManage) {
        tools.style.display = 'block';
        document.getElementById('add-service-form').addEventListener('submit', submitNewService);
    } else {
        tools.style.display = 'none';
    }
}

// Submit new service (for service providers/admin)
async function submitNewService(e) {
    e.preventDefault();
    const alertBox = document.getElementById('provider-alert');
    const body = {
        name: document.getElementById('sp-name').value,
        serviceType: document.getElementById('sp-type').value,
        price: parseFloat(document.getElementById('sp-price').value),
        phone: parseInt(document.getElementById('sp-phone').value),
        email: document.getElementById('sp-email').value || null,
        rating: 0,
        status: 'ACTIVE'
    };
    try {
        const res = await fetch('/services/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify(body)
        });
        if (!res.ok) {
            const text = await res.text();
            throw new Error(text || 'Failed to add service');
        }
        alertBox.className = 'alert alert-success';
        alertBox.textContent = 'Service added successfully';
        alertBox.classList.remove('d-none');
        e.target.reset();
        await loadServices();
    } catch (err) {
        alertBox.className = 'alert alert-danger';
        alertBox.textContent = err.message;
        alertBox.classList.remove('d-none');
    }
}

// Delete service (for service providers/admin)
async function deleteService(id) {
    if (!confirm('Are you sure you want to delete this service?')) return;
    try {
        const res = await fetch(`/services/${id}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });
        if (!res.ok && res.status !== 204) {
            const text = await res.text();
            throw new Error(text || 'Failed to delete service');
        }
        await loadServices();
    } catch (err) {
        alert(err.message);
    }
}

// Load current user data and store user ID
async function loadCurrentUser() {
    try {
        const decodedToken = JSON.parse(atob(localStorage.getItem('token').split('.')[1]));
        const userEmail = decodedToken.sub;
        
        // Find user by email to get the ID
        const response = await fetch('/users/', {
            headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });
        
        if (response.ok) {
            const users = await response.json();
            const currentUser = users.find(user => user.email === userEmail);
            if (currentUser) {
                localStorage.setItem('currentUserId', currentUser.id);
                localStorage.setItem('currentUserName', currentUser.name);
            }
        }
    } catch (error) {
        console.error('Failed to load current user:', error);
    }
}

// Ensure userId is available, load if needed
async function ensureUserId() {
    let userId = localStorage.getItem('currentUserId');
    if (!userId) {
        await loadCurrentUser();
        userId = localStorage.getItem('currentUserId');
    }
    return userId;
} 