# Multi-Threaded Job Scheduler

An in-memory engine that runs tasks immediately, after a delay, repeatedly,
or by priority — the way a real backend queues background work. Built-in
pure Java, no frameworks or dependencies.

## How it works

- **Job** — a unit of work: an id, when it should next run, an optional
  repeat interval, a priority, and the actual work as a 'Runnable'.
- **JobScheduler** — the engine:
  - A `PriorityQueue<Job>` (min-heap) keyed by `nextRunTime` keeps the
    soonest job on top.
  - A dedicated dispatcher thread watches the heap and sleeps exactly until
    the next job is due, using `wait()` / `notifyAll()` — no busy-waiting.
  - Ready jobs are handed to a fixed thread pool so slow jobs never block
    the dispatcher or each other.
  - Failed jobs retry up to 3 times with exponential backoff (200ms, 400ms,
    800ms) before being logged as dropped.
  - Repeating jobs are automatically re-inserted into the heap after each run.

## How to run it

```bash
javac -d out src/scheduler/*.java
java -cp out scheduler.Main
```
**Main.java** demonstrates an immediate job, a delayed job, a repeating job,
a job that fails twice before succeeding (to show retry/backoff), and a
100-job load test with randomized delays.

## Load test results

Scheduled 100 jobs with randomized delays (0–5 seconds) across a 4-thread
pool. Result: **100/100 jobs completed**, zero lost or duplicated executions.

## Hardest design decision: protecting the heap across threads

The heap is accessed concurrently by whoever calls `schedule()` and by the
dispatcher thread reading/removing jobs. Since `PriorityQueue` isn't
thread-safe, every access is wrapped in `synchronized(this)`.

The tricky part was avoiding busy-waiting. The dispatcher uses
`wait()/wait(delay)` to sleep exactly until the next job is due, and
`schedule()` calls `notifyAll()` after adding a job — this matters when a
newly-added job is due *sooner* than whatever the dispatcher was already
sleeping on; without the wake-up, it would run late
