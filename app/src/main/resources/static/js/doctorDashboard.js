/* doctorDashboard.js - Service script managing clinician dashboard tracking and appointment tables */

// Import required backend service wrappers and row factories
import { getAllAppointments } from "./services/appointmentRecordService.js";
import { createPatientRow } from "./components/patientRows.js";

// --- Initialize Global Variables ---
let tableBody = null;
let selectedDate = "";
let token = "";
let patientName = "null"; // Expected as a string literal "null" by the backend configuration when empty

/**
 * Initializes the doctor dashboard workspace components and hooks up interactive DOM listeners.
 */
function initDoctorDashboard() {
    // Select the table body target container where patient rows will be cleanly appended
    tableBody = document.getElementById("patientTableBody");
    
    // Retrieve authentication token context parameters
    token = localStorage.getItem("token");

    // Initialize selectedDate with today's date in 'YYYY-MM-DD' format
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const dd = String(today.getDate()).padStart(2, '0');
    selectedDate = `${yyyy}-${mm}-${dd}`;

    // Update the date picker UI input element to visually reflect today's date
    const datePicker = document.getElementById("datePicker");
    if (datePicker) {
        datePicker.value = selectedDate;
        
        // Add a change event listener to the date picker component
        datePicker.addEventListener("change", (e) => {
            selectedDate = e.target.value;
            loadAppointments();
        });
    }

    // Add an 'input' event listener to the client search lookup bar
    const searchBar = document.getElementById("searchBar");
    if (searchBar) {
        searchBar.addEventListener("input", (e) => {
            const inputVal = e.target.value.trim();
            // If search is not empty, use it as filter; else reset to "null" as expected by backend
            patientName = inputVal !== "" ? inputVal : "null";
            loadAppointments();
        });
    }

    // Add a click listener to the "Today's Appointments" command button
    const todayButton = document.getElementById("todayButton");
    if (todayButton) {
        todayButton.addEventListener("click", () => {
            selectedDate = `${yyyy}-${mm}-${dd}`;
            if (datePicker) {
                datePicker.value = selectedDate;
            }
            loadAppointments();
        });
    }

    // Execute baseline data extraction to populate today's appointments by default
    loadAppointments();
}

/**
 * Fetch and display patient records based on selected date criteria and search parameters.
 */
async function loadAppointments() {
    if (!tableBody) return;

    try {
        // Step 1: Call getAllAppointments with selectedDate, patientName, and token
        const appointments = await getAllAppointments(selectedDate, patientName, token);

        // Step 2: Clear the table body content completely before rendering new rows
        tableBody.innerHTML = "";

        // Step 3: If no appointments are returned, present a friendly italic empty state message row
        if (!appointments || appointments.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="5" class="noPatientRecord">No Appointments found for today.</td>
                </tr>
            `;
            return;
        }

        // Step 4: If appointments exist, process entries sequentially
        appointments.forEach(appointment => {
            // Support defensive checking in case nested patient payload objects are flat or grouped
            const patientObj = appointment.patient || {
                id: appointment.patientId || "N/A",
                name: appointment.patientName || "Unknown Patient",
                phone: appointment.patientPhone || appointment.phone || "N/A",
                email: appointment.patientEmail || appointment.email || "N/A"
            };

            // Call createPatientRow to generate a cleanly formatted standard table row structure
            const rowElement = createPatientRow(appointment, patientObj);
            
            // Append each row to the table body container workspace target
            tableBody.appendChild(rowElement);
        });

    } catch (error) {
        // Step 5: Catch and handle any errors during fetch by mounting an explicit warning feedback banner
        console.error("Critical fault encountered inside loadAppointments() routine:", error);
        tableBody.innerHTML = `
            <tr>
                <td colspan="5" class="noPatientRecord" style="color: #A62B1F;">
                    Error loading appointments. Try again later.
                </td>
            </tr>
        `;
    }
}

// --- Initial Render on Page Load ---
document.addEventListener("DOMContentLoaded", () => {
    // Call renderContent layout setup function if it is globally active in your architecture environment
    if (typeof window.renderContent === "function") {
        window.renderContent();
    }
    
    // Fire up baseline dashboard operations trackers
    initDoctorDashboard();
});

// Expose loadAppointments globally to support child rows or modal forms triggering table refreshes
window.refreshAppointmentsTable = loadAppointments;
