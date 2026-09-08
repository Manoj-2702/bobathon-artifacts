# PS03 — Legacy Codebase Onboarding Accelerator

## The Scenario

You've just joined a bank's engineering team. On day one your manager points you at this module and says:
*"The original developer left in 2014. There's no documentation. We need you to understand it and be able to make changes safely."*

The module is `com.fincore.engine` — a financial calculation and account management engine. You have the source code and nothing else.

**Your job:** use Bob to build enough understanding to be productive and safe on day one.

---

## Before You Start

- Open [IBM Bob](https://ibmbob.ai) in your browser
- Make sure you are in **Agent mode**
- Have this README open alongside Bob so you can follow the steps

---

## Step 1 — Set the context

Open a new Bob conversation and paste this as your **first message**:

```
You are a senior engineer documenting a legacy module for a junior developer joining
the team today who has no prior context about this codebase. The original developer
left years ago and there is zero documentation.

I will paste the source files one by one. After I paste all of them, produce
documentation that would make a new developer productive and safe on day one.
Do not start yet — just acknowledge and wait.
```

---

## Step 2 — Paste the source files

Copy the content of each file and paste it into Bob one at a time, saying **"next file coming"** between each.

**File 1 — Core calculation engine:**
`src/main/java/com/fincore/engine/LoanCalcEngine.java`

**File 2 — Account manager:**
`src/main/java/com/fincore/engine/AcctMgr.java`

**File 3 — Report generator:**
`src/main/java/com/fincore/engine/RptGen.java`

**File 4 — Utility class:**
`src/main/java/com/fincore/engine/Util.java`

After pasting the last file, say:
```
That's all four files. Now produce the documentation.
```

---

## Step 3 — Ask for a module README

Once Bob responds, send:

```
Generate a README for this module that includes:
1. What this module does in plain English — what business function does it serve?
2. A description of each class and its role
3. How the classes interact with each other — describe the data flow
4. What the key methods do (without just restating the code)
5. Any areas you would flag as risky or fragile for a new developer
```

---

## Step 4 — Ask for a risk map

After the README, send:

```
Now produce a risk map. Identify every part of this codebase that is dangerous to
modify without a deep understanding of the system. For each risky area:
- Name the class and method
- Explain exactly what makes it dangerous
- Describe what could go wrong if a developer changes it without understanding it
```

---

## Step 5 — Ask for a "first change" guide

Finally, send:

```
A developer's first task is to add a new loan type that uses flat-rate interest
(fixed monthly payment equal to loan amount divided by number of months, no interest
component). Write a step-by-step guide for where to make this change safely, what to
watch out for, and what to test before committing.
```

---

## What to Expect

The code is intentionally obfuscated — cryptic method names (`proc1`, `calcX`, `doIt`, `run`), magic numbers with no explanation, shared mutable static state, methods that mix concerns. Bob should:

- Correctly identify that `LoanCalcEngine` is an amortization/loan calculation engine
- Identify that `AcctMgr` manages account lifecycle operations (open, close, freeze, dormant)
- Spot the risky shared static state (`_acc`, `_pacc`, `_cyc`) in `LoanCalcEngine`
- Flag the magic integer dispatch in `AcctMgr.process(int type)`
- Notice the SQL injection risk in `RptGen`

---

## Deliverable for the Demo

Walk through your Bob conversation live and show:
1. The generated README — can a new developer understand this module from it?
2. The risk map — did Bob identify the genuinely dangerous parts?
3. The first-change guide — is it specific and actionable?
