# MicroSave – Self-Help Group (SHG) Savings & Loan Tracker

A comprehensive Full-Stack Java Web Application built with **Spring Boot 3**, **Spring Data JPA / Hibernate**, **MySQL**, **Thymeleaf**, and **Springdoc OpenAPI (Swagger)** for tracking member savings contributions, calculating dynamic group pool balances, disbursing internal loans, and managing debt repayment.

---

## 1. Project Architecture & Directory Structure

```text
microsave/
│
├── pom.xml
├── README.md
│
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/microsave/
    │   │       ├── MicroSaveApplication.java
    │   │       │
    │   │       ├── controller/
    │   │       │   ├── GroupController.java
    │   │       │   ├── MemberController.java
    │   │       │   ├── ContributionController.java
    │   │       │   ├── LoanController.java
    │   │       │   ├── RepaymentController.java
    │   │       │   └── WebController.java
    │   │       │
    │   │       ├── service/
    │   │       │   ├── GroupService.java
    │   │       │   ├── MemberService.java
    │   │       │   ├── ContributionService.java
    │   │       │   ├── LoanService.java
    │   │       │   └── RepaymentService.java
    │   │       │
    │   │       ├── repository/
    │   │       │   ├── GroupRepository.java
    │   │       │   ├── MemberRepository.java
    │   │       │   ├── ContributionRepository.java
    │   │       │   ├── LoanRepository.java
    │   │       │   └── RepaymentRepository.java
    │   │       │
    │   │       ├── entity/
    │   │       │   ├── Group.java
    │   │       │   ├── Member.java
    │   │       │   ├── Contribution.java
    │   │       │   ├── Loan.java
    │   │       │   ├── Repayment.java
    │   │       │   └── LoanStatus.java
    │   │       │
    │   │       ├── dto/
    │   │       │   ├── GroupRequest.java
    │   │       │   ├── MemberRequest.java
    │   │       │   ├── ContributionRequest.java
    │   │       │   ├── LoanRequest.java
    │   │       │   ├── RepaymentRequest.java
    │   │       │   ├── MemberBalanceResponse.java
    │   │       │   └── GroupPoolResponse.java
    │   │       │
    │   │       └── exception/
    │   │           ├── ResourceNotFoundException.java
    │   │           ├── BusinessRuleException.java
    │   │           └── GlobalExceptionHandler.java
    │   │
    │   └── resources/
    │       ├── templates/
    │       │   ├── index.html
    │       │   ├── dashboard.html
    │       │   ├── groups.html
    │       │   ├── members.html
    │       │   ├── contributions.html
    │       │   ├── loans.html
    │       │   └── repayments.html
    │       │
    │       ├── static/
    │       │   ├── css/
    │       │   │   └── style.css
    │       │   └── js/
    │       │       └── app.js
    │       │
    │       └── application.properties
    │
    └── test/
        └── java/
            └── com/microsave/
                └── MicroSaveApplicationTests.java
```

---

## 2. Database Schema & Entity Relationships

The relational model reflects real-world Self-Help Group operations:

```text
GROUP (shg_group)
  │
  │ 1 : N
  ▼
MEMBER (member)
  │
  ├──────────────► CONTRIBUTION (contribution)  [1 : N]
  │
  └──────────────► LOAN (loan)                  [1 : N]
                       │
                       │ 1 : N
                       ▼
                   REPAYMENT (repayment)
```

### Table Definitions

1. **`shg_group`**
   - `group_id` (BIGINT, PK, AUTO_INCREMENT)
   - `group_name` (VARCHAR, NOT NULL)
   - `created_date` (DATE)

2. **`member`**
   - `member_id` (BIGINT, PK, AUTO_INCREMENT)
   - `name` (VARCHAR, NOT NULL)
   - `phone` (VARCHAR)
   - `join_date` (DATE)
   - `group_id` (BIGINT, FK -> shg_group.group_id)

3. **`contribution`**
   - `contribution_id` (BIGINT, PK, AUTO_INCREMENT)
   - `amount` (DECIMAL(19,2), NOT NULL)
   - `contribution_date` (DATE, NOT NULL)
   - `member_id` (BIGINT, FK -> member.member_id)

4. **`loan`**
   - `loan_id` (BIGINT, PK, AUTO_INCREMENT)
   - `amount` (DECIMAL(19,2), NOT NULL)
   - `loan_date` (DATE, NOT NULL)
   - `status` (VARCHAR - `ACTIVE`, `PAID`)
   - `member_id` (BIGINT, FK -> member.member_id)

5. **`repayment`**
   - `repayment_id` (BIGINT, PK, AUTO_INCREMENT)
   - `amount` (DECIMAL(19,2), NOT NULL)
   - `repayment_date` (DATE, NOT NULL)
   - `loan_id` (BIGINT, FK -> loan.loan_id)

---

## 3. Core Business Rules (Service-Layer Enforced)

All core constraints are strictly enforced in the service layer before any database mutation:

1. **Rule 1 — Dynamic Available Pool Calculation (`GroupService`):**
   $$\text{Available Pool} = \text{Total Contributions} - \text{Outstanding Loans}$$
   $$\text{Outstanding Loan} = \text{Disbursed Amount} - \sum(\text{Repayments})$$
   Calculated on-the-fly dynamically from the database.

2. **Rule 2 — Single Active Loan Constraint (`LoanService`):**
   A member with an existing unpaid loan (`status = ACTIVE`) cannot obtain a new loan.
   - Throws: `BusinessRuleException("Member already has an active loan.")`

3. **Rule 3 — Group Pool Limit Enforcement (`LoanService`):**
   Loan disbursement cannot exceed the group's current available pool balance.
   - Throws: `BusinessRuleException("Insufficient group balance for this loan.")`

4. **Rule 4 — Repayment Limit & Status Transition (`RepaymentService`):**
   - Installments cannot exceed the loan's current outstanding balance.
   - When the cumulative repayments equal the loan amount, the loan status automatically flips from `ACTIVE` $\to$ `PAID`.
   - Once marked `PAID`, the borrower becomes eligible for new internal loans.

---

## 4. REST API Endpoints

| Method | Endpoint | Description | Request Body / Query |
| :--- | :--- | :--- | :--- |
| **POST** | `/api/groups` | Create a new SHG group | `GroupRequest` |
| **GET** | `/api/groups` | List all groups | - |
| **GET** | `/api/groups/{id}` | Get group details by ID | - |
| **GET** | `/api/groups/{id}/pool` | Get group pool summary | Returns `GroupPoolResponse` |
| **POST** | `/api/members` | Add a member to a group | `MemberRequest` |
| **GET** | `/api/members` | List all members | - |
| **GET** | `/api/members/{id}` | Get member by ID | - |
| **GET** | `/api/members/{id}/balance` | Member savings & loan balance | Returns `MemberBalanceResponse` |
| **POST** | `/api/contributions` | Record weekly/monthly savings | `ContributionRequest` |
| **GET** | `/api/contributions` | List all contributions | - |
| **GET** | `/api/contributions/member/{id}` | Get contributions of member | - |
| **POST** | `/api/loans` | Disburse an internal loan | `LoanRequest` |
| **GET** | `/api/loans` | List all loans | - |
| **GET** | `/api/loans/{id}` | Get loan details by ID | - |
| **GET** | `/api/loans/member/{id}` | Get loans for member | - |
| **POST** | `/api/repayments` | Record loan repayment installment | `RepaymentRequest` |
| **GET** | `/api/repayments` | List all repayment records | - |
| **GET** | `/api/repayments/loan/{id}` | List repayments for loan | - |

---

## 5. Web Frontend UI Pages

Rendered via Thymeleaf with pure responsive Vanilla CSS and asynchronous JavaScript `fetch()`:

- **Welcome Landing (`/` & `/index`)**: System overview, core SHG rules, and navigation hub.
- **Dashboard (`/dashboard`)**: Metric cards (Contributions, Outstanding Loans, Available Pool, Members, Active Loans), plus live recent contribution, active loan, and repayment transaction feeds.
- **Groups Page (`/groups`)**: Create groups and view live dynamic group pool breakdown.
- **Members Page (`/members`)**: Register members, list members with group affiliation, and check individual member balances.
- **Contributions Page (`/contributions`)**: Record member savings deposits.
- **Loans Page (`/loans`)**: Internal loan disbursement with interactive available group pool display and instant validation checks.
- **Repayments Page (`/repayments`)**: Log repayments with live outstanding debt calculation and automatic `PAID` transition.

---

## 6. How to Run the Application

### Prerequisites
- **JDK 17** or higher installed.
- **MySQL Server** installed and running on port `3306`.
- **Maven 3.8+** (or use the included `./mvnw.cmd` wrapper).

### MySQL Configuration
Verify credentials in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/microsave?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root
```
*(Ensure database user and password match your local MySQL configuration. The database `microsave` is created automatically if it doesn't exist).*

### Build and Launch
1. Open PowerShell / Command Prompt in the `microsave/` folder.
2. Compile and run:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
3. Open your browser and navigate to:
   - **Frontend App**: [http://localhost:8080](http://localhost:8080)
   - **Interactive Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
   - **OpenAPI v3 Docs**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 7. Complete Demonstration & Viva Workflow

Follow this sequence to demonstrate all assessment requirements:

1. **Create Group**: Navigate to `/groups`, create group `"Ooty Women's SHG"`.
2. **Add Members**: Navigate to `/members`, create members `"Anitha"` and `"Kavitha"`.
3. **Record Contributions**: Navigate to `/contributions`:
   - Anitha deposits ₹30,000.
   - Kavitha deposits ₹20,000.
   - Total Pool becomes ₹50,000.
4. **Inspect Group Pool**: On Dashboard or Groups page, verify Available Pool = ₹50,000.
5. **Disburse Valid Loan**: Navigate to `/loans`:
   - Disburse ₹20,000 to Anitha.
   - Pool decreases to ₹30,000 (Contributions ₹50,000 - Outstanding ₹20,000).
6. **Test Rule 2 (Second Active Loan)**:
   - Try requesting another loan for Anitha.
   - System immediately rejects: `❌ Member already has an active loan.`
7. **Test Rule 3 (Loan Exceeds Pool)**:
   - Try requesting a loan of ₹35,000 for Kavitha (Available pool is ₹30,000).
   - System immediately rejects: `❌ Insufficient group balance for this loan.`
8. **Record Partial Repayment**: Navigate to `/repayments`:
   - Select Anitha's loan (₹20,000).
   - Repay ₹5,000.
   - Outstanding drops to ₹15,000; Available Pool increases to ₹35,000.
9. **Record Final Repayment**:
   - Repay remaining ₹15,000.
   - Loan status automatically updates to `PAID`.
   - Anitha can now take a new loan if requested.
