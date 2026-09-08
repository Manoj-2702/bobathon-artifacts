# PS04 — Java 8 → Java 21 Migration Planner & Refactorer

## The Scenario

You are the lead engineer on a banking team that has been told to upgrade the customer account service from Java 8 to Java 21. Java 8 is end-of-life. The team has no experience with the new language features introduced between Java 9 and Java 21.

Before the team can upgrade, you need: a migration assessment, a refactored version of the code, and an annotated explanation of every change — suitable for a knowledge-sharing session with the team.

**Your job:** use Bob in Java Modernisation mode to analyse, refactor, and explain the migration.

---

## Before You Start

- Open [IBM Bob](https://ibmbob.ai) in your browser
- **Switch to Java Modernisation mode** — click the mode selector at the top and choose `Java Modernisation`
- Have this README open alongside Bob so you can follow the steps

> ⚠️ **Important:** Java Modernisation mode must be active before you start. This mode has specific knowledge of Java version incompatibilities and migration patterns. Using Agent mode will give you a weaker result.

---

## Step 1 — Set the context

Open a new Bob conversation (in Java Modernisation mode) and paste this as your **first message**:

```
I have a Java 8 customer account service that needs to be upgraded to Java 21.
I will paste the source files one by one. After I have pasted all of them:
1. Produce a migration assessment listing every Java 8 pattern that should be modernised
2. For each pattern, identify the Java 21 feature that replaces it
Do not start yet — acknowledge and wait for the files.
```

---

## Step 2 — Paste the source files

Copy and paste each file into the conversation, saying **"next file"** between each one.

**File 1 — Core service (most legacy patterns are here):**
`src/main/java/com/bank/account/CustomerAccountService.java`

**File 2 — Account model:**
`src/main/java/com/bank/account/Account.java`

**File 3 — Result wrapper:**
`src/main/java/com/bank/account/AccountResult.java`

**File 4 — Account type enum:**
`src/main/java/com/bank/account/AccountType.java`

**File 5 — Notification service:**
`src/main/java/com/bank/account/NotificationService.java`

After the last file, say:
```
That's all five files. Now produce the migration assessment.
```

---

## Step 3 — Ask for the refactored Java 21 version

Once you have the assessment, send:

```
Now generate the fully refactored Java 21 version of each file. Apply every
modernisation you identified:
- Records (where applicable)
- Pattern matching instanceof
- Sealed classes + record variants (for AccountResult)
- Enhanced switch expressions
- Virtual Threads (Thread.ofVirtual())
- Optional instead of null returns
- java.time API instead of java.util.Date / Calendar
- Lambdas and streams instead of anonymous inner classes and raw loops
- try-with-resources
- StringBuilder instead of StringBuffer

Generate each file in full — not just the changed parts.
```

---

## Step 4 — Ask for the annotated comparison

After receiving the refactored code, send:

```
Now produce an annotated before/after comparison for the 5 most impactful changes.
For each one:
- Show the Java 8 code snippet (before)
- Show the Java 21 code snippet (after)
- Write a plain-English explanation of what changed and why the Java 21 version
  is better — as if you were presenting this at a team tech talk
```

This is the output you will demo to the room.

---

## What the Code Contains

The Java 8 files contain these migration targets (Bob should find all of them):

| Pattern | Where |
|---------|-------|
| `java.util.Date` + `Calendar` | `CustomerAccountService`, `Account` |
| Raw types (`List`, `ArrayList` with no type parameter) | `CustomerAccountService` |
| Anonymous `Comparator` inner class | `CustomerAccountService.getSortedAccountsByBalance()` |
| Manual null checks instead of `Optional` | `CustomerAccountService.findAccountByNumber()` |
| `new Thread(new Runnable() { ... })` | `CustomerAccountService.openAccount()`, all of `NotificationService` |
| Verbose try-catch-finally for resource cleanup | `CustomerAccountService.loadAccountFromDatabase()` |
| `StringBuffer` in single-threaded context | `NotificationService`, `CustomerAccountService` |
| `Hashtable` + `Enumeration` | `CustomerAccountService.logActiveSessions()` |
| `instanceof` + explicit cast | `CustomerAccountService.processAccountEvent()` |
| Manual POJO boilerplate (equals/hashCode/toString) | `Account`, `AccountResult` |

---

## Deliverable for the Demo

Walk through your Bob conversation live and show:
1. The migration assessment — how many patterns did Bob identify?
2. One refactored file side by side with the original
3. The annotated before/after comparison for at least 2 changes, explained as a tech talk
