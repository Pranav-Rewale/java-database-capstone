# Smart Clinic Management System Schema Design


## MySQL Database Design

### Table: patients
- id: INT, Primary Key, Auto Increment
- first_name: VARCHAR(50), Not Null
- last_name: VARCHAR(50), Not Null
- email: VARCHAR(100), Unique, Not Null
- phone: VARCHAR(15), Unique, Not Null
- date_of_birth: DATE, Not Null
- gender: VARCHAR(10)
- address: VARCHAR(255)
- created_at: TIMESTAMP, Default Current Timestamp

Notes:

- Email and phone numbers should be validated in the application layer.
- Patient records should be retained for historical medical records even if the patient becomes inactive.

### Table: doctors
- id: INT, Primary Key, Auto Increment
- first_name: VARCHAR(50), Not Null
- last_name: VARCHAR(50), Not Null
- email: VARCHAR(100), Unique, Not Null
- phone: VARCHAR(15), Unique, Not Null
- specialization: VARCHAR(100), Not Null
- license_number: VARCHAR(50), Unique, Not Null
- created_at: TIMESTAMP, Default Current Timestamp

Notes:

- Each doctor must have a unique license number.
- Doctor contact information should remain available for appointment history records.

### Table: appointments
- id: INT, Primary Key, Auto Increment
- patient_id: INT, Foreign Key → patients(id), Not Null
- doctor_id: INT, Foreign Key → doctors(id), Not Null
- appointment_time: DATETIME, Not Null
- duration_minutes: INT, Default 60
- status: VARCHAR(20), Not Null (Scheduled, Completed, Cancelled)
- created_at: TIMESTAMP, Default Current Timestamp

Foreign Key Relationships:

- patient_id → patients(id)
- doctor_id → doctors(id)

Notes:

- Doctors should not be allowed to have overlapping appointments. This should be enforced by application logic before booking.
- Appointment history should be retained permanently for audit and medical purposes.
- If a patient is deleted, appointments should remain for historical records. Consider using a soft-delete approach instead of physical deletion.

### Table: admin
- id: INT, Primary Key, Auto Increment
- username: VARCHAR(50), Unique, Not Null
- password_hash: VARCHAR(255), Not Null
- email: VARCHAR(100), Unique, Not Null
- role: VARCHAR(20), Default 'ADMIN'
- created_at: TIMESTAMP, Default Current Timestamp

Notes:

- Passwords should be stored as hashed values, never plain text.
- Only authorized administrators should have access to this table.

### Table: clinic_locations
- id: INT, Primary Key, Auto Increment
- name: VARCHAR(100), Not Null
- address: VARCHAR(255), Not Null
- city: VARCHAR(100), Not Null
- phone: VARCHAR(15)
- created_at: TIMESTAMP, Default Current Timestamp

Notes:

- Supports multiple clinic branches in the future.
- Doctors may be assigned to one or more locations.

### Table: doctor_availability
- id: INT, Primary Key, Auto Increment
- doctor_id: INT, Foreign Key → doctors(id), Not Null
- available_date: DATE, Not Null
- start_time: TIME, Not Null
- end_time: TIME, Not Null
- is_available: BOOLEAN, Default TRUE

Foreign Key Relationships:

- doctor_id → doctors(id)

Notes:

- Stores available time slots for doctors.
- Patients can only book appointments during available slots.
- Helps prevent overlapping appointments.

### Table: payments
- id: INT, Primary Key, Auto Increment
- appointment_id: INT, Foreign Key → appointments(id), Not Null
- amount: DECIMAL(10,2), Not Null
- payment_method: VARCHAR(50), Not Null
- payment_status: VARCHAR(20), Not Null (Pending, Completed, Failed)
- transaction_date: TIMESTAMP, Default Current Timestamp

Foreign Key Relationships:

- appointment_id → appointments(id)

Notes:

- Each payment is linked to a specific appointment.
- Payment history should be retained for accounting purposes.

### Design Decisions
1. Patient and doctor information is stored in MySQL because it is structured, relational data with strong consistency requirements.
2. Appointments act as the central relationship table connecting patients and doctors.
3. Doctor availability is stored separately to support scheduling and prevent double-booking.
4. Appointment history is never deleted to preserve medical and audit records.
5. Soft deletion is preferred over permanent deletion of patients or doctors to maintain data integrity.
6. Prescriptions are stored in MongoDB, as shown in the architecture diagram, and are linked to appointments using the appointment_id. A prescription should      always be associated with a specific appointment rather than existing independently.
7. Email, phone number, and password validation should be handled in the application layer using Spring Boot validation mechanisms.

## MongoDB Collection Design

Since prescriptions contain flexible and potentially evolving data (doctor notes, refill information, pharmacy details, attachments, and metadata), they are a good fit for MongoDB. The collection stores prescription documents linked to appointments in MySQL through the appointmentId field. This approach avoids duplicating patient and doctor records while allowing the schema to evolve without database migrations.

### Collection: prescriptions

```
{
  "_id": "ObjectId('6807dd712725f013281e7201')",
  "appointmentId": 51,
  "patientId": 101,
  "doctorId": 201,
  "patientName": "John Smith",
  "medication": "Paracetamol",
  "dosage": "500mg",
  "doctorNotes": "Take 1 tablet every 6 hours.",
  "refillCount": 2,
  "status": "Active",
  "tags": [
    "fever",
    "pain-relief"
  ],
  "pharmacy": {
    "name": "City Health Pharmacy",
    "location": "Downtown Clinic"
  },
  "attachments": [
    {
      "fileName": "lab_report.pdf",
      "fileType": "application/pdf"
    }
  ],
  "metadata": {
    "createdAt": "2025-05-01T10:30:00Z",
    "lastUpdated": "2025-05-03T14:15:00Z"
  }
}
```

### Design Decisions
1. Reference MySQL records using IDs
   - The document stores appointmentId, patientId, and doctorId rather than embedding complete patient or doctor objects.
   - This avoids data duplication and keeps MySQL as the source of truth for relational data.
2. Support schema evolution
   - New fields such as insurance information, allergy warnings, prescription history, or digital signatures can be added without changing existing documents.
3. Use nested documents
   - Pharmacy information and metadata are stored as embedded objects because they belong only to a specific prescription.
4. Use arrays for flexible data
   - Tags and attachments are stored as arrays, making it easy to add multiple values without altering the structure.
5. Prescription-to-Appointment Relationship
   - Each prescription is tied to a specific appointment through appointmentId.
   - This ensures prescriptions are associated with a documented patient-doctor consultation.
  
### Future MongoDB Collections

MongoDB could also be used for collections such as:

#### feedback

```
{
  "_id": "ObjectId('6808aa112725f013281e7301')",
  "appointmentId": 51,
  "patientId": 101,
  "rating": 5,
  "comments": "The doctor was very professional and helpful.",
  "submittedAt": "2025-05-05T16:00:00Z"
}
```

#### messages

```
{
  "_id": "ObjectId('6808bb112725f013281e7302')",
  "appointmentId": 51,
  "senderId": 101,
  "receiverId": 201,
  "message": "Should I continue taking the medication after three days?",
  "timestamp": "2025-05-05T17:30:00Z",
  "read": false
}
```
