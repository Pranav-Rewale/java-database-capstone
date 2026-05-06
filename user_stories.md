##User Stories

'##Admin User Stories'
1. Login to Portal

Title:
As an admin, I want to log into the portal using my username and password, so that I can securely manage the platform.

Acceptance Criteria:

Admin can enter valid username and password
System authenticates credentials
Admin is redirected to dashboard on success

Priority: High
Story Points: 3
Notes:
Show error for invalid credentials


2. Logout from Portal

Title:
As an admin, I want to log out of the portal, so that I can protect system access.

Acceptance Criteria:

Logout button is visible
Session is terminated on click
User is redirected to login page

Priority: High
Story Points: 2

3. Add Doctor

Title:
As an admin, I want to add doctors to the portal, so that they can provide services to patients.

Acceptance Criteria:

Admin can enter doctor details
Data is validated before saving
Doctor is successfully added to system

Priority: High
Story Points: 5

4. Delete Doctor

Title:
As an admin, I want to delete a doctor’s profile, so that I can manage system records.

Acceptance Criteria:

Admin can select a doctor
Confirmation prompt is shown
Doctor record is deleted

Priority: Medium
Story Points: 3

5. View Appointment Statistics

Title:
As an admin, I want to run a stored procedure to get monthly appointment counts, so that I can track usage statistics.

Acceptance Criteria:

Stored procedure runs successfully
Monthly data is retrieved
Results are displayed clearly

Priority: Medium
Story Points: 4




'##Patient User Stories'

6. View Doctors Without Login

Title:
As a patient, I want to view a list of doctors without logging in, so that I can explore options.

Acceptance Criteria:

Doctor list is visible publicly
Basic details are shown
No login is required

Priority: High
Story Points: 3

7. Sign Up

Title:
As a patient, I want to sign up using email and password, so that I can book appointments.

Acceptance Criteria:

User enters email and password
Validation is performed
Account is created successfully

Priority: High
Story Points: 4

8. Login

Title:
As a patient, I want to log into the portal, so that I can manage my bookings.

Acceptance Criteria:

User enters credentials
Authentication is successful
Redirect to dashboard

Priority: High
Story Points: 3

9. Book Appointment

Title:
As a patient, I want to book an appointment with a doctor, so that I can get consultation.

Acceptance Criteria:

Patient selects doctor and time slot
Appointment duration is one hour
Booking is confirmed

Priority: High
Story Points: 5

10. View Upcoming Appointments

Title:
As a patient, I want to view my upcoming appointments, so that I can prepare accordingly.

Acceptance Criteria:

Upcoming appointments are listed
Date and time are visible
Doctor details are shown

Priority: Medium
Story Points: 3




'##Doctor User Stories'


11. Doctor Login

Title:
As a doctor, I want to log into the portal, so that I can manage my appointments.

Acceptance Criteria:

Doctor enters credentials
Authentication is successful
Redirect to dashboard

Priority: High
Story Points: 3

12. Doctor Logout

Title:
As a doctor, I want to log out, so that I can protect my data.

Acceptance Criteria:

Logout option is available
Session ends securely
Redirect to login page

Priority: High
Story Points: 2

13. View Appointment Calendar

Title:
As a doctor, I want to view my appointment calendar, so that I can stay organized.

Acceptance Criteria:

Calendar view is displayed
Appointments are listed by date
Time slots are clearly visible

Priority: High
Story Points: 4

14. Mark Unavailability

Title:
As a doctor, I want to mark my unavailability, so that patients can only book available slots.

Acceptance Criteria:

Doctor selects unavailable dates/times
System blocks those slots
Patients cannot book unavailable slots

Priority: Medium
Story Points: 4

15. Update Profile

Title:
As a doctor, I want to update my profile, so that patients have accurate information.

Acceptance Criteria:

Doctor edits specialization/contact info
Changes are saved
Updated info is visible to patients

Priority: Medium
Story Points: 3

16. View Patient Details

Title:
As a doctor, I want to view patient details for upcoming appointments, so that I can be prepared.

Acceptance Criteria:

Patient details are accessible
Linked to appointments
Information is accurate

Priority: High
Story Points: 4
