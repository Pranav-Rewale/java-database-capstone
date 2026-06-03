/* patientDashboard.js - Service script for managing patient guest lookups, filters, and authentication */

// Import required layout helpers, rendering components, and API data services
import { createDoctorCard } from './components/doctorCard.js';
import { openModal } from './components/modals.js';
import { getDoctors, filterDoctors } from './services/doctorServices.js';
import { patientSignup, patientLogin } from './services/patientServices.js';

/**
 * Initializes the main guest dashboard interface by setting up event observers
 * and loading baseline data structures once the DOM tree has parsed.
 */
function initPatientDashboard() {
    // 1. Initial Render on Page Load
    loadDoctorCards();

    // 2. Bind Modal Triggers for Login and Signup
    const signupBtn = document.getElementById("patientSignup");
    if (signupBtn) {
        signupBtn.addEventListener("click", () => openModal("patientSignup"));
    }

    const loginBtn = document.getElementById("patientLogin");
    if (loginBtn) {
        loginBtn.addEventListener("click", () => openModal("patientLogin"));
    }

    // 3. Setup Interactive Search and Filter Change Trackers
    const searchBar = document.getElementById("searchBar");
    const filterTime = document.getElementById("filterTime");
    const filterSpecialty = document.getElementById("filterSpecialty");

    if (searchBar) searchBar.addEventListener("input", filterDoctorsOnChange);
    if (filterTime) filterTime.addEventListener("change", filterDoctorsOnChange);
    if (filterSpecialty) filterSpecialty.addEventListener("change", filterDoctorsOnChange);
}

/**
 * Fetches all clinical provider profiles from backend services and populates grids.
 */
async function loadDoctorCards() {
    try {
        const doctors = await getDoctors();
        renderDoctorCards(doctors);
    } catch (error) {
        console.error("Failed to load doctor profiles onto dashboard view:", error);
    }
}

/**
 * Asynchronously gathers filter and text criteria parameters to refine active results.
 */
async function filterDoctorsOnChange() {
    const searchBarElement = document.getElementById("searchBar");
    const filterTimeElement = document.getElementById("filterTime");
    const filterSpecialtyElement = document.getElementById("filterSpecialty");

    // Gather and normalize empty state parameter bounds to explicit null markers
    const name = (searchBarElement && searchBarElement.value.trim().length > 0) ? searchBarElement.value.trim() : null;
    const time = (filterTimeElement && filterTimeElement.value.length > 0) ? filterTimeElement.value : null;
    const specialty = (filterSpecialtyElement && filterSpecialtyElement.value.length > 0) ? filterSpecialtyElement.value : null;

    try {
        // Fetch filtered results from your unified async doctor services layer
        const response = await filterDoctors(name, time, specialty);
        
        // Safety normalization: Treat response directly as an array matching doctorServices layout
        const doctors = Array.isArray(response) ? response : (response.doctors || []);
        
        renderDoctorCards(doctors);
    } catch (error) {
        console.error("Failed to execute search filter operations workflow:", error);
        alert("❌ An error occurred while filtering doctor profiles.");
    }
}

/**
 * Cleanly wipes target container bounds and loops down data sets to mount doctor elements.
 * @param {Array} doctors - Collection payload holding target doctor profiles.
 */
function renderDoctorCards(doctors) {
    const contentDiv = document.getElementById("content");
    if (!contentDiv) return;

    // Clear layout bounds cleanly to avoid duplicate cards on rapid keystrokes
    contentDiv.innerHTML = "";

    if (!doctors || doctors.length === 0) {
        contentDiv.innerHTML = "<p class='noPatientRecord'>No doctors found with the given filters.</p>";
        return;
    }

    doctors.forEach(doctor => {
        const card = createDoctorCard(doctor);
        contentDiv.appendChild(card);
    });
}

/**
 * Globally exposed form submission wrapper to compile details and register new patient profiles.
 */
window.signupPatient = async function () {
    try {
        const nameElement = document.getElementById("name");
        const emailElement = document.getElementById("email");
        const passwordElement = document.getElementById("password");
        const phoneElement = document.getElementById("phone");
        const addressElement = document.getElementById("address");

        if (!nameElement || !emailElement || !passwordElement || !phoneElement || !addressElement) {
            alert("❌ Patient registration failed: Missing required entry fields inside the DOM framework.");
            return;
        }

        const name = nameElement.value.trim();
        const email = emailElement.value.trim();
        const password = passwordElement.value;
        const phone = phoneElement.value.trim();
        const address = addressElement.value.trim();

        if (!name || !email || !password || !phone || !address) {
            alert("Please fill out all input fields inside the signup form before proceeding.");
            return;
        }

        const data = { name, email, password, phone, address };
        
        // Dispatch data structure to core background patient communication services
        const { success, message } = await patientSignup(data);
        
        if (success) {
            alert(message || "Registration completed successfully!");
            
            // Cleanly dismiss the modal system overlay framework
            const modal = document.getElementById("modal");
            if (modal) {
                modal.style.display = "none";
                modal.classList.remove("active");
            }
            
            // Reload window context to update global session layout layers safely
            window.location.reload();
        } else {
            alert(message || "Signup failed. Please double check credentials syntax rules.");
        }
    } catch (error) {
        console.error("Critical fault intercepted inside signupPatient transaction thread:", error);
        alert("❌ An unexpected error occurred while signing up.");
    }
};

/**
 * Globally exposed handler tasked with authenticating patient credentials and extracting token keys.
 */
window.loginPatient = async function () {
    try {
        const emailElement = document.getElementById("email");
        const passwordElement = document.getElementById("password");

        if (!emailElement || !passwordElement) {
            alert("❌ Authentication fields are unmapped inside the layout context.");
            return;
        }

        const email = emailElement.value.trim();
        const password = passwordElement.value;

        if (!email || !password) {
            alert("Please supply both email and password parameters to login.");
            return;
        }

        const data = { email, password };
        
        // Execute background fetch validation network operation
        const response = await patientLogin(data);

        if (response.ok) {
            const result = await response.json();
            
            // Extract the secure key tracking signature safely
            const token = result.token || result.jwt;
            
            if (token) {
                localStorage.setItem('token', token);
                
                // Route setting definitions through the selection matrix to lock view tracking states
                if (typeof window.selectRole === "function") {
                    window.selectRole('loggedPatient');
                } else {
                    localStorage.setItem('userRole', 'loggedPatient');
                }
                
                // Advance user layout directly into their secured dashboard home context
                window.location.href = '/pages/loggedPatientDashboard.html';
            } else {
                alert("❌ Authentication succeeded, but verification session data is missing.");
            }
        } else {
            alert('❌ Invalid credentials!');
        }
    } catch (error) {
        console.error("Critical fault intercepted within loginPatient initialization execution:", error);
        alert("❌ Failed to reach authentication validation systems. Please retry later.");
    }
};

// Expose the rendering tool globally to accommodate adjacent service orchestration demands
window.renderDoctorCards = renderDoctorCards;

// Register initialization observer anchors cleanly matching page lifecycle states
if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initPatientDashboard);
} else {
    initPatientDashboard();
}
