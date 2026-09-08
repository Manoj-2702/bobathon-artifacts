# PS03 — Legacy Codebase Onboarding Accelerator

## The Problem

Every organisation has that codebase — 10 years old, no documentation, original developers long gone. A new developer joining the team spends their first two weeks just trying to understand what the system does and where to make a change safely. In financial institutions, this onboarding risk is amplified because touching the wrong class can have downstream effects on calculations, reporting, or regulatory outputs.

Static documentation goes stale. What a new developer actually needs is someone they can *ask*.

**Your task:** Build an interactive onboarding agent — a conversational CLI where a new developer can ask plain-English questions about an unfamiliar codebase and get accurate, context-aware answers in real time.

---

## What You Are Building

A conversational CLI agent that:

1. **Loads a codebase at startup** — reads all source files from a directory into memory
2. **Maintains a conversation loop** — the developer types questions, the agent responds
3. **Answers questions about the code** — what a class does, what a method is for, whether something is safe to change, where to add new functionality
4. **Remembers the conversation** — earlier questions inform later answers (chat history / context window management)

Think of it as a senior engineer sitting next to the new developer, available to answer any question about the codebase at any time.

---

## The Codebase to Onboard Into

A deliberately obfuscated legacy Java module is provided in `src/`. It has:
- Cryptic method names (`proc1`, `calcX`, `doIt`, `run`, `applyAdj`)
- Magic numbers with no explanation (`0.0025`, `1.15`, `0.30`, `36`, `55`, `99`)
- Shared mutable static state that causes subtle bugs
- Methods that mix concerns — a single method does SQL, calculation, and formatting
- No comments, no Javadoc, no documentation of any kind

```
src/main/java/com/fincore/engine/
├── LoanCalcEngine.java
├── AcctMgr.java
├── RptGen.java
└── Util.java
```

Your agent should be able to answer questions like:
- *"What does LoanCalcEngine do?"*
- *"What does proc1() actually calculate?"*
- *"What is the magic number 0.0025 in calcX()?"*
- *"Is it safe for me to modify doTierCalc()?"*
- *"Where should I add a new flat-rate loan type?"*
- *"Why does AcctMgr.process() take an int instead of an enum?"*

---

## How to Use Bob to Build This

### Step 1 — Design the agent architecture with Bob

Open Bob in **Agent mode** and start here:

```
I want to build a conversational CLI agent in Python that helps new developers
understand an unfamiliar codebase. The agent should:

1. Load all .java files from a directory at startup
2. Run an interactive question-answer loop in the terminal
3. Send each question to an LLM along with the codebase content
4. Print the answer and wait for the next question
5. Maintain chat history so earlier questions give context to later ones

Help me design the architecture. What are the key components, how should
I structure the code, and how should I manage the codebase context?
```

---

### Step 2 — Build the codebase loader and context builder

```
Write the Python function that:
1. Reads all .java files recursively from a given directory
2. Formats them into a single context string the LLM can understand —
   each file clearly labelled with its name
3. Counts the total token size so I know if I'm close to the context limit

Also show me how to truncate or summarise if the codebase is too large
to fit in a single context window.
```

---

### Step 3 — Engineer the system prompt with Bob

The system prompt is what makes the agent behave like a knowledgeable senior engineer rather than a generic chatbot. Ask Bob:

```
Write a system prompt for an LLM that acts as a senior engineer who knows
this codebase inside out. The agent is helping a new developer who has
just joined the team.

The agent should:
- Answer questions about what classes and methods do in plain English
- Flag anything that is risky or dangerous to modify, and explain why
- Suggest safe entry points for making changes
- Be honest when something is unclear or ambiguous in the code
- Never make up functionality that isn't in the code

The full codebase will be included in the user context. The agent should
refer to specific class names, method names, and line numbers when answering.
```

---

### Step 4 — Build the conversation loop with Bob

```
Write the main conversation loop for my agent:
1. Print a welcome message explaining what codebase is loaded
2. Show a prompt like "> Ask anything about this codebase:"
3. Read the user's input
4. Append it to the chat history
5. Send the full history + codebase context to the LLM
6. Print the response
7. Loop back to step 2
8. Exit cleanly when the user types "exit" or "quit"

Show me how to maintain the chat history as a list of messages
so the LLM sees the full conversation context on each turn.
```

---

### Step 5 — Test against the legacy codebase

Run your agent against `src/main/java/com/fincore/engine/` and ask it the questions below. These are the benchmark questions your demo should answer well:

```bash
python onboarding_agent.py --dir src/main/java/com/fincore/engine/
```

**Benchmark questions to test:**
1. `What does this codebase do overall?`
2. `What does proc1() in LoanCalcEngine do?`
3. `What is the significance of 0.0025 in calcX()?`
4. `Is it safe to modify the static fields _acc, _pacc and _cyc?`
5. `What does AcctMgr.process(2) do versus process(3)?`
6. `Where would I add a new loan type that uses flat-rate interest?`
7. `What is the risk of calling getLoanBal() multiple times for the same account?`

If the answers are vague or wrong, go back to Bob and improve the system prompt or context formatting.

---

### Step 6 — Add memory and improve the agent with Bob

Once the basic loop works, ask Bob to make it smarter:

```
My agent works but it loses track of what we discussed earlier. After a few
questions it seems to forget the context. Help me:

1. Implement a sliding window — keep only the last N messages in history
   to avoid exceeding the context limit
2. Add a "summarise so far" feature: when history gets long, summarise
   the earlier conversation into a single message and use that as the
   base context going forward
```

Also ask Bob to add useful agent commands:

```
Add these special commands to my agent's conversation loop:
- /files  — list all the files that were loaded
- /reset  — clear the chat history and start fresh
- /risk   — ask the agent to produce a risk map of the entire codebase
- /readme — ask the agent to generate a README for the codebase
```

---

## What a Good Agent Looks Like

Test your agent against all 7 benchmark questions. A well-built agent should:

- Correctly identify `LoanCalcEngine` as a loan amortization engine with penalty and early repayment calculation
- Explain that `proc1()` builds a payment schedule (amortization table) — type 1 = annuity, type 2 = declining balance, type 3 = interest-only
- Identify `0.0025` as a daily penalty rate (0.25% per day overdue)
- Flag the static fields `_acc`, `_pacc`, `_cyc` as dangerous shared state — not thread-safe, accumulates across instances
- Explain that `process(2)` freezes the account (status 3) and `process(3)` marks it dormant (status 4)
- Suggest `LoanCalcEngine.proc1()` and `doTierCalc()` as the right entry points for a new loan type
- Warn that `getLoanBal()` applies a silent 5% discount on repeated calls for the same account due to the cache bug

---

## Deliverable for the Demo

Run your agent live in the terminal and show:

1. **The agent starting up** — loading the codebase files, showing a welcome message
2. **Live Q&A** — ask at least 3 of the 7 benchmark questions and show the answers
3. **Conversation memory** — ask a follow-up question that references something from earlier in the session
4. **One improvement iteration** — show how you used Bob to improve the agent (better system prompt, sliding window, special commands, etc.)

---

## Tips

- Start with the simplest possible loop — one question, one answer, no history — and get it working before adding memory
- Ask Bob to help you print a "thinking..." indicator while waiting for the LLM response so the CLI feels responsive
- The legacy codebase is intentionally hard — if your agent answers the benchmark questions well, it's genuinely useful
- If you have time, ask Bob to add a `--diff` mode: the developer pastes a code change and the agent assesses whether it's safe to make given what it knows about the codebase
