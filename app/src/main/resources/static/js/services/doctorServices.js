/* doctorServices.js - Service module for managing doctor-related backend API interactions */

// Import central API base configurations
import { API_BASE_URL } from "../config/config.js";

// Centralized Doctor Base API Endpoint
const DOCTOR_API = API_BASE_URL + '/doctor';

/**
 * Retrieves the full list of all active doctors from the system database.
 * Used on Admin, Patient, and Guest dashboards.
 * @returns {Promise<Array>} Resolves to a list of doctor objects or an empty array on failure.
 */
export async function getDoctors() {
    try {
        const response = await fetch(DOCTOR_API, {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        });

        if (response.ok) {
            const data = await response.json();
            return Array.isArray(data) ? data : [];
        }
        
        console.error(`Failed to fetch doctors list. Status: ${response.status}`);
        return [];
    } catch (error) {
        // Gracefully handles network or server errors to prevent frontend page crashes
        console.error("Critical error encountered within getDoctors() service:", error);
        return [];
    }
}

/**
 * Removes a doctor profile permanently from the database.
 * Restricted to authenticated Administrators.
 * @param {number|string} id - The unique identifier of the target doctor.
 * @param {string} token - The active administrative JWT security token.
 * @returns {Promise<boolean>} True if successfully deleted, false otherwise.
 */
export async function deleteDoctor(id, token) {
    if (!id || !token) {
        console.error("Missing mandatory parameters for deleteDoctor() transaction.");
        return false;
    }

    try {
        // Constructs the full secure endpoint URL using the ID and authorization token headers
        const response = await fetch(`${DOCTOR_API}/${id}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Accept': 'application/json'
            }
        });

        if (response.ok) {
            return true;
        }

        console.error(`Delete transaction failed at server level. Status: ${response.status}`);
        return false;
    } catch (error) {
        console.error(`Critical crash intercepted inside deleteDoctor(id: ${id}):`, error);
        return false;
    }
}

/**
 * Registers and persists a brand new Doctor profile into the system database records.
 * Restricted to authenticated Administrators via the 'Add Doctor' modal popup drawer.
 * @param {Object} doctor - Object payload filled with doctor attributes (name, email, specialty, etc).
 * @param {string} token - The active administrative JWT security token.
 * @returns {Promise<Object>} Formatted object holding transaction status { success: boolean, message: string }.
 */
export async function saveDoctor(doctor, token) {
    if (!doctor || !token) {
        return { success: false, message: "Invalid argument signatures provided to service." };
    }

    try {
        const response = await fetch(DOCTOR_API, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(doctor) // Transforms data payload into standardized JSON string streams
        });

        if (response.ok) {
            const data = await response.json();
            return {
                success: true,
                message: data.message || "Doctor account registered successfully."
            };
        }

        // Handles server-side business logic rejections (e.g., duplicated email entries, failed bean validations)
        let errorMsg = "Server validation failed.";
        try {
            const errData = await response.json();
            errorMsg = errData.message || errorMsg;
        } catch (_) {}

        return { success: false, message: `Registration failed: ${errorMsg} (Status ${response.status})` };
    } catch (error) {
        console.error("Debugging context logged within saveDoctor():", error);
        return { success: false, message: "Network connection lost or server unreachable. Please retry." };
    }
}

/**
 * Executes a filtered search query against doctor records using query string criteria matching rules.
 * Supports asynchronous search inputs and specialty drop-down selection pipelines.
 * @param {string} name - Doctor target lookup name constraint string parameter.
 * @param {string} time - Availability timeline constraint parameter (e.g. 'AM' or 'PM').
 * @param {string} specialty - Targeted clinical field qualification criterion string.
 * @returns {Promise<Array>} Filtered results arrays or a placeholder empty list array.
 */
export async function filterDoctors(name, time, specialty) {
    try {
        // Build dynamic search criteria parameters safely avoiding broken undefined strings
        const queryParams = new URLSearchParams();
        if (name && name.trim() !== "") queryParams.append("name", name.trim());
        if (time && time.trim() !== "") queryParams.append("time", time.trim());
        if (specialty && specialty.trim() !== "") queryParams.append("specialty", specialty.trim());

        // Combines base paths smoothly with variable queries to form valid REST URLs
        const filterUrl = queryParams.toString() ? `${DOCTOR_API}/search?${queryParams.toString()}` : DOCTOR_API;

        const response = await fetch(filterUrl, {
            method: 'GET',
            headers: { 'Accept': 'application/json' }
        });

        if (response.ok) {
            const data = await response.json();
            return Array.isArray(data) ? data : [];
        }

        console.warn(`Search routing filter rejected with status code: ${response.status}`);
        return [];
    } catch (error) {
        console.error("Query filter sequence execution exception caught:", error);
        alert("Unable to process filter criteria at this time due to system connectivity issues.");
        return [];
    }
}
