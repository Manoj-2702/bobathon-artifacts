# IBM Bob Hackathon — Sample Codebases

**Event:** IBM Bob Hackathon · Financial & Enterprise Clients
**Duration:** 3 Hours · SDLC & Java Modernisation Tracks

This repository contains five ready-to-use sample codebases — one per problem statement. Clone this repo, open the folder for your assigned problem statement, and follow the step-by-step README inside.

---

## How to Get Started (2 minutes)

1. **Clone this repo**
   ```bash
   git clone https://github.com/Manoj-2702/bobathon-artifacts.git
   cd bobathon-artifacts
   ```

2. **Open the folder for your assigned problem statement** (see table below)

3. **Read the `README.md` inside that folder** — it tells you exactly what to build and how to use Bob to build it

4. **Open [IBM Bob](https://ibmbob.ai)** and select the correct mode (Agent or Java Modernisation — the README will tell you)

5. **Build, prompt, iterate** — you have 3 hours

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
**Build** a CLI tool that reads Java source files, sends them to an LLM with a structured review prompt, and outputs a severity-sorted finding report. Test it against the intentionally flawed payment transfer service in `src/` — which contains SQL injection, hardcoded credentials, a logic bug, missing auth, and more.

**Key files:**
- `README.md` — what to build and how to use Bob to build it
- `src/main/java/com/bank/payments/` — the flawed codebase your tool reviews

---

### PS02 — Requirement-to-Test Coverage Generator
**Folder:** `ps02-test-coverage/`
**Build** a CLI tool that takes a plain-English business requirement as input, sends it to an LLM, and outputs a JUnit 5 test suite skeleton covering happy paths, negative cases, boundary values, and compliance edge cases. Test it against four banking requirements provided in the README.

**Key files:**
- `README.md` — what to build, the sample requirements, and how to use Bob
- `src/main/java/com/bank/transfers/` — the implementation your generated tests map to

---

### PS03 — Legacy Codebase Onboarding Accelerator
**Folder:** `ps03-legacy-onboarding/`
**Build** an interactive conversational CLI agent — a new developer types plain-English questions about the codebase and gets accurate answers in real time. The agent loads source files at startup, maintains chat history, and behaves like a senior engineer who knows the codebase. Test it against the deliberately obfuscated legacy loan engine in `src/`.

**Key files:**
- `README.md` — what to build, 7 benchmark questions, and how to use Bob
- `src/main/java/com/fincore/engine/` — the mystery codebase your agent must understand

---

### PS04 — Java 8 → Java 21 Migration Planner & Refactorer
**Folder:** `ps04-java-modernisation/`
A Java 8 customer account service. Use Bob in **Java Modernisation mode** to analyse the code, produce a migration assessment, refactor each file to idiomatic Java 21, and explain every change as an annotated before/after — ready to present as a team tech talk.

**Key files:**
- `README.md` — step-by-step guide for the Bob session
- `src/main/java/com/bank/account/` — the Java 8 codebase to migrate

---

### PS05 — WebSphere to Open Liberty Re-platforming Guide
**Folder:** `ps05-websphere-liberty/`
A trade settlement service deployed on WebSphere Application Server 8.5. Use Bob in **Java Modernisation mode** to identify every WAS-specific construct that will break on Liberty, produce Liberty-compatible replacements for each one, and generate a sprint-by-sprint migration backlog.

**Key files:**
- `README.md` — step-by-step guide for the Bob session
- `src/main/java/com/bank/settlement/` — EJB service code with WAS-specific APIs
- `src/main/webapp/WEB-INF/` — proprietary WAS binding files
- `src/main/resources/was-config/` — WAS admin console datasource config

---

## Bob Starter Prompts

| PS | First thing to ask Bob |
|----|------------------------|
| PS01 | *"I want to build a Python CLI that reads Java files, sends them to an LLM, and outputs a severity-sorted code review report. Help me design the structure and write the LLM call."* |
| PS02 | *"I want to build a Python CLI that takes a business requirement as input and generates a JUnit 5 test suite skeleton. Help me design the tool and write the system prompt that extracts testable conditions."* |
| PS03 | *"I want to build a conversational CLI agent that loads a Java codebase at startup and answers developer questions about it. Help me design the architecture and write the system prompt."* |
| PS04 | *"Produce a migration assessment for this Java 8 code, then generate the fully refactored Java 21 version with a before/after annotation for each change, as if preparing a team tech talk."* |
| PS05 | *"Act as a WebSphere-to-Liberty migration architect. Identify every WAS-specific construct that will not work on Open Liberty, produce the Liberty-compatible replacement for each, and generate a prioritised migration checklist."* |

---

*Made with IBM Bob*
