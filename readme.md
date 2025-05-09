# Aircraft Mission Management System

A comprehensive JavaFX application for managing aircraft missions, weapons, launchers, and monitoring fatigue data.

## Project Overview

This JavaFX application provides a complete solution for the management of military aircraft missions, weapons, and launcher systems. The application connects to a MySQL database to store and retrieve data, providing an intuitive user interface for operators to manage all aspects of mission planning and execution.

## Features

- **User Authentication**: Secure login system
- **Aircraft Data Management**: Track and manage aircraft by serial number
- **Weapon Management**: Add and view weapons with their specifications
- **Launcher Management**: Track launcher systems and their life status
- **Mission Management**: Create and monitor missions
- **Post-Flight Data Management (PFMD)**: Record flight data and missile status after missions
- **Fatigue Monitoring**: Track launcher life status and generate reports

## Requirements

- Java JDK 17 or higher
- MySQL 8.0 or higher
- JavaFX (included in the Maven dependencies)

## Installation and Setup

### Database Setup

1. Install MySQL Server if not already installed
2. Open MySQL Workbench or your preferred MySQL client
3. Import the provided SQL file (`manutenzione_am.sql`) to create the database schema and tables
4. Verify the database was created successfully

### Application Setup

1. Clone or download this repository
2. Open the project in IntelliJ IDEA or your preferred Java IDE
3. Configure the database connection in `src/main/java/com/aircraft/config/DBConfig.java`
4. Build the project using Maven: `mvn clean install`
5. Run the application: `mvn javafx:run` or run `Main.java` directly from your IDE

## Project Structure

```
AircraftMissionManagement/
├── lib/                       # External libraries
│   └── mysql-connector-j-8.0.33.jar
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── aircraft/
│   │   │           ├── config/      # Configuration classes
│   │   │           ├── controller/  # JavaFX controllers
│   │   │           ├── dao/         # Data Access Objects
│   │   │           ├── model/       # Domain model classes
│   │   │           ├── util/        # Utility classes
│   │   │           └── Main.java    # Application entry point
│   │   └── resources/
│   │       ├── css/                 # CSS stylesheets
│   │       ├── fxml/                # FXML layout files
│   │       └── images/              # Image resources
└── pom.xml                         # Maven project configuration
```

## Usage

1. **Login**: Use username "root" and password "100K" to login
2. **Navigation**: Use the menu tree on the left side of the dashboard to navigate to different modules
3. **Data Management**: Add and view weapons, launchers, and aircraft data
4. **Mission Management**: Create new missions and assign weapons/launchers
5. **PFMD**: Record post-flight data including missile status
6. **Fatigue Monitoring**: Track launcher life status and generate reports

## Screenshots

- Login Screen: Simple authentication
- Dashboard: Main navigation interface with menu tree
- PFMD: Interactive interface for recording missile status
- Fatigue Monitoring: Detailed view of launcher life status

## Customization

- The UI colors and styles can be modified in `src/main/resources/css/main.css`
- Layout modifications can be made through the FXML files in `src/main/resources/fxml/`
- Database connection settings can be adjusted in `DBConfig.java`

## Default Credentials

- Username: root
- Password: 0000

## Development Notes

This application follows the MVC (Model-View-Controller) architecture:
- Models represent database entities
- Views are implemented with FXML files
- Controllers handle user interaction and business logic

## License

This project is provided as-is with no warranty. Use at your own risk.

## Acknowledgments

This project was created based on the requirements provided in the document "da mandare.it.en.docx".