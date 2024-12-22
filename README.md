# :money_with_wings: **PennyPincher** :money_with_wings:

PennyPincher is a local web application designed to simplify expense splitting among groups of friends. With this tool, you can easily create events, invite friends, add expenses, and divide them as you see fit. The application also allows users to track their spending through their personalized profile page.

## :coin: **Features**

- User Registration and Authentication: Secure account creation and login via Spring Security.

- Event Management: Create events and invite friends to participate.

- Expense Splitting: Add expenses to events and allocate them flexibly.

- User Dashboard: View personal expenses and events on your profile page.

## :coin: **Technologies Used**

- Backend: Java, Spring Framework (Spring MVC, Spring Boot, Spring Security, Spring Data JPA)

- Frontend: Thymeleaf

- Database: MySQL
  
- ORM: Hibernate

- Testing: JUnit, Mockito

- Containerization: Docker

- Architecture: Model-View-Controller (MVC)

## :coin: **Getting Started**

### **Prerequisites**

- Docker (option 1): For running the application via containerized scripts

- PowerShell (option 1): To use the provided run_pennypincher.ps1 script

- Java Development Kit (JDK) (option 2): Version 17

- MySQL (option 2): A running instance of MySQL server

## :coin: **Setup Options**

### :dollar: **Option 1**: Quick Setup with PowerShell Script
*Note: If you choose this option, there is no need to download the entire project. The script will handle everything for you.*

1. Download the run_pennypincher.ps1 script.

2. Execute the script in PowerShell by ".\run_pennypincher.ps1" in console.

3. Access the Application by going to "http://localhost:8080".

### :dollar: **Option 2**: Manual Setup

1. Clone the Repository:

2. git clone https://github.com/DamianKwcn/PennyPincher.git

Configure Application:

3. Open the application.yml file located in the src/main/resources directory.

4. Set your MySQL connection details (e.g., URL, username, password).

5. Build the Application by "mvn clean install" in console.

6. Run the Application by "java -jar target/CV-0.0.1-SNAPSHOT.jar" in console.

7. Open your browser and go to "http://localhost:8080".

## :coin: **Usage Instructions**

 1. Create an Account: Sign up on the registration page.

 2. Log In: Use your credentials to log in.

 3. Create an Event: Add an event and invite your friends.

 4. Add Expenses: Enter expenses for the event and split them as you see fit.

 5. Track Your Spending: View your expenses and event details on your profile page.

## :coin: **Challenges Encountered**

 - Database Relationships: Defining and maintaining correct relationships between entities (e.g., users, events, and expenses) required careful planning and testing to avoid data inconsistency.

## :coin: **Future Enhancements**

- Additional Expense Types: Support for a wider variety of expense categories and custom rules for splitting expenses.

- Real-Time Payments: Integration with payment gateways to enable real-time expense settlements.
