## Architecture summary

The application follows a layered architecture combining both MVC and REST paradigms within a single Spring Boot system. The Admin and Doctor dashboards are implemented using Thymeleaf-based MVC controllers, enabling server-side rendering for UI-driven workflows. In contrast, all other modules are exposed via REST APIs, allowing flexible and scalable client interactions through JSON-based communication. Both controller types—MVC and REST—act as entry points and consistently delegate request handling to a centralized service layer, ensuring separation of concerns and maintainable business logic.

The service layer serves as the core of the application, orchestrating operations and routing data access requests to the appropriate persistence layer. The system uses a hybrid database approach: MySQL for structured relational data (such as Patient, Doctor, Appointment, and Admin entities) managed via JPA repositories, and MongoDB for unstructured or document-based data (specifically Prescription records) handled through MongoDB repositories. This dual-database design allows the application to leverage the strengths of both relational and NoSQL storage while maintaining a unified and scalable architecture.


Steps of Data flow based on the architecture diagram :

1.The user interacts with the application through either the Admin/Doctor dashboards (via Thymeleaf UI) or other modules exposed through REST endpoints.

2.The request is received by the appropriate controller—Thymeleaf (MVC) controllers for UI-based flows or REST controllers for API-based interactions.

3.The controller forwards the request to the centralized service layer, which contains the core business logic.

4.The service layer processes the request and determines which data source (MySQL or MongoDB) is required.

5.For relational data (patients, doctors, appointments, admins), the service layer calls JPA-based MySQL repositories; for prescription-related data, it calls MongoDB repositories.

6.The respective repositories interact with their databases—MySQL using entity models and MongoDB using document models—to fetch or persist data.

7.The response is returned back through the service layer to the controller, which then renders a Thymeleaf view or sends a JSON response to the client.
