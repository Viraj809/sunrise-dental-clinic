/**
 * Sunrise Dental Clinic – Login Page Logic
 * Handles authentication, redirects, password toggle, and remember-me.
 */

(function () {
  /* ── Auto-redirect if already logged in ─────────────────── */
  const token = localStorage.getItem('token');
  const role  = localStorage.getItem('role');

  if (token && role) {
    redirectByRole(role.toUpperCase());
  }

  /* ── Restore remembered email ────────────────────────────── */
  const savedEmail = localStorage.getItem('rememberedEmail');
  if (savedEmail) {
    const emailInput = document.getElementById('email');
    if (emailInput) {
      emailInput.value = savedEmail;
      document.getElementById('rememberMe').checked = true;
    }
  }

  /* ── Form Submit ─────────────────────────────────────────── */
  const form    = document.getElementById('loginForm');
  const errDiv  = document.getElementById('error-msg');
  const errText = document.getElementById('error-text');
  const btn     = document.getElementById('submitBtn');
  const spinner = document.getElementById('btnSpinner');
  const btnLabel = document.getElementById('btnLabel');

  form.addEventListener('submit', async function (e) {
    e.preventDefault();

    const email    = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const remember = document.getElementById('rememberMe').checked;

    /* Clear previous error */
    hideError();

    /* Basic validation */
    if (!email || !password) {
      showError('Please enter both email/username and password.');
      return;
    }

    /* Loading state */
    setLoading(true);

    try {
      const res = await fetch('http://localhost:8080/Backend/resources/auth/login', {
        method : 'POST',
        headers: { 'Content-Type': 'application/json' },
        body   : JSON.stringify({ email, password })
      });

      const data = await res.json();

      if (!res.ok) {
        showError(data.error || 'Invalid email or password. Please try again.');
        setLoading(false);
        return;
      }

      /* Persist auth data */
      localStorage.setItem('token',     data.token);
      localStorage.setItem('role',      data.role);
      localStorage.setItem('name',      data.name);
      localStorage.setItem('staffId',   data.staffId);
      localStorage.setItem('patientId', data.patientId);
      localStorage.setItem('email',     data.email);

      /* Remember me */
      if (remember) {
        localStorage.setItem('rememberedEmail', email);
      } else {
        localStorage.removeItem('rememberedEmail');
      }

      redirectByRole((data.role || '').toUpperCase());

    } catch (err) {
      showError('Cannot connect to server. Please ensure the backend is running.');
      setLoading(false);
    }
  });

  /* ── Password Toggle ─────────────────────────────────────── */
  const pwToggle   = document.getElementById('pwToggle');
  const pwInput    = document.getElementById('password');
  const iconEye    = document.getElementById('iconEye');
  const iconEyeOff = document.getElementById('iconEyeOff');

  if (pwToggle) {
    pwToggle.addEventListener('click', function () {
      const isPassword = pwInput.type === 'password';
      pwInput.type = isPassword ? 'text' : 'password';
      iconEye.style.display    = isPassword ? 'none'  : 'block';
      iconEyeOff.style.display = isPassword ? 'block' : 'none';
      pwToggle.setAttribute('aria-label', isPassword ? 'Hide password' : 'Show password');
    });
  }

  /* ── Helpers ─────────────────────────────────────────────── */
  function showError(message) {
    errText.textContent = message;
    errDiv.classList.add('show');
    errDiv.style.display = 'flex';
  }

  function hideError() {
    errDiv.classList.remove('show');
    errDiv.style.display = 'none';
  }

  function setLoading(loading) {
    btn.disabled = loading;
    spinner.style.display = loading ? 'block' : 'none';
    btnLabel.textContent  = loading ? 'Signing in…' : 'Sign In';
  }

  function redirectByRole(activeRole) {
    if (activeRole === 'SYSTEM_ADMIN' || activeRole === 'ADMIN') {
      window.location.href = '/Frontend/admin-dashboard.html';
    } else if (activeRole === 'RECEPTIONIST') {
      window.location.href = '/Frontend/receptionist-dashboard.html';
    } else if (activeRole === 'DENTIST') {
      window.location.href = '/Frontend/dentist-dashboard.html';
    } else if (activeRole === 'PATIENT') {
      showError('Patient portal is not available. Please contact the clinic.');
      setTimeout(() => window.location.href = '/Frontend/login.html', 2500);
    } else {
      window.location.href = '/Frontend/login.html';
    }
  }
})();
