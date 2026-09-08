# PS02 — Requirement-to-Test Coverage Generator

## The Problem

Business analysts write requirements. Developers build features. QA engineers write test cases — from scratch, manually, often late. The result is patchy test coverage, missed edge cases, and production defects that were entirely predictable from the original requirement. In regulated industries like banking, missing a boundary condition isn't just a bug — it can be a compliance failure.

**Your task:** Build a tool using Bob that takes a business requirement as input and automatically generates a complete test suite skeleton — covering happy paths, negative cases, boundary values, and compliance edge cases.

---

## What You Are Building

A working tool (Python script, Node.js CLI, or any language you prefer) that:

1. **Takes a business requirement as input** — plain English text, pasted or read from a file
2. **Sends it to an LLM** with a prompt engineered to think like an auditor-facing QA engineer
3. **Outputs a JUnit 5 test suite skeleton** — with descriptive test method names, inline comments, and `TODO` bodies ready for implementation

---

## The Requirements to Test Against

A sample banking transfer and interest calculation service is provided in `src/`. Use these four business requirements as your tool's test input:

```
Requirement 1 — Domestic Transfer Limits
A customer cannot transfer more than their available balance in a single transaction.
Overdraft is not permitted. The minimum transfer amount is £0.01 and the maximum
single transfer is £250,000.

Requirement 2 — Savings Interest Calculation
Interest must compound daily for savings accounts with a balance above £10,000.
Accounts at or below £10,000 receive simple monthly interest. The annual interest
rate is 2.5% for standard savings and 3.8% for premium savings accounts.

Requirement 3 — International Transfer Fees
International transfers require additional SWIFT validation and are subject to a
flat fee of £25 plus 0.5% of the transfer amount. The fee is deducted from the
source account before the transfer is initiated.

Requirement 4 — Fraud Flagging Rules
A transfer is automatically flagged for manual review if: the destination account
has received more than 3 transfers in the past 24 hours, OR the transfer amount
exceeds 80% of the sender's average monthly balance.
```

The implementation of these requirements lives in `src/main/java/com/bank/transfers/`. Your tool's output should map directly to this code.

---

## How to Use Bob to Build This

### Step 1 — Design the tool with Bob

Open Bob in **Agent mode** and describe what you want to build:

```
I want to build a Python CLI tool that:
1. Accepts a business requirement as a text file or stdin
2. Sends it to an LLM with a prompt that extracts all testable conditions
3. Generates a JUnit 5 test suite skeleton in Java with descriptive method names,
   inline comments, and TODO bodies — ready to be filled in

Help me design the structure of this tool — what functions do I need and
what should the main flow look like?
```

---

### Step 2 — Build the LLM call with Bob

```
Write the Python function that sends a system prompt and a business requirement
to [your chosen LLM] and returns the raw response. Show me how to handle the
API key securely using an environment variable.
```

---

### Step 3 — Engineer the extraction prompt with Bob

This is the core of the tool — the prompt that turns a requirement into testable conditions. Ask Bob:

```
Write a system prompt for an LLM that acts as a senior QA engineer at a
regulated bank. Given a business requirement, it must:

1. Extract every testable condition, grouped into four categories:
   - Happy Path (what should work)
   - Negative Cases (what should be rejected)
   - Boundary Values (exact limits and thresholds)
   - Compliance / Edge Cases (regulatory and corner cases)

2. Generate a JUnit 5 test skeleton in Java with:
   - One test class per logical service (e.g. TransferServiceTest)
   - Descriptive method names like shouldRejectTransferWhenAmountExceedsBalance()
   - A one-line @DisplayName annotation explaining what the test validates
   - assertThrows where the test expects an exception
   - Empty TODO method bodies

The output must be valid Java code only — no markdown explanation, just the class.
Minimum 8 test methods. Cover at least 3 of the 4 categories above.
```

---

### Step 4 — Test your tool against the sample requirements

Run your tool against the four banking requirements above and check the output:

```bash
python test_generator.py --requirements requirements.txt --output TransferServiceTest.java
```

If the generated tests are too generic or miss obvious boundary cases, iterate the prompt with Bob:

```
My tool generated this test skeleton: [paste output]

The original requirement said the minimum transfer is £0.01 and maximum is £250,000
but the tests don't cover those exact boundary values. Help me improve the prompt
so the tool always generates tests for exact boundary values and off-by-one cases.
```

---

### Step 5 — Add requirement ambiguity detection

Extend your tool with a second LLM call that analyses the requirements for gaps. Ask Bob:

```
Add a second function to my tool that sends the same requirement to the LLM
and asks it to identify any ambiguities that would make the requirement
impossible to test precisely. For each ambiguity it should output:
- What is unclear
- Why it's a problem for testing
- A suggested rewrite that makes it testable
```

---

### Step 6 — Validate against the implementation

Optionally, paste the implementation files into Bob alongside the generated tests:

```
Here is the generated test skeleton: [paste tests]
Here is the actual implementation: [paste TransferService.java]

Are there any testable conditions in the requirements that the implementation
does not handle? Are there any generated tests that test behaviour the
implementation can't actually support?
```

The implementation is in:
- `src/main/java/com/bank/transfers/TransferService.java`
- `src/main/java/com/bank/transfers/InterestCalculationService.java`
- `src/main/java/com/bank/transfers/FraudFlagService.java`

---

## What a Good Output Looks Like

Your tool's output for the four requirements should include:
- At least **8 test methods** across the three service test classes
- Coverage of **all four categories** — happy path, negative, boundary, compliance
- Boundary tests at exact values: `£0.01`, `£250,000`, `£10,000` balance threshold, `80%` of monthly balance, `3 transfers in 24 hours`
- A **list of requirement ambiguities** with suggested rewrites (e.g. "what happens if averageMonthlyBalance is null or zero?")

---

## Deliverable for the Demo

Walk through your Bob conversation and working tool live. Show:

1. **The tool running** — feeding in a requirement and producing a test skeleton
2. **The generated tests** — at least 8 methods, clearly named, covering multiple categories
3. **One prompt iteration** — show how you improved the prompt using Bob to get better boundary coverage
4. **The ambiguity report** — requirements your tool flagged as untestable and how it suggested rewriting them

---

## Tips

- Run the tool against one requirement at a time first — it's easier to evaluate quality
- Ask Bob to make the LLM output a JSON intermediate (list of testable conditions) before generating Java — gives you more control over the final output
- If you have time, extend the tool to accept a GitHub issue URL or Jira ticket and extract the requirement automatically
