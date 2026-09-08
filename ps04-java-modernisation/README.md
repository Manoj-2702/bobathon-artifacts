# PS04 — Java 8 → Java 21 Migration Planner & Refactorer

## The Scenario

You are the lead engineer on a banking team that has been told to upgrade the customer account service from Java 8 to Java 21. Java 8 is end-of-life. The team has no experience with the new language features introduced between Java 9 and Java 21.

Before the team can upgrade, you need: a migration assessment, a refactored version of the code, and an annotated explanation of every change — suitable for a knowledge-sharing session with the team.

**Your job:** use Bob in Java Modernisation mode to analyse, refactor, and explain the migration.

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

> ⚠️ **Do not use Agent mode for this PS.** Java Modernisation mode has specific knowledge of Java version incompatibilities, removed APIs, and migration patterns. Agent mode will give significantly weaker results.

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
modernisation you identified, using the most idiomatic Java 21 approach for
each pattern you found. Generate each file in full — not just the changed parts.
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

## What to Expect

The Java 8 files contain a range of legacy patterns spanning date/time handling, type safety, concurrency, resource management, and class design. A thorough Bob session should identify at least **10 distinct modernisation opportunities** across the five files — with a Java 21 replacement for each.

The more patterns Bob finds and correctly maps to Java 21 equivalents, the better your migration assessment.

---

## Deliverable for the Demo

Walk through your Bob conversation live and show:
1. The migration assessment — how many patterns did Bob identify?
2. One refactored file side by side with the original
3. The annotated before/after comparison for at least 2 changes, explained as a tech talk
