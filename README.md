# IBM Bob Hackathon — Sample Codebases

**Event:** IBM Bob Hackathon · Financial & Enterprise Clients
**Duration:** 3 Hours · SDLC & Java Modernisation Tracks

This repository contains five ready-to-use sample codebases — one per problem statement. You do **not** need to write any code. Clone this repo, open the folder for your assigned problem statement, and follow the step-by-step README inside.

---

## How to Get Started (2 minutes)

1. **Clone this repo**
   ```bash
   git clone https://github.com/Manoj-2702/bobathon-sample-codebases.git
   cd bobathon-sample-codebases
   ```

2. **Open the folder for your assigned problem statement** (see table below)

3. **Read the `README.md` inside that folder** — it has the exact prompts to paste into Bob, step by step

4. **Open [IBM Bob](https://ibmbob.ai)** and select the correct mode (Agent or Java Modernisation — the README will tell you)

5. **Paste, prompt, iterate** — you have 3 hours

---

## Folder Map

| Folder | Problem Statement | Bob Mode |
|--------|-----------------|----------|
| [`ps01-pr-reviewer/`](ps01-pr-reviewer/) | PS01 · Intelligent Pull Request Reviewer | **Agent** |
| [`ps02-test-coverage/`](ps02-test-coverage/) | PS02 · Requirement-to-Test Coverage Generator | **Agent** |
| [`ps03-legacy-onboarding/`](ps03-legacy-onboarding/) | PS03 · Legacy Codebase Onboarding Accelerator | **Agent** |
| [`ps04-java-modernisation/`](ps04-java-modernisation/) | PS04 · Java 8 → Java 21 Migration Planner | **Java Modernisation** |
| [`ps05-websphere-liberty/`](ps05-websphere-liberty/) | PS05 · WebSphere to Open Liberty Re-platforming | **Java Modernisation** |

---

## Problem Statement Summaries

### PS01 — Intelligent Pull Request Reviewer
**Folder:** `ps01-pr-reviewer/`  
A payment transfer service that has just been submitted as a pull request. It contains SQL injection, hardcoded credentials, missing input validation, a logic bug in the balance check, and information leakage through stack traces. Use Bob to perform a structured senior-engineer code review and produce a prioritised finding report.

**Key files to review with Bob:**
- `src/main/java/com/bank/payments/PaymentService.java`
- `src/main/java/com/bank/payments/PaymentRepository.java`
- `src/main/java/com/bank/payments/PaymentController.java`

---

### PS02 — Requirement-to-Test Coverage Generator
**Folder:** `ps02-test-coverage/`  
A clean banking transfer and interest calculation service. The value is in the **business requirements** documented in the README — copy them into Bob and ask it to extract all testable conditions and generate a full JUnit 5 test suite skeleton.

**Key files:**
- `README.md` — contains the business requirements to paste into Bob
- `src/main/java/com/bank/transfers/TransferService.java`
- `src/main/java/com/bank/transfers/InterestCalculationService.java`
- `src/main/java/com/bank/transfers/FraudFlagService.java`

---

### PS03 — Legacy Codebase Onboarding Accelerator
**Folder:** `ps03-legacy-onboarding/`  
A deliberately obfuscated legacy Java module — cryptic method names, magic numbers, tangled dependencies, no documentation. There is intentionally almost no README. Paste the source files into Bob and ask it to explain the code, generate documentation, and identify dangerous areas.

**Key files to paste into Bob:**
- `src/main/java/com/fincore/engine/LoanCalcEngine.java`
- `src/main/java/com/fincore/engine/AcctMgr.java`
- `src/main/java/com/fincore/engine/RptGen.java`

---

### PS04 — Java 8 → Java 21 Migration Planner & Refactorer
**Folder:** `ps04-java-modernisation/`  
A Java 8 customer account service that uses every major legacy pattern: `java.util.Date`, raw types, anonymous inner classes, manual null checks, `Thread`/`Runnable`, verbose try-catch-finally, `StringBuffer`, and manual POJO boilerplate. Switch Bob to **Java Modernisation mode** before starting.

**Key files to modernise:**
- `src/main/java/com/bank/account/CustomerAccountService.java`
- `src/main/java/com/bank/account/Account.java`
- `src/main/java/com/bank/account/AccountResult.java`
- `src/main/java/com/bank/account/NotificationService.java`

---

### PS05 — WebSphere to Open Liberty Re-platforming Guide
**Folder:** `ps05-websphere-liberty/`  
A WebSphere 8.5 trade settlement application with EJB session beans, WebSphere-proprietary APIs (`WSSubject`, `FFDCFilter`, `DistributedMap`), WAS-specific JNDI bindings, and proprietary XML binding files (`ibm-web-bnd.xml`, `ibm-ejb-jar-bnd.xml`). Switch Bob to **Java Modernisation mode** and ask it to identify every migration blocker.

**Key files to analyse:**
- `src/main/java/com/bank/settlement/TradeSettlementBean.java`
- `src/main/java/com/bank/settlement/TradeRepository.java`
- `src/main/webapp/WEB-INF/ibm-web-bnd.xml`
- `src/main/webapp/WEB-INF/ibm-ejb-jar-bnd.xml`
- `src/main/resources/was-config/datasource.xml`

---

## Bob Prompts Cheat Sheet

| PS | Starter Prompt |
|----|----------------|
| PS01 | *"Review this code as a senior engineer would before merging to production in a regulated financial environment. Produce a prioritised finding report with severity levels and concrete fixes."* |
| PS02 | *"Think like a QA engineer at a bank who has to defend test coverage to an auditor. Extract all testable conditions from these requirements and generate a JUnit 5 test suite skeleton with at least 8 test cases."* |
| PS03 | *"You are a senior engineer documenting this for a junior developer joining the team today who has no prior context. Write documentation that would make them productive and safe."* |
| PS04 | *"Produce a migration assessment for this Java 8 code, then generate the fully refactored Java 21 version. Provide a before/after comparison with a plain-English annotation for each change, as if preparing a team tech talk."* |
| PS05 | *"Act as a WebSphere-to-Liberty migration architect. Identify every WAS-specific construct that will not work on Open Liberty out of the box, produce the Liberty-compatible replacement for each, and generate a prioritised migration checklist."* |

---

*Made with IBM Bob*
