/* header.js - Component file for rendering role-based application navigation headers */

/**
 * Main orchestration function responsible for painting the global navigation header 
 * based on active user security context rules stored in local state keys.
 */
function renderHeader() {
    const headerDiv = document.getElementById("header");
    if (!headerDiv) return; // Prevent script errors if landing container is not on the page

    const currentPath = window.location.pathname;

    // 1. Check if the Current Page is the Root Homepage
    if (currentPath === "/" || currentPath.endsWith("/") || currentPath.endsWith("index.html")) {
        localStorage.removeItem("userRole");
        localStorage.removeItem("token");
        
        headerDiv.innerHTML = `
            <header class="header">
                <div class="logo-section">
                    <img src="../assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
                    <span class="logo-title">Hospital CMS</span>
                </div>
            </header>`;
        return;
    }

    // 2. Retrieve User Session Credentials
    const role = localStorage.getItem("userRole");
    const token = localStorage.getItem("token");

    // 3. Intercept & Terminate Invalid Sessions / Empty Tokens
    if ((role === "loggedPatient" || role === "admin" || role === "doctor") && !token) {
        localStorage.removeItem("userRole");
        localStorage.removeItem("token");
        alert("Session expired or invalid login. Please log in again.");
        window.location.href = "/";
        return;
    }

    // 4. Initialize Baseline Frame Template Structure
    let headerContent = `
        <header class="header">
            <div class="logo-section" style="cursor: pointer;" onclick="window.location.href='/'">
                <img src="../assets/images/logo/logo.png" alt="Hospital CRM Logo" class="logo-img">
                <span class="logo-title">Hospital CMS</span>
            </div>
            <nav class="nav-controls">`;

    // 5. Append Component Form Actions Tailored to Current User Roles
    if (role === "admin") {
        headerContent += `
            <button id="addDocBtn" class="adminBtn">Add Doctor</button>
            <a href="#" id="headerLogoutLink" class="logout-link">Logout</a>`;
    } 
    else if (role === "doctor") {
        headerContent += `
            <button id="doctorHomeBtn" class="adminBtn">Home</button>
            <a href="#" id="headerLogoutLink" class="logout-link">Logout</a>`;
    } 
    else if (role === "patient") {
        headerContent += `
            <button id="patientLogin" class="adminBtn">Login</button>
            <button id="patientSignup" class="adminBtn">Sign Up</button>`;
    } 
    else if (role === "loggedPatient") {
        headerContent += `
            <button id="home" class="adminBtn">Home</button>
            <button id="patientAppointments" class="adminBtn">Appointments</button>
            <a href="#" id="patientLogoutLink" class="logout-link">Logout</a>`;
    } 
    else {
        // Fallback layout config state for generic unauthenticated view sessions
        headerContent += `
            <button id="backToRolesBtn" class="adminBtn" onclick="window.location.href='/'">Select Role</button>`;
    }

    // Close interior element block tags
    headerContent += `
            </nav>
        </header>`;

    // 6. Paint Final Combined Stream Content to DOM Container Target
    headerDiv.innerHTML = headerContent;

    // 7. Bind Interactive Dynamic Element Observers Safely
    attachHeaderButtonListeners();
}

/**
 * Safely looks up newly appended DOM controls by ID references and maps their execution scopes.
 */
function attachHeaderButtonListeners() {
    // Shared System Sign-out link bindings
    const logoutLink = document.getElementById("headerLogoutLink");
    if (logoutLink) {
        logoutLink.addEventListener("click", function(e) {
            e.preventDefault();
            logout();
        });
    }

    // Admin Add-Doctor Action modal bindings
    const addDocBtn = document.getElementById("addDocBtn");
    if (addDocBtn) {
        addDocBtn.addEventListener("click", function() {
            if (typeof openModal === "function") {
                openModal("addDoctor");
            } else {
                console.error("openModal framework handler is unavailable in this environment scope.");
            }
        });
    }

    // Doctor Dashboard Navigation shortcut triggers
    const doctorHomeBtn = document.getElementById("doctorHomeBtn");
    if (doctorHomeBtn) {
        doctorHomeBtn.addEventListener("click", function() {
            if (typeof selectRole === "function") {
                selectRole("doctor");
            } else {
                window.location.href = "/templates/doctor/doctorDashboard.html";
            }
        });
    }

    // Base Patient Authentication workflow redirects
    const patientLoginBtn = document.getElementById("patientLogin");
    if (patientLoginBtn) {
        patientLoginBtn.addEventListener("click", function() {
            if (typeof openModal === "function") openModal("patientLogin");
        });
    }

    const patientSignupBtn = document.getElementById("patientSignup");
    if (patientSignupBtn) {
        patientSignupBtn.addEventListener("click", function() {
            if (typeof openModal === "function") openModal("patientSignup");
        });
    }

    // Authenticated Patient Navigation workflows
    const homeBtn = document.getElementById("home");
    if (homeBtn) {
        homeBtn.addEventListener("click", function() {
            window.location.href = "/pages/loggedPatientDashboard.html";
        });
    }

    const appointmentsBtn = document.getElementById("patientAppointments");
    if (appointmentsBtn) {
        appointmentsBtn.addEventListener("click", function() {
            window.location.href = "/pages/patientAppointments.html";
        });
    }

    const patientLogoutLink = document.getElementById("patientLogoutLink");
    if (patientLogoutLink) {
        patientLogoutLink.addEventListener("click", function(e) {
            e.preventDefault();
            logoutPatient();
        });
    }
}

/**
 * Standard System Signout Action routine. Clears session store records entirely 
 * and routes execution workflows back to the landing view root.
 */
function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("userRole");
    window.location.href = "/";
}

/**
 * Specialized Patient Account signout wrapper routine. Revokes token keys, 
 * down-grades context to public view settings, and updates directory indices.
 */
function logoutPatient() {
    localStorage.removeItem("token");
    localStorage.setItem("userRole", "patient"); // Return to standard patient role to reveal registration links
    window.location.href = "/pages/patientDashboard.html";
}

// 8. Auto-initialize rendering immediately when the component script loads
document.addEventListener("DOMContentLoaded", renderHeader);