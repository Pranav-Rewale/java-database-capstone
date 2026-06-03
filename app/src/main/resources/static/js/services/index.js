/* index.js - Core service module for managing landing page workflows and role-based login handlers */

// Import dependent handlers and configuration properties
import { openModal } from "../components/modals.js";
import { API_BASE_URL } from "../config/config.js";

// Define strict endpoint properties for administrative and provider endpoints
const ADMIN_API = API_BASE_URL + '/admin';
const DOCTOR_API = API_BASE_URL + '/doctor/login';

/**
 * Orchestrates event configurations upon complete layout resource load.
 * Safely hooks click listeners into role-selection triggers.
 */
window.onload = function () {
    const adminBtn = document.getElementById('adminLogin');
    if (adminBtn) {
        adminBtn.addEventListener('click', () => {
            openModal('adminLogin');
        });
    }

    const doctorBtn = document.getElementById('doctorLogin');
    if (doctorBtn) {
        doctorBtn.addEventListener('click', () => {
            openModal('doctorLogin');
        });
    }
};

/**
 * Asynchronous workflow handler attached globally to process administrative login submissions.
 * Invoked directly from the form submit button actions inside the active modal overlay.
 */
window.adminLoginHandler = async function () {
    const usernameInput = document.getElementById('adminUsername');
    const passwordInput = document.getElementById('adminPassword');

    if (!usernameInput || !passwordInput) {
        alert("Authentication UI fields are missing from the DOM tree.");
        return;
    }

    const username = usernameInput.value.trim();
    const password = passwordInput.value;

    if (!username || !password) {
        alert("Please enter both username and password.");
        return;
    }

    // Step 2: Assemble credential payload structure
    const adminData = { username, password };

    try {
        // Step 3: Dispatch async network post stream
        const response = await fetch(ADMIN_API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(adminData)
        });

        // Step 4 & 5: Evaluate server execution outcomes
        if (response.ok) {
            const data = await response.json();
            
            // Expected parameter target fallback handling
            const token = data.token || data.jwt; 
            if (token) {
                localStorage.setItem("token", token);
                
                // Invoke global helper from render.js to set storage state and refresh layout layers
                if (typeof window.selectRole === "function") {
                    window.selectRole("admin");
                } else {
                    localStorage.setItem("userRole", "admin");
                    window.location.href = "/templates/admin/adminDashboard.html";
                }
            } else {
                alert("Authentication completed, but no valid session token was returned.");
            }
        } else {
            alert("Invalid credentials!");
        }
    } catch (error) {
        // Step 6: Graceful error trapping and logging
        console.error("Critical connection failure intercepted during admin login sequence:", error);
        alert("An error occurred while connecting to the authentication server. Please try again later.");
    }
};

/**
 * Asynchronous workflow handler attached globally to process clinician login submissions.
 * Invoked directly from the form submit button actions inside the active modal overlay.
 */
window.doctorLoginHandler = async function () {
    const emailInput = document.getElementById('doctorEmail');
    const passwordInput = document.getElementById('doctorPassword');

    if (!emailInput || !passwordInput) {
        alert("Doctor authentication UI fields are missing from the DOM tree.");
        return;
    }

    const email = emailInput.value.trim();
    const password = passwordInput.value;

    if (!email || !password) {
        alert("Please enter both email and password.");
        return;
    }

    // Step 2: Assemble credential payload structure
    const doctorData = { email, password };

    try {
        // Step 3: Dispatch async network post stream
        const response = await fetch(DOCTOR_API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(doctorData)
        });

        // Step 4 & 5: Evaluate clinician security token validation
        if (response.ok) {
            const data = await response.json();
            const token = data.token || data.jwt;

            if (token) {
                localStorage.setItem("token", token);
                
                // Invoke global helper to apply state parameters and shift to doctor dashboard views
                if (typeof window.selectRole === "function") {
                    window.selectRole("doctor");
                } else {
                    localStorage.setItem("userRole", "doctor");
                    window.location.href = "/templates/doctor/doctorDashboard.html";
                }
            } else {
                alert("Doctor authorization succeeded, but session data is unreadable.");
            }
        } else {
            alert("Invalid credentials!");
        }
    } catch (error) {
        // Step 6: Graceful network timeout trapping
        console.error("Critical connectivity failure intercepted during doctor login sequence:", error);
        alert("Unable to reach doctor verification systems. Please check server conditions and try again.");
    }
};
