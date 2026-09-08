# PS01 — Intelligent Pull Request Reviewer

## The Scenario

Your team just received a PR to merge a new **payment transfer feature** into the main branch of the bank's core payments service.
The author built it under deadline pressure. Your tech lead is on leave. You've been asked to approve it before the end-of-day deployment window.

**Your job:** use Bob to perform the review a senior engineer would do.

---

## Before You Start

- Open [IBM Bob](https://ibmbob.ai) in your browser
- Make sure you are in **Agent mode** (check the mode selector in the top bar)
- Have this README open alongside Bob so you can follow the steps

---

## Step 1 — Set the context

Open a new Bob conversation and paste this as your **first message**:

```
You are a senior engineer reviewing a pull request for the bank's core payments service before it merges to production in a regulated financial environment.

I will paste the changed files one by one. After I have pasted all of them, perform a structured code review. Do not review yet — just acknowledge and wait.
```

---

## Step 2 — Paste the four changed files

Copy the content of each file below and paste it into the same Bob conversation, one at a time. After each paste just say **"next file coming"** so Bob holds its review.

**File 1 — Core business logic:**
`src/main/java/com/bank/payments/PaymentService.java`

**File 2 — REST controller:**
`src/main/java/com/bank/payments/PaymentController.java`

**File 3 — Data access layer:**
`src/main/java/com/bank/payments/PaymentRepository.java`

**File 4 — Request model:**
`src/main/java/com/bank/payments/TransferRequest.java`

After pasting the last file, say:
```
That's all four files. Now perform the full review.
```

---

## Step 3 — Ask for a prioritised finding report

Once Bob has reviewed the code, send this prompt:

```
Produce a structured finding report. For each issue include:
- Severity: Critical / High / Medium / Low
- File and line reference
- Plain-English explanation of the problem
- Why it matters in a regulated banking environment
- A concrete suggested fix

Sort the findings from Critical down to Low.
```

**You should expect Bob to find at least 3 distinct issues across different severity levels.**
If Bob misses something obvious, prompt it: *"Are there any security issues in the data access layer?"*

---

## Step 4 — Ask Bob to fix the most critical issue

After the report is produced, send:

```
Take the most critical issue you identified. Regenerate the complete corrected version of the file it appears in, applying your suggested fix. Show the full file, not just the changed lines.
```

---

## What to Expect Bob to Find

The code contains real, intentionally hidden issues across multiple severity levels. You are not told what they are — that is the point of the exercise. Bob should surface them.

As a guide, there are issues spanning:
- **Security** — something that would let an attacker manipulate the database
- **Secrets** — something that should never be in source code
- **Logic** — a business rule that is implemented incorrectly, causing valid transactions to be rejected
- **Data integrity** — two operations that should be atomic but aren't
- **Observability** — something that leaks internal system detail to the caller
- **Access control** — something missing from the HTTP layer

---

## Deliverable for the Demo

Walk through your Bob conversation live and show:
1. The structured finding report with at least 3 findings at different severity levels
2. The corrected file Bob generated for the most critical issue
3. One finding you found most interesting and why Bob's explanation was useful
