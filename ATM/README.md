# 🏧 ATM Interface System

A professional **ATM Interface System** built with **Java Swing GUI**, **MySQL Database**, and **JDBC** connectivity. This project demonstrates core Object-Oriented Programming (OOP) concepts in a real-world banking application.

---

## 📋 Features

| Feature | Description |
|---|---|
| **Signup** | Create account with name, mobile, address, PIN |
| **Login** | Authenticate using Account Number + PIN |
| **Check Balance** | View current account balance |
| **Deposit** | Add money to your account |
| **Withdraw** | Withdraw with insufficient balance check |
| **Transfer** | Send money to another account |
| **Transaction History** | View all past transactions in a table |
| **Dark Theme GUI** | Modern, professional dark-themed interface |

---

## 🛠 Technology Stack

- **Language:** Java (JDK 8+)
- **GUI:** Java Swing (Dark Theme)
- **Database:** MySQL
- **Connectivity:** JDBC (MySQL Connector/J)
- **Concepts:** OOP, Encapsulation, DAO Pattern, MVC

---

## 📁 Project Structure

```
ATM/
├── src/
│   └── atm/
│       ├── Main.java              ← Entry Point
│       ├── model/
│       │   ├── User.java          ← User model (name, mobile, pin)
│       │   ├── Account.java       ← Account model (accNo, balance)
│       │   └── Transaction.java   ← Transaction model (type, amount)
│       ├── db/
│       │   └── DatabaseConnection.java  ← JDBC Connection (Singleton)
│       ├── dao/
│       │   ├── UserDAO.java       ← User DB operations (signup, login)
│       │   ├── AccountDAO.java    ← Account DB operations (deposit, withdraw, transfer)
│       │   └── TransactionDAO.java ← Transaction DB operations (history)
│       └── gui/
│           ├── UIHelper.java      ← Reusable styled components
│           ├── LoginFrame.java    ← Login screen
│           ├── SignupFrame.java   ← Registration screen
│           └── ATMFrame.java      ← Main ATM menu screen
├── sql/
│   └── schema.sql                 ← Database creation script
├── lib/
│   └── mysql-connector-j-X.X.X.jar  ← (Place MySQL JAR here)
├── run.bat                        ← Compile + Run script
├── compile.bat                    ← Compile only script
└── README.md                     ← This file
```

---

## 🚀 How to Run (Step by Step)

### Prerequisites
- **JDK 8+** installed ([Download](https://www.oracle.com/java/technologies/javase-downloads.html))
- **MySQL Server** installed and running ([Download](https://dev.mysql.com/downloads/mysql/))
- **MySQL Connector/J** JAR file ([Download](https://dev.mysql.com/downloads/connector/j/))

### Step 1: Set Up the Database

1. Open **MySQL Command Line** or **MySQL Workbench**
2. Run the SQL script:

```sql
SOURCE C:/Users/Admin/OneDrive/Desktop/ATM/sql/schema.sql;
```

Or copy-paste the contents of `sql/schema.sql` into MySQL.

### Step 2: Configure Database Connection

Open `src/atm/db/DatabaseConnection.java` and update:

```java
private static final String URL = "jdbc:mysql://localhost:3306/atm_system";
private static final String USERNAME = "root";     // ← Your MySQL username
private static final String PASSWORD = "";         // ← Your MySQL password
```

### Step 3: Add MySQL Connector JAR

1. Download `mysql-connector-j-X.X.X.jar` from the link above
2. Place it in the `lib/` folder

### Step 4: Compile & Run

**Option A - Using batch script (Easy):**
```
Double-click run.bat
```

**Option B - Manual commands:**
```cmd
cd C:\Users\Admin\OneDrive\Desktop\ATM

REM Compile
javac -d out -cp "lib\mysql-connector-j-9.2.0.jar" src\atm\model\*.java src\atm\db\*.java src\atm\dao\*.java src\atm\gui\*.java src\atm\Main.java

REM Run
java -cp "out;lib\mysql-connector-j-9.2.0.jar" atm.Main
```

> ⚠️ Replace `mysql-connector-j-9.2.0.jar` with your actual JAR filename.

---

## 🗄 Database Schema

### Tables

#### 1. `users` - Stores user personal details
| Column | Type | Description |
|---|---|---|
| user_id | INT (PK, Auto) | Unique user ID |
| full_name | VARCHAR(100) | User's full name |
| mobile | VARCHAR(15) | Mobile number |
| address | VARCHAR(255) | Address |
| pin | VARCHAR(10) | 4-digit PIN |
| created_at | TIMESTAMP | Registration date |

#### 2. `accounts` - Stores bank account details
| Column | Type | Description |
|---|---|---|
| account_id | INT (PK, Auto) | Unique account ID |
| user_id | INT (FK) | Linked user ID |
| account_number | VARCHAR(20) | Unique 10-digit account number |
| ifsc_code | VARCHAR(15) | Generated IFSC code |
| balance | DOUBLE | Current balance |
| created_at | TIMESTAMP | Account creation date |

#### 3. `transactions` - Stores all transaction history
| Column | Type | Description |
|---|---|---|
| transaction_id | INT (PK, Auto) | Unique transaction ID |
| account_number | VARCHAR(20) (FK) | Account involved |
| transaction_type | VARCHAR(20) | DEPOSIT / WITHDRAW / TRANSFER_IN / TRANSFER_OUT |
| amount | DOUBLE | Transaction amount |
| balance_after | DOUBLE | Balance after transaction |
| description | VARCHAR(255) | Transaction description |
| transaction_date | TIMESTAMP | Date & time of transaction |

---

## 🧠 OOP Concepts Used

| Concept | Where Used |
|---|---|
| **Encapsulation** | All model classes (private fields + getters/setters) |
| **Constructor Overloading** | User, Account, Transaction classes |
| **Singleton Pattern** | DatabaseConnection class |
| **DAO Pattern** | Separate DAO classes for each entity |
| **MVC Architecture** | Model (models), View (GUI), Controller (DAOs) |
| **Association** | User → Account (one-to-one) |
| **Exception Handling** | Try-catch in all database operations |
| **Transaction Management** | Atomic operations with commit/rollback |

---

## 📸 Application Flow

```
┌─────────────┐     ┌──────────────┐     ┌─────────────────┐
│   STARTUP   │────▶│  LOGIN SCREEN │────▶│   ATM MENU      │
│  (Main.java)│     │              │     │                 │
└─────────────┘     │  • Acc No    │     │  • Balance      │
                    │  • PIN       │     │  • Deposit      │
                    │              │     │  • Withdraw     │
                    │  [Signup] ──▶│     │  • Transfer     │
                    │              │     │  • History      │
                    └──────────────┘     │  • Logout       │
                           │             └─────────────────┘
                           ▼
                    ┌──────────────┐
                    │ SIGNUP SCREEN│
                    │              │
                    │  • Name      │
                    │  • Mobile    │
                    │  • Address   │
                    │  • PIN       │
                    │              │
                    │  → Generates │
                    │    Acc No    │
                    │    IFSC Code │
                    └──────────────┘
```

---

## 🔑 Viva / Exam Questions & Answers

<details>
<summary><b>1. What design pattern is used for database connection?</b></summary>
Singleton Pattern - Only one connection instance is created and reused throughout the application.
</details>

<details>
<summary><b>2. What is JDBC?</b></summary>
Java Database Connectivity - An API that allows Java programs to interact with databases using SQL queries.
</details>

<details>
<summary><b>3. Why use PreparedStatement instead of Statement?</b></summary>
PreparedStatement prevents SQL injection attacks and improves performance through query pre-compilation.
</details>

<details>
<summary><b>4. What is the DAO pattern?</b></summary>
Data Access Object - Separates database operations from business logic. Each entity (User, Account, Transaction) has its own DAO class.
</details>

<details>
<summary><b>5. How is money transfer handled atomically?</b></summary>
Using database transactions with setAutoCommit(false), commit(), and rollback(). If any step fails, all changes are rolled back.
</details>

<details>
<summary><b>6. What OOP concepts are demonstrated?</b></summary>
Encapsulation (private fields), Constructor Overloading, Association (User-Account), Singleton Pattern, and Abstraction through the DAO layer.
</details>

---

## 📝 License

This project is created for educational purposes. Feel free to use and modify.
