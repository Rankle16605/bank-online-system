<div align="right">
</div>

<div align="center">
<br/>
<img src="docs/banner.png" alt="Smart Online Banking System" width="100%"/>
<br/>
<br/>
# 🏦 Smart Online Banking
### 🤖 AI-Powered · Frontend & Backend Separated · Full-Process Banking Practice
<br/>
<table>
<tr>
<td><img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"/></td>
<td><img src="https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/></td>
<td><img src="https://img.shields.io/badge/Vue-3.4-4FC08D?style=for-the-badge&logo=vuedotjs&logoColor=white"/></td>
<td><img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white"/></td>
<td><img src="https://img.shields.io/badge/License-MIT-C8102E?style=for-the-badge"/></td>
</tr>
</table>
</div>

---

## 1. 🔴 Project Overview
> 📌 **In one sentence**: A smart online banking system built on **Spring Boot 3 + Vue 3 + LLM**, enabling the digitalization of full-process banking services and an AI customer service.

This is a frontend-backend separated project that integrates a large language model through Spring AI, providing intelligent customer service, account management, fund transfer, bill inquiry, and loan management. The system contains **39 REST APIs and 16 frontend pages**, completed collaboratively by a 4-person team. I served as the team leader, responsible for architecture design, AI customer service development, and testing.

<div align="center">

| 🏗️ Backend Classes | 🌐 APIs | 🖥️ Pages | 👥 Team |
|:---:|:---:|:---:|:---:|
| 70+ | 39 | 16 | 4 |

</div>

> ⚠️ All data in this project is simulated and does not involve any real financial business.

---

## 2. 🟠 Tech Stack
The backend is developed in Java 17 on the Spring Boot 3.2.5 framework, using Spring Security 6 for authentication and access control. AI capabilities are powered by the Spring AI 1.0.0-M4 framework to integrate the large language model. The data layer uses MyBatis-Plus 3.5.6 to operate the MySQL 8.0 database and MinIO 8.5.10 for object storage. The frontend is built with Vue 3.4, the Ant Design Vue 4.2 component library, and Pinia 2.1 for state management, bundled with Vite 5.4, with ECharts 5.5 for data visualization. In addition, Knife4j 4.4.0 is used to generate API documentation, the backend is built with Maven 3.8+, and JUnit 5 is used for testing.

---

## 3. 🟡 System Architecture
```mermaid
graph TB
    A["🖥️ Vue Frontend<br/>Vite · Ant Design Vue"] --> B["🔐 Spring Security<br/>JWT Auth · RBAC"]
    B --> C["🎯 Controller Layer"]
    C --> D["⚙️ Service Layer"]
    D --> E[("💾 MySQL Database")]
    D --> F[("📦 MinIO Object Storage")]
    D --> G["🤖 LLM Service"]
    G --> H["💬 Customer Service Chat"]
    G --> I["🎨 Image Generation"]
    classDef front fill:#4FC08D,stroke:#2C8C69,color:#fff
    classDef sec fill:#FA541C,stroke:#AD350F,color:#fff
    classDef ctrl fill:#1890FF,stroke:#0B5FA5,color:#fff
    classDef svc fill:#722ED1,stroke:#4A1896,color:#fff
    classDef data fill:#2F54EB,stroke:#1D39A8,color:#fff
    classDef store fill:#13C2C2,stroke:#0C7E7E,color:#fff
    classDef ai fill:#C8102E,stroke:#8B0000,color:#fff
    classDef leaf fill:#EB2F96,stroke:#A31866,color:#fff
    class A front
    class B sec
    class C ctrl
    class D svc
    class E data
    class F store
    class G ai
    class H,I leaf
```
> The system follows a standard layered architecture: frontend JWT authentication → Controller layer → Service layer → Data layer. AI capabilities are encapsulated as independent services and decoupled from business modules.

---

## 4. 🟢 Functional Modules
<div align="center">

| No. | Module | Main Functions |
|:---:|---|---|
| 1 | User Management | Registration, login authentication, profile maintenance |
| 2 | Account Management | Account inquiry, recharge, account flows, balance management |
| 3 | Fund Transfer | Internal transfer, limit validation, transaction history |
| 4 | Bill Management | Automatic monthly bills, income and expenditure details |
| 5 | Loan Management | Loan application, approval, installment repayment |
| 6 | Check Management | Check issuance, cashing, voiding (admin) |
| 7 | AI Customer Service | Multi-session management, streaming chat, context memory, image generation |
| 8 | Admin Console | Data statistics, user/account management, loan approval |

</div>

---

## 5. 🔵 Project Structure
```mermaid
%%{init: {'flowchart': {'curve': 'linear', 'nodeSpacing': 45, 'rankSpacing': 90}, 'themeVariables': {'fontSize': '20px'}}}%%
flowchart TD
    ROOT["🏦 Smart Online Banking System"]
    ROOT --> BE["⚙️ Backend<br/>Spring Boot"]
    ROOT --> FE["🎨 Frontend<br/>Vue 3"]
    ROOT --> DB["🗃️ SQL Scripts"]
    ROOT --> DOC["📚 Docs"]
    BE --> B1["C<br/>t<br/>r<br/>l"]
    BE --> B2["L<br/>o<br/>g<br/>i<br/>c"]
    BE --> B3["D<br/>A<br/>O"]
    BE --> B4["S<br/>e<br/>c"]
    BE --> B5["C<br/>f<br/>g"]
    FE --> F1["V<br/>i<br/>e<br/>w<br/>s"]
    FE --> F2["C<br/>o<br/>m<br/>p<br/>s"]
    FE --> F3["S<br/>t<br/>o<br/>r<br/>e"]
    FE --> F4["R<br/>o<br/>u<br/>t<br/>e"]
    DB --> S1["S<br/>c<br/>h<br/>e<br/>m<br/>a"]
    DB --> S2["S<br/>e<br/>e<br/>d"]
    DB --> S3["U<br/>p<br/>g<br/>r<br/>a<br/>d<br/>e"]
    DOC --> D1["B<br/>a<br/>n<br/>n<br/>e<br/>r"]
    DOC --> D2["C<br/>o<br/>n<br/>f<br/>i<br/>g"]
    classDef root fill:#C8102E,stroke:#8B0000,color:#fff,font-weight:bold
    classDef be fill:#52C41A,stroke:#389E0D,color:#fff
    classDef fe fill:#13C2C2,stroke:#0C7E7E,color:#fff
    classDef db fill:#1890FF,stroke:#0B5FA5,color:#fff
    classDef doc fill:#722ED1,stroke:#4A1896,color:#fff
    classDef leaf1 fill:#F6FFED,stroke:#73D13D,color:#389E0D
    classDef leaf2 fill:#E6FFFB,stroke:#36CFC9,color:#0C7E7E
    classDef leaf3 fill:#E6F4FF,stroke:#40A9FF,color:#0B5FA5
    classDef leaf4 fill:#F9F0FF,stroke:#9254DE,color:#4A1896
    class ROOT root
    class BE be
    class FE fe
    class DB db
    class DOC doc
    class B1,B2,B3,B4,B5 leaf1
    class F1,F2,F3,F4 leaf2
    class S1,S2,S3 leaf3
    class D1,D2 leaf4
```

---

## 6. 🟣 Project Outcomes
1. ✅ Delivered **8 major functional modules**, closing the full loop of banking services, with 39 APIs and 16 pages;
2. ✅ The AI customer service supports **streaming output and multi-turn context memory**, with output safety ensured through system prompt constraints, sensitive-word filtering, and anti-fraud reminders;
3. ✅ Designed an **LLM fallback mechanism** that automatically switches to local replies when the model service is unavailable, keeping the system stable;
4. ✅ Fund transfer ensures data consistency through **database transactions and limit validation**;
5. ✅ The project passed functional, interface, and performance tests, and the **course design was rated Excellent**.

---

## 7. ⚫ Quick Start
**① Initialize the database**

Run the `init.sql` script in the `sql` directory.

**② Configure the application**

Set up the database connection and LLM API key in `application.yml` (environment variables are recommended; do not commit keys in plain text).

**③ Start the backend**
```bash
mvn spring-boot:run
```
**④ Start the frontend**
```bash
cd frontend
npm install
npm run dev
```
<div align="center">

| Entry | URL |
|---|---|
| 🖥️ Frontend | http://localhost:3000 |
| 📖 API Docs | http://localhost:8080/doc.html |
| ❤️ Health Check | http://localhost:8080/actuator/health |

</div>

---

## 8. Disclaimer
This project is intended solely for learning, exchange, and technical research. The institution names and business data involved are all simulated and are unrelated to any real organization.

<div align="center">
<sub>🚀 By BUAS</sub>
</div>
