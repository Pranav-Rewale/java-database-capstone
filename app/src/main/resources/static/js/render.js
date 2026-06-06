// render.js

function selectRole(role) {
  if (!role) return;
  const normalizedRole = role.toLowerCase();
  setRole(normalizedRole);
  const token = localStorage.getItem('token');
  if (normalizedRole === "admin") {
    if (token) {
      window.location.href = `/adminDashboard/${token}`;
    } else {
      if (typeof window.openModal === "function") {
        window.openModal("adminLogin");
      }
    }
  } else if (normalizedRole === "patient") {
    window.location.href = "/pages/patientDashboard.html";
  } else if (normalizedRole === "doctor") {
    if (token) {
      window.location.href = `/doctorDashboard/${token}`;
    } else {
      if (typeof window.openModal === "function") {
        window.openModal("doctorLogin");
      }
    }
  } else if (normalizedRole === "loggedpatient") {
    window.location.href = "/pages/loggedPatientDashboard.html";
  }
}
window.selectRole = selectRole;


function renderContent() {
  const role = getRole();
  if (!role) {
    window.location.href = "/"; // if no role, send to role selection page
    return;
  }
}
