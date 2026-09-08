# PS05 — WebSphere to Open Liberty Re-platforming Guide

## The Scenario

You are a migration architect at a bank. The trade settlement service currently runs on WebSphere Application Server (WAS) 8.5. Leadership has approved re-platforming it to Open Liberty. You have been asked to produce a concrete, code-level migration guide — a blocker inventory and a sprint-by-sprint checklist the development team can act on.

**Your job:** use Bob in Java Modernisation mode to identify every WAS-specific construct that will break on Liberty, produce Liberty-compatible replacements, and generate a prioritised migration backlog.

---

## Before You Start

- Open [IBM Bob](https://ibmbob.ai) in your browser
- **Switch to Java Modernisation mode** — click the mode selector at the top and choose `Java Modernisation`
- Have this README open alongside Bob so you can follow the steps

> ⚠️ **Important:** Java Modernisation mode must be active. It has specific knowledge of WebSphere-to-Liberty migration patterns, Liberty feature names, and Jakarta EE API changes.

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

## What the Code Contains

The codebase has these WAS-specific constructs. Bob should identify all of them:

| Blocker | File | Why it's a problem |
|---------|------|-------------------|
| `WSSubject.getCallerSubject()` | `TradeSettlementBean.java` | WAS security API — no Liberty equivalent; use `SessionContext.getCallerPrincipal()` |
| `WSSecurityException` | `TradeSettlementBean.java` | WAS-only exception class |
| `FFDCFilter.processException()` | `TradeSettlementBean.java` (3 call sites) | WAS FFDC infrastructure — silently no-ops on Liberty |
| `ctx.lookup("jdbc/TradeDS")` without `java:comp/env` | `TradeSettlementBean.java` | WAS global JNDI extension — not portable |
| `@EJB(name = "java:comp/env/ejb/TradeRepo")` | `TradeSettlementBean.java` | Binding resolved via `ibm-ejb-jar-bnd.xml` — Liberty uses a different mechanism |
| `com.ibm.websphere.cache.DistributedMap` | `TradeRepository.java` | WAS Dynamic Cache API — replace with JCache (`javax.cache.Cache`) |
| `com.ibm.websphere.cache.EntryInfo` | `TradeRepository.java` | No JCache equivalent — TTL set differently |
| `ctx.lookup("services/cache/TradeCache")` | `TradeRepository.java` | WAS-specific cache JNDI name |
| `ibm-web-bnd.xml` | `WEB-INF/` | Proprietary — virtual host config moves to `server.xml` |
| `ibm-ejb-jar-bnd.xml` (ejblocal: scheme) | `WEB-INF/` | `ejblocal:` is WAS-only; Liberty uses `java:global/` names |
| `ibm-application-bnd.xml` (`ALL_AUTHENTICATED_USERS`) | `WEB-INF/` | WAS-only special subject |
| WAS admin console datasource XML | `was-config/datasource.xml` | Must be translated to Liberty `server.xml` `<dataSource>` stanza |

> **Note:** `Trade.java` is standard JPA 2.0 — it requires no changes and will work on Liberty as-is. This is intentional — it shows participants that not everything breaks.

---

## Deliverable for the Demo

Walk through your Bob conversation live and show:
1. The blocker inventory — how many WAS-specific constructs did Bob find?
2. The remediation guide — show one specific code fix (e.g. WSSubject → SessionContext)
3. The corrected `server.xml` snippet
4. The migration checklist — could the team pick this up as a sprint backlog?
