# PS05 — WebSphere to Open Liberty Re-platforming Guide

## The Scenario

You are a migration architect at a bank. The trade settlement service currently runs on WebSphere Application Server (WAS) 8.5. Leadership has approved re-platforming it to Open Liberty. You have been asked to produce a concrete, code-level migration guide — a blocker inventory and a sprint-by-sprint checklist the development team can act on.

**Your job:** use Bob in Java Modernisation mode to identify every WAS-specific construct that will break on Liberty, produce Liberty-compatible replacements, and generate a prioritised migration backlog.

---

## Before You Start

### 1. Import Java Modernisation mode (free trial only)

The free trial version of Bob does not include this mode by default. A config file is included in the root of this repo.

1. Open **IBM Bob** and click **Settings** in the top navigation
2. Go to the **Modes** section
3. Click the **Import** button (next to the **+** icon)
4. Select or paste the contents of [`java-modernization.yaml`](../java-modernization.yaml) from the repo root
5. **Save** — **"☕ Java Modernization"** will appear in your mode list

### 2. Activate the mode

- Click the mode selector and choose **☕ Java Modernization**
- Confirm it is active before pasting any code

> ⚠️ **Do not use Agent mode for this PS.** Java Modernisation mode has specific knowledge of WebSphere-to-Liberty migration patterns, Liberty feature names, and Jakarta EE API changes. Agent mode will give significantly weaker results.

---

## Step 1 — Set the context

Open a new Bob conversation (in Java Modernisation mode) and paste this as your **first message**:

```
Act as a WebSphere-to-Liberty migration architect.

I have a trade settlement service currently deployed on WebSphere Application Server 8.5.
I will paste the source files and configuration files one by one. After I paste all of them:
1. Identify every WAS-specific construct that will NOT work on Open Liberty out of the box
2. For each blocker, explain clearly why it is a blocker

Do not start yet — acknowledge and wait for the files.
```

---

## Step 2 — Paste the source and configuration files

Paste each file into the conversation, saying **"next file"** between each one.

**File 1 — Main EJB session bean (most blockers are here):**
`src/main/java/com/bank/settlement/TradeSettlementBean.java`

**File 2 — Repository EJB with WAS caching:**
`src/main/java/com/bank/settlement/TradeRepository.java`

**File 3 — JPA entity (for context — this one is clean):**
`src/main/java/com/bank/settlement/Trade.java`

**File 4 — WAS EJB binding file:**
`src/main/webapp/WEB-INF/ibm-ejb-jar-bnd.xml`

**File 5 — WAS web binding file:**
`src/main/webapp/WEB-INF/ibm-web-bnd.xml`

**File 6 — WAS application binding file:**
`src/main/webapp/WEB-INF/ibm-application-bnd.xml`

**File 7 — WAS datasource config:**
`src/main/resources/was-config/datasource.xml`

After the last file, say:
```
Those are all the files. Now identify every migration blocker.
```

---

## Step 3 — Ask for Liberty-compatible replacements

Once Bob has listed the blockers, send:

```
For each blocker you identified, produce the Liberty-compatible replacement. Include:
- The corrected Java code (where the blocker is in a .java file)
- The Liberty server.xml configuration stanza needed (datasource, security, EJB bindings)
- Any Jakarta EE 10 API changes required (e.g. javax.* → jakarta.* namespace)

Present this as a blocker-by-blocker remediation guide.
```

---

## Step 4 — Ask for the migration checklist

After the remediation guide, send:

```
Now produce a migration checklist ordered by effort from lowest to highest.
Format it as a sprint-by-sprint backlog a development team could follow.
For each item include:
- A one-line description of the task
- The effort level: Low / Medium / High
- Whether it can be done incrementally or requires a full cut-over
- Any dependencies on other tasks
```

---

## Bonus Step — Ask for the corrected server.xml

If you want a complete artifact to show, send:

```
Produce a complete Open Liberty server.xml for this application that includes:
- The correct features (ejbLite, jpa, jdbc, appSecurity, jndi)
- The datasource definition equivalent to what was in the WAS admin console
- The application security role bindings
- Any JNDI bindings needed to replace the ibm-ejb-jar-bnd.xml entries
```

---

## What to Expect

The codebase contains a mix of WAS-specific Java APIs, proprietary JNDI patterns, WAS-only XML binding files, and admin-console-managed configuration — spread across both the Java source files and the deployment descriptors.

A thorough Bob session in Java Modernisation mode should surface **at least 10 distinct blockers**, each with a clear explanation of why it fails on Liberty and a concrete replacement.

Not everything in the codebase needs to change — some constructs are standard Java EE and will carry across to Liberty without modification. Part of the exercise is recognising which is which.

---

## Deliverable for the Demo

Walk through your Bob conversation live and show:
1. The blocker inventory — how many WAS-specific constructs did Bob find?
2. The remediation guide — show one specific code fix (e.g. WSSubject → SessionContext)
3. The corrected `server.xml` snippet
4. The migration checklist — could the team pick this up as a sprint backlog?
