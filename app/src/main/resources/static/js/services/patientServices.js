/* patientServices.js - Service module for managing patient-related backend API interactions */

// Import the central API base URL configuration to avoid hardcoding routes
import { API_BASE_URL } from "../config/config.js";

// Centralized Patient Base API Endpoint
const PATIENT_API = API_BASE_URL + '/patient';

/**
 * Handles registration and persistence of new patient records in the database system.
 * @param {Object} data - Form metadata payload holding name, email, password, etc.
 * @returns {Promise<Object>} Formatted object holding transaction outcome { success: boolean, message: string }.
 */
export async function patientSignup(data) {
    try {
        // Step 1: Send a POST request containing the body payload to the signup endpoint
        const response = await fetch(`${PATIENT_API}/signup`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(data) // Transmit formatted patient structural values
        });

        // Step 2: Extract the response payload to pass outcomes upstream
        const result = await response.json();

        if (response.ok) {
            return {
                success: true,
                message: result.message || "Registration completed successfully."
            };
        }

        return {
            success: false,
            message: result.message || `Signup failed with status code: ${response.status}`
        };

    } catch (error) {
        // Step 3: Trap unexpected infrastructure dropouts gracefully
        console.error("Critical error encountered within patientSignup() transaction stream:", error);
        return {
            success: false,
            message: "Unable to reach the registration server. Please check connection parameters."
        };
    }
}

/**
 * Authenticates a patient against core verification services.
 * @param {Object} data - Target verification credentials (email, password).
 * @returns {Promise<Response>} The raw HTTP fetch response wrapper for downstream lifecycle tracking.
 */
export async function patientLogin(data) {
    try {
        // Step 1: Send a POST request with headers indicating JSON content to validation services
        const response = await fetch(`${PATIENT_API}/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(data)
        });

        // Step 2: Relinquish control back to dashboard engines to extract token keys
        return response;

    } catch (error) {
        console.error("Critical error encountered within patientLogin() execution thread:", error);
        throw error; // Bubble up connection exceptions to trigger UI warning modules
    }
}

/**
 * Retrieves the profile details of the currently authenticated active Patient session.
 * @param {string} token - The active user's authorization JWT security token.
 * @returns {Promise<Object|null>} Patient profile dictionary records object, or null on rejection.
 */
export async function getPatientData(token) {
    if (!token) {
        console.error("Operation aborted: getPatientData requires an active authorization token.");
        return null;
    }

    try {
        // Step 1: Dispatch a GET request securely conveying the credential token in headers
        const response = await fetch(`${PATIENT_API}/me`, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Accept': 'application/json'
            }
        });

        // Step 2: Evaluate operational parsing states
        if (response.ok) {
            const patientData = await response.json();
            return patientData;
        }

        console.warn(`Profile extraction failed at backend with status code: ${response.status}`);
        return null;

    } catch (error) {
        console.error("Gracefully caught fault during patient data recovery:", error);
        return null;
    }
}

/**
 * Fetches medical appointment records dynamically for both Doctor and Patient tracking spaces.
 * @param {number|string} id - Target baseline tracking parameter identity.
 * @param {string} token - The active account's session security token.
 * @param {string} user - Role contextual key indicator (e.g., "patient" or "doctor").
 * @returns {Promise<Array|null>} Combined array block containing historical schedules, or null.
 */
export async function getPatientAppointments(id, token, user) {
    if (!id || !token || !user) {
        console.error("Mandatory query arguments are missing from getPatientAppointments execution footprint.");
        return null;
    }

    try {
        // Step 1: Construct a dynamic URL path targeting the specific workspace environment request
        // e.g., maps endpoints out to either '/patient/appointments/patient/1' or '/patient/appointments/doctor/1'
        const dynamicUrl = `${PATIENT_API}/appointments/${user.toLowerCase()}/${id}`;

        // Step 2: Send a secure GET request using the verified session token mapping
        const response = await fetch(dynamicUrl, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Accept': 'application/json'
            }
        });

        // Step 3: Return extracted payload arrays back to presentation view painters
        if (response.ok) {
            const appointmentsArray = await response.json();
            return Array.isArray(appointmentsArray) ? appointmentsArray : [];
        }

        console.error(`Schedules inquiry failed at server boundaries. Status: ${response.status}`);
        return null;

    } catch (error) {
        console.error("Critical error caught inside getPatientAppointments data fetch routine:", error);
        return null;
    }
}

/**
 * Performs parameterized filtering operations across scheduled consultation arrays.
 * @param {string} condition - Diagnostic lookup condition state string (e.g., "pending" or "consulted").
 * @param {string} name - Doctor or Patient criteria name target lookup tracking keyword.
 * @param {string} token - The active user's structural session token.
 * @returns {Promise<Array>} Filtered operational consultation arrays, or an empty fallback checklist block.
 */
export async function filterAppointments(condition, name, token) {
    if (!token) {
        console.error("Security exception: Filtering operations require an active session token context.");
        return [];
    }

    try {
        // Step 1: Secure query string parsing avoiding manual raw string concatenation issues
        const searchParams = new URLSearchParams();
        if (condition && condition.trim() !== "") searchParams.append("status", condition.trim());
        if (name && name.trim() !== "") searchParams.append("queryName", name.trim());

        // Step 2: Formulate clean URL paths utilizing the safe parsing matrix parameters
        const destinationUrl = searchParams.toString() 
            ? `${PATIENT_API}/appointments/filter?${searchParams.toString()}` 
            : `${PATIENT_API}/appointments/filter`;

        // Step 3: Run the network request cycle passing required security authorization bearer keys
        const response = await fetch(destinationUrl, {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Accept': 'application/json'
            }
        });

        // Step 4: Safely process data outputs preventing presentation layer breaks
        if (response.ok) {
            const filteredResults = await response.json();
            return Array.isArray(filteredResults) ? filteredResults : [];
        }

        console.warn(`Filter operation returned unsupportable response signature code: ${response.status}`);
        return [];

    } catch (error) {
        console.error("Unexpected failure tracked within filterAppointments service execution block:", error);
        alert("An error occurred while filtering data parameters due to background infrastructure limits.");
        return [];
    }
}
