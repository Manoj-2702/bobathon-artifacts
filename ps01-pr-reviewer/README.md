# PS01 — Intelligent Pull Request Reviewer

## The Problem

Your team merges dozens of pull requests every day. Code reviews are inconsistent — senior developers catch different things, junior developers miss security gaps, and no one has time to review everything thoroughly. Bugs and vulnerabilities slip into production because review quality depends entirely on who is available that day.

**Your task:** Build an automated PR reviewer tool using Bob that enforces quality gates before code reaches `main`.

---

## What You Are Building

A working tool (Python script, Node.js CLI, or any language you prefer) that:

1. **Takes code as input** — one or more source files, or a diff
2. **Sends it to an LLM** with a carefully engineered review prompt
3. **Outputs a structured finding report** — each issue with a severity level (Critical / High / Medium / Low), a plain-English explanation, and a concrete fix suggestion

---

## The Codebase to Review

A sample payment transfer service is provided in `src/`. This is what your tool will run against.

```
src/main/java/com/bank/payments/
├── PaymentService.java       ← core business logic
├── PaymentController.java    ← REST endpoint
├── PaymentRepository.java    ← JDBC data layer
└── TransferRequest.java      ← request model
```

This code is **intentionally flawed** — it contains real bugs, security vulnerabilities, and code quality issues hidden across the files. Your tool should surface them. You are not told what they are — that is the point of the exercise.

---

## How to Use Bob to Build This

### Step 1 — Design the tool with Bob

Open Bob in **Agent mode** and describe what you want to build:

```
I want to build a Python CLI tool that:
1. Reads Java source files from a directory
2. Sends them to an LLM API with a code review prompt
3. Parses the response and prints a structured finding report sorted by severity

Help me design the structure of this tool — what functions do I need,
what should the main flow look like, and which LLM library should I use?
```

---

### Step 2 — Build the LLM call with Bob

Once you have a structure, ask Bob to write the core LLM integration:

```
Write the Python function that sends a system prompt and the file contents
to [your chosen LLM — e.g. OpenAI GPT-4o / watsonx / Claude] and returns
the raw response. Show me how to handle the API key securely using an
environment variable, not hardcoded in the source.
```

---

### Step 3 — Engineer the review prompt with Bob

This is the most important step — the quality of your tool depends entirely on the prompt. Ask Bob:

```
Write a system prompt for an LLM that will review Java pull requests for a
regulated banking application. The reviewer should behave like a senior
engineer. The output must be a JSON array where each finding has:
  - severity (Critical / High / Medium / Low)
  - file name
  - approximate line number
  - title
  - plain-English explanation of the problem
  - a concrete code-level fix

The model should look specifically for: SQL injection, hardcoded secrets,
missing input validation, broken business logic, improper exception handling,
information leakage, missing authentication, and non-atomic database operations.
Sort findings Critical first.
```

---

### Step 4 — Test against the sample codebase

Run your tool against the payment service in `src/` and check what it finds:

```bash
python pr_reviewer.py --dir src/main/java/com/bank/payments/
```

If the findings are vague or miss obvious issues, go back to Bob and iterate the prompt:

```
My tool reviewed the code but the findings are too generic. Here is my current
system prompt: [paste it]. Here is what it returned: [paste output].

Help me improve the prompt so findings are more specific — with exact file names,
line references, and actionable fixes rather than general advice.
```

---

### Step 5 — Ask Bob to generate the fix

Once your tool produces a good finding report, pick the most critical issue and ask Bob to fix it:

```
Based on this finding: [paste the Critical finding from your tool's output]

Regenerate the complete corrected version of [filename] with this issue fixed.
Show the full file. Then explain what you changed and why it matters in a
production banking environment.
```

---

## What Your Tool Should Find

The sample codebase contains issues across at least 4 severity levels. As a guide — without giving them away — there are problems related to:

- Something that lets an attacker manipulate the database directly
- Credentials that should never appear in source code
- A business rule that is implemented incorrectly, silently rejecting valid transactions
- Two database operations that should be atomic but aren't
- Internal system details being leaked to the HTTP caller
- A missing access control check on a financial endpoint

A well-engineered tool with a good prompt should surface all of these.

---

## Deliverable for the Demo

Walk through your Bob conversation and working tool live. Show:

1. **The tool running** — `python pr_reviewer.py --dir src/...` producing output
2. **The finding report** — at least 3 findings at different severity levels, each with a clear explanation and fix suggestion
3. **One prompt iteration** — show a before/after of how you improved the prompt using Bob to get better results
4. **The corrected file** — the fixed version of the most critical file, generated by Bob

---

## Tips

- Start with a simple script that just prints the LLM's raw response — get the API call working before worrying about formatting
- Temperature `0.1`–`0.2` gives more consistent, deterministic findings than higher values
- Ask Bob to add a summary table at the top of the report showing finding counts per severity
- If you have time, extend the tool to accept a `--diff` flag and review a git diff instead of full files
