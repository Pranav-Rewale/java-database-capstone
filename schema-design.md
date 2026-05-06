## Smart Clinic Management System Schema Design


## MySQL Database Design


The following tables represent the core operational data of the clinic, focusing on structured relationships and data integrity.  

##Table: patients

id: INT, Primary Key, Auto Increment.  

first_name: VARCHAR(50), Not Null.  

last_name: VARCHAR(50), Not Null.  

email: VARCHAR(100), Unique, Not Null.  

phone: VARCHAR(15).  

date_of_birth: DATE, Not Null.  


##Table: doctors
id: INT, Primary Key, Auto Increment.  

full_name: VARCHAR(100), Not Null.  

specialization: VARCHAR(100).  

email: VARCHAR(100), Unique, Not Null.  

working_hours: VARCHAR(255) (e.g., "09:00-17:00").  

##Table: appointments

id: INT, Primary Key, Auto Increment.  

doctor_id: INT, Foreign Key references doctors(id).  

patient_id: INT, Foreign Key references patients(id).  

appointment_time: DATETIME, Not Null.  

status: INT (0: Scheduled, 1: Completed, 2: Cancelled).  

##Table: admin
id: INT, Primary Key, Auto Increment.  

username: VARCHAR(50), Unique, Not Null.  

password_hash: VARCHAR(255), Not Null.  

role: VARCHAR(20) (e.g., 'SUPER_ADMIN', 'STAFF').


## MongoDB Collection Design
MongoDB is used here to store flexible, document-based data such as prescriptions and detailed medical notes that do not require a rigid relational structure.  

Collection: prescriptions
This collection stores clinical outcomes from appointments, allowing for nested metadata and flexible fields.

{
  "_id": "ObjectId('64abc123456')",
  "patientId": 101,
  "appointmentId": 51,
  "medication": [
    {
      "name": "Paracetamol",
      "dosage": "500mg",
      "frequency": "Every 6 hours"
    },
    {
      "name": "Amoxicillin",
      "dosage": "250mg",
      "frequency": "Twice daily"
    }
  ],
  "doctorNotes": "Patient reports mild fever and sore throat. Take tablets after meals.",
  "refillCount": 2,
  "issuedDate": "2026-05-06T19:45:00Z",
  "pharmacy": {
    "name": "Walgreens SF",
    "location": "Market Street",
    "contact": "555-0123"
  },
  "tags": ["Urgent", "Follow-up Required"]
}

Design Justification

MySQL: Chosen for Patients, Doctors, and Appointments because these entities have strict relationships (e.g., an appointment must have a doctor and a patient) that benefit from referential integrity.  

MongoDB: Chosen for Prescriptions to handle varying amounts of medication data and free-form doctor notes without needing to alter a fixed table schema as medical requirements evolve.
