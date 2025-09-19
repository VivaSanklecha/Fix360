document.addEventListener('DOMContentLoaded', function () {
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');

    if (loginForm) {
        loginForm.addEventListener('submit', async function (e) {
            e.preventDefault();
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const errorMessage = document.getElementById('error-message');

            try {
                const response = await fetch('/auth/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, password }),
                });

                if (response.ok) {
                    const data = await response.json();
                    localStorage.setItem('token', data.token);
                    // Redirect to dashboard after successful login
                    window.location.href = '/dashboard.html'; 
                } else {
                    let message = 'Login failed!';
                    try {
                        const errorData = await response.json();
                        message = errorData.message || message;
                    } catch (e) {
                        const text = await response.text();
                        if (text) message = text;
                    }
                    errorMessage.textContent = message;
                    errorMessage.classList.remove('d-none');
                }
            } catch (error) {
                errorMessage.textContent = 'An error occurred. Please try again.';
                errorMessage.classList.remove('d-none');
            }
        });
    }

    if (registerForm) {
        registerForm.addEventListener('submit', async function (e) {
            e.preventDefault();
            const fullName = document.getElementById('fullName').value;
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const flatno = document.getElementById('flatno').value;
            const wing = document.getElementById('wing').value;
            const contactno = document.getElementById('contactno').value;
            const role = document.getElementById('role').value;
            const successMessage = document.getElementById('success-message');
            const errorMessage = document.getElementById('error-message');

            // Clear previous messages
            successMessage.classList.add('d-none');
            errorMessage.classList.add('d-none');

            try {
                const response = await fetch('/auth/register', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        name: fullName,
                        email,
                        password,
                        flatno: parseInt(flatno),
                        wing,
                        contactno: parseInt(contactno),
                        role
                    }),
                });

                if (response.ok) {
                    successMessage.textContent = 'Registration successful! You can now log in.';
                    successMessage.classList.remove('d-none');
                    registerForm.reset();
                } else {
                    let message = 'Registration failed!';
                    try {
                        const errorData = await response.json();
                        message = errorData.message || message;
                    } catch (e) {
                        const text = await response.text();
                        if (text) message = text;
                    }
                    errorMessage.textContent = message;
                    errorMessage.classList.remove('d-none');
                }
            } catch (error) {
                errorMessage.textContent = 'An error occurred. Please try again.';
                errorMessage.classList.remove('d-none');
            }
        });
    }
}); 