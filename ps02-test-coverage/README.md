# PS02 — Requirement-to-Test Coverage Generator

## The Scenario

You are a QA engineer at a bank. A set of business requirements has just been handed to you for the fund transfer and savings interest subsystems. There are no existing tests. Your job is to generate a comprehensive JUnit 5 test suite from the requirements alone — one that can withstand scrutiny from a compliance auditor.

**Your job:** use Bob to extract every testable condition and generate a full test skeleton.

---

## Before You Start

- Open [IBM Bob](https://ibmbob.ai) in your browser
- Make sure you are in **Agent mode**
- Have this README open alongside Bob so you can follow the steps

---

## Step 1 — Set the context

Open a new Bob conversation and paste this as your **first message**:

```
You are a senior QA engineer at a bank. I am going to give you a set of business requirements.
Think like a QA engineer who has to defend test coverage to an auditor.
Do not generate any tests yet — just acknowledge and wait for the requirements.
```

---

## Step 2 — Paste the business requirements

Copy the block below exactly and paste it into Bob:

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

After pasting, say:
```
Those are all four requirements. Now extract every testable condition from them.
List them as bullet points grouped by: Happy Path, Negative Cases, Boundary Values,
and Compliance / Edge Cases.
```

---

## Step 3 — Generate the test suite skeleton

Once Bob has listed the testable conditions, send:

```
Now generate a complete JUnit 5 test suite skeleton in Java covering all the conditions
you identified. Use one test class per service:
- TransferServiceTest
- InterestCalculationServiceTest
- FraudFlagServiceTest

For each test method:
- Use a descriptive name that explains what is being tested (e.g. shouldRejectTransferWhenAmountExceedsBalance)
- Add a one-line comment explaining what the test validates
- Use @Test, @DisplayName, and assertThrows where appropriate
- Leave the test body as TODO — I will implement it

I want a minimum of 8 test methods total, covering at least 3 of the 4 categories above.
```

---

## Step 4 — Interrogate the requirements

After receiving the skeleton, send:

```
Now identify any ambiguities in the original requirements that would make them
difficult or impossible to test precisely. For each ambiguity, explain:
- What is unclear
- Why it is a problem for testing
- How you would rewrite that part of the requirement to make it testable
```

---

## Bonus Step — Paste the implementation for deeper analysis

If you want to go further, paste the implementation files and ask Bob to map each test method to the corresponding code path:

The implementation lives in:
- `src/main/java/com/bank/transfers/TransferService.java`
- `src/main/java/com/bank/transfers/InterestCalculationService.java`
- `src/main/java/com/bank/transfers/FraudFlagService.java`

Then ask:
```
Given this implementation, are there any testable conditions in the requirements
that the implementation does not currently handle? List any gaps.
```

---

## What to Expect

Bob should produce:
- At least 8 named test methods across the three test classes
- Coverage of happy path (transfer succeeds), negative cases (transfer rejected), boundary values (£0.01, £250,000, £10,000 balance threshold), and at least one fraud/compliance edge case
- A short list of requirement ambiguities with suggested rewrites

---

## Deliverable for the Demo

Walk through your Bob conversation live and show:
1. The list of testable conditions Bob extracted from the requirements
2. The test suite skeleton with at least 8 test methods
3. The requirement ambiguities Bob identified and how it suggested rewriting them
