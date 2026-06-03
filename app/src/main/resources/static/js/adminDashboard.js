/* adminDashboard.js - Service script managing administrative doctor interactions and data binding */

// Import required modal, creation components, and services
import { openModal } from "./components/modals.js";
import { createDoctorCard } from "./components/doctorCard.js";
import { getDoctors, filterDoctors, saveDoctor } from "./services/doctorServices.js";

/**
 * Initializes the administrative control panel by setting up event observers 
 * and executing baseline data extraction once the DOM context is ready.
 */
function initAdminDashboard() {
    // 1. Hook the 'Add Doctor' structural navigation controller button trigger
    const addDocBtn = document.getElementById('addDocBtn');
    if (addDocBtn) {
        addDocBtn.addEventListener('click', () => {
            openModal('addDoctor');
        });
    }

    // 2. Attach instant real-time event lookup watchers to the filter components
    const searchBar = document.getElementById("searchBar");
    const timeFilter = document.getElementById("timeFilter"); // Targets id="timeFilter" inside template
    const specialtyFilter = document.getElementById("specialtyFilter"); // Targets id="specialtyFilter" inside template

    if (searchBar) searchBar.addEventListener("input", filterDoctorsOnChange);
    if (timeFilter) timeFilter.addEventListener("change", filterDoctorsOnChange);
    if (specialtyFilter) specialtyFilter.addEventListener("change", filterDoctorsOnChange);

    // 3. Mount baseline data into active viewport tracking layout trees
    loadDoctorCards();
}

/**
 * Orchestrates backend inquiries to fetch all clinical providers and present them.
 */
async function loadDoctorCards() {
    try {
        const doctors = await getDoctors();
        renderDoctorCards(doctors);
    } catch (error) {
        console.error("Critical error encountered within loadDoctorCards() loop execution:", error);
    }
}

/**
 * Asynchronously evaluates active user input fields to coordinate database search filters.
 */
async function filterDoctorsOnChange() {
    const searchBar = document.getElementById("searchBar");
    const timeFilter = document.getElementById("timeFilter");
    const specialtyFilter = document.getElementById("specialtyFilter");

    // Read values, trimming text spaces, and standardizing empty parameters to null
    const nameValue = searchBar ? searchBar.value.trim() : null;
    const timeValue = (timeFilter && timeFilter.value !== "") ? timeFilter.value : null;
    const specialtyValue = (specialtyFilter && specialtyFilter.value !== "") ? specialtyFilter.value : null;

    try {
        // Dispatch filtered search criteria across background query parameters
        const matchingDoctors = await filterDoctors(nameValue, timeValue, specialtyValue);
        
        const contentDiv = document.getElementById("content");
        if (!contentDiv) return;

        if (matchingDoctors && matchingDoctors.length > 0) {
            renderDoctorCards(matchingDoctors);
        } else {
            // Display empty state layout text notification context safely if no fields correspond
            contentDiv.innerHTML = `<div class="noPatientRecord">No doctors found with the given filters.</div>`;
        }
    } catch (error) {
        console.error("Query sequence evaluation exception intercepted:", error);
        alert("An error occurred while attempting to filter doctor profile layouts.");
    }
}

/**
 * Modular layout helper method dedicated to populating and cleaning the dashboard panel grids.
 * @param {Array} doctors - Collection payload holding target doctor profiles.
 */
function renderDoctorCards(doctors) {
    const contentDiv = document.getElementById("content");
    if (!contentDiv) return;

    // Clear structural container layout bounds cleanly preventing duplicate row paint cards
    contentDiv.innerHTML = "";

    if (!doctors || doctors.length === 0) {
        contentDiv.innerHTML = `<div class="noPatientRecord">No doctor profile records are currently registered.</div>`;
        return;
    }

    // Traverse result arrays to construct and mount independent HTML element cards
    doctors.forEach(doctor => {
        const doctorCardElement = createDoctorCard(doctor);
        contentDiv.appendChild(doctorCardElement);
    });
}

/**
 * Global operational handler responsible for packaging form metadata payloads 
 * and executing remote persistence updates for new clinical providers.
 */
window.adminAddDoctor = async function () {
    // Locate and extract raw input fields context parameters safely out of the modal form markup
    const nameInp = document.getElementById("docName");
    const specialtyInp = document.getElementById("docSpecialty");
    const emailInp = document.getElementById("docEmail");
    const passwordInp = document.getElementById("docPassword");
    const phoneInp = document.getElementById("docPhone");

    if (!nameInp || !specialtyInp || !emailInp || !passwordInp || !phoneInp) {
        alert("Registration failed: Form input fields are missing from the current active modal DOM frame.");
        return;
    }

    // Capture checked values across schedule blocks (e.g. checkbox selections for available shifts)
    const activeCheckedSlots = [];
    const checkboxes = document.querySelectorAll("#modal-body input[type='checkbox']:checked");
    checkboxes.forEach(box => {
        activeCheckedSlots.push(box.value);
    });

    // Fallback: If no checkbox array is explicitly matched, parse generic fallback text rules
    if (activeCheckedSlots.length === 0) {
        const timeInputText = document.getElementById("docAvailability");
        if (timeInputText && timeInputText.value.trim() !== "") {
            activeCheckedSlots.push(timeInputText.value.trim());
        }
    }

    // Verify system validation fields before packing payload arrays
    if (!nameInp.value.trim() || !specialtyInp.value.trim() || !emailInp.value.trim() || !passwordInp.value || !phoneInp.value.trim()) {
        alert("Please complete all mandatory field entries inside the registration form.");
        return;
    }

    // Retrieve active administration token authentication key context parameters
    const token = localStorage.getItem("token");
    if (!token) {
        alert("Administrative authorization expired: Security credentials token not found. Please log in again.");
        return;
    }

    // Structure the comprehensive data object matching your backend Entity definitions
    const newDoctorObject = {
        name: nameInp.value.trim(),
        specialty: specialtyInp.value.trim(),
        email: emailInp.value.trim(),
        password: passwordInp.value,
        phone: phoneInp.value.trim(),
        availableTimes: activeCheckedSlots
    };

    try {
        // Trigger server-side background network stream mapping transaction
        const result = await saveDoctor(newDoctorObject, token);

        if (result.success) {
            alert(result.message || "Doctor profile saved and registered successfully.");
            
            // Cleanly dismiss the modal system popups overlay
            const modalElement = document.getElementById("modal");
            if (modalElement) {
                modalElement.classList.remove("active");
            }
            
            // Refresh dashboard list layers cleanly to incorporate new profiles instant updates
            loadDoctorCards();
        } else {
            alert(result.message || "Unable to save doctor profile records.");
        }
    } catch (error) {
        console.error("Critical fault encountered during adminAddDoctor registration workflow:", error);
        alert("An unexpected exception took place while persisting this record data. Please retry.");
    }
};

// Bind component triggers smoothly upon browser parsing complete milestones
if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", initAdminDashboard);
} else {
    initAdminDashboard();
}
