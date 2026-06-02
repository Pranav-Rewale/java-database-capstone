/* doctorCard.js - Reusable UI component for rendering doctor profiles dynamically */

// Import dependent context actions from layout handlers and service endpoints
import { showBookingOverlay } from "../loggedPatient.js";
import { deleteDoctor } from "../services/doctorServices.js";
import { getPatientData } from "../services/patientServices.js";

/**
 * Named export function that constructs a modular HTML component tree representing 
 * a single doctor, managing interactive action controls dynamically based on role configurations.
 * * @param {Object} doctor - Target payload object holding specific provider information.
 * @returns {HTMLElement} - Completely assembled interactive doctor card element.
 */
export function createDoctorCard(doctor) {
    // 1. Initialize the baseline outer structural layout card wrapper
    const card = document.createElement("div");
    card.classList.add("doctor-card");

    // 2. Extract current state verification contexts from localStorage
    const role = localStorage.getItem("userRole");

    // 3. Assemble the comprehensive text profile metadata content section
    const infoDiv = document.createElement("div");
    infoDiv.classList.add("doctor-info");

    const name = document.createElement("h3");
    name.textContent = doctor.name || "N/A";

    const specialization = document.createElement("p");
    specialization.classList.add("doctor-specialty");
    specialization.innerHTML = `<strong>Specialty:</strong> ${doctor.specialty || "General Physician"}`;

    const email = document.createElement("p");
    email.classList.add("doctor-email");
    email.innerHTML = `<strong>Email:</strong> ${doctor.email || "N/A"}`;

    const availability = document.createElement("p");
    availability.classList.add("doctor-availability");
    
    // Evaluate if availableTimes exists and is non-empty before processing array strings
    const timesList = (doctor.availableTimes && doctor.availableTimes.length > 0)
        ? doctor.availableTimes.join(", ")
        : "No active slot configurations";
    availability.innerHTML = `<strong>Available Slots:</strong> ${timesList}`;

    // Append standard profile records fields to the secondary section holder
    infoDiv.appendChild(name);
    infoDiv.appendChild(specialization);
    infoDiv.appendChild(email);
    infoDiv.appendChild(availability);

    // 4. Set up the dynamic button layout container to accommodate contextual actions
    const actionsDiv = document.createElement("div");
    actionsDiv.classList.add("card-actions");

    // 5. Evaluate and inject interaction triggers customized by current authorization roles
    if (role === "admin") {
        const removeBtn = document.createElement("button");
        removeBtn.textContent = "Delete";
        removeBtn.classList.add("admin-delete-btn");

        // Set non-blocking asynchronous listener loop to invoke administrative backend overrides
        removeBtn.addEventListener("click", async () => {
            const confirmAction = confirm(`Are you absolutely sure you want to permanently delete Dr. ${doctor.name}?`);
            if (!confirmAction) return;

            const token = localStorage.getItem("token");
            if (!token) {
                alert("Administrative operation revoked: Missing security token. Please log in again.");
                return;
            }

            try {
                // Trigger background operational call to core API endpoint service routing systems
                const success = await deleteDoctor(doctor.id, token);
                if (success) {
                    alert("Doctor record purged successfully.");
                    card.remove(); // Seamlessly animate card eviction out of active viewport tracking tree
                } else {
                    alert("Unable to delete doctor. Please verify server connectivity or permissions.");
                }
            } catch (error) {
                console.error("Critical fault encountered during doctor record deletion workflow:", error);
                alert("An error occurred while attempting to delete this provider profile record.");
            }
        });

        actionsDiv.appendChild(removeBtn);
    } 
    else if (role === "patient") {
        const bookNow = document.createElement("button");
        bookNow.textContent = "Book Now";
        bookNow.classList.add("booking-trigger-btn");

        // Public user alert configuration catch-interceptor
        bookNow.addEventListener("click", () => {
            alert("Patient needs to login first.");
        });

        actionsDiv.appendChild(bookNow);
    } 
    else if (role === "loggedPatient") {
        const bookNow = document.createElement("button");
        bookNow.textContent = "Book Now";
        bookNow.classList.add("booking-trigger-btn");

        // Active booking operations tracking loop flow
        bookNow.addEventListener("click", async (e) => {
            const token = localStorage.getItem("token");
            if (!token) {
                alert("Authentication missing. Redirecting to initialization panel...");
                window.location.href = "/";
                return;
            }

            try {
                // Disable button element context block briefly to prevent double-tap execution crashes
                bookNow.disabled = true;
                bookNow.textContent = "Loading...";

                // Fetch comprehensive demographic records via background API stream maps
                const patientData = await getPatientData(token);
                
                if (patientData) {
                    // Populate and present overlay workspace context drawer
                    showBookingOverlay(e, doctor, patientData);
                } else {
                    alert("Failed to retrieve valid patient metadata configuration records.");
                }
            } catch (error) {
                console.error("Critical workflow block detected during appointment initialization setup:", error);
                alert("An error occurred while opening the scheduling drawer dashboard. Please retry.");
            } finally {
                // Restore interaction control capabilities to base state boundaries
                bookNow.disabled = false;
                bookNow.textContent = "Book Now";
            }
        });

        actionsDiv.appendChild(bookNow);
    }

    // 6. Final assembly coupling of all nested elements into the target component instance
    card.appendChild(infoDiv);
    card.appendChild(actionsDiv);

    // 7. Relinquish component control loop context boundaries back to the structural painter loop
    return card;
}