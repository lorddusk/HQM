# Agent rules

These rules apply to any AI coding agent working in this repository.

## Git

- Do not run any git command that writes or changes state, except `git add` and creating a new local branch with a commit on it (see below). This includes `commit`, `push`, `rebase`, `merge`, `reset`, `checkout`/`switch` to another branch, `stash`, `tag`, `cherry-pick`, and anything that creates or edits pull requests.
- Read-only git commands (`status`, `log`, `diff`, `show`, `blame`, `branch --list`) are fine.
- `git add` is allowed, to stage files, for example to prepare a separate commit. Only stage the files that were asked for, and do not unstage or change anything already staged.
- Creating a new local branch is allowed, and so is committing the files that were asked for onto it. Do not switch the working tree to it, do not touch the current branch, and do not change or delete existing branches. Never push.
- Leave the changes in the working tree (staged or not). The developer decides what to commit and push.

## Scope

- Make only the changes that were explicitly requested. Do not refactor, reformat, rename, update dependencies, or "clean up" anything on the side.
- If you notice something else worth changing (a bug, outdated code, a missing test), propose it in your reply instead of doing it.
- If a request is ambiguous, ask or pick the smallest reasonable interpretation and say so.

## Code

- Code must be readable and understandable by humans. Prefer clear names and simple control flow over clever or compact code. Match the style of the surrounding code, and add a comment only where the reason for something is not obvious from the code.
- Code must be as efficient as possible in game ticks and frames. Minecraft is slow enough, so every cost in code that runs per tick, per frame, per packet or per block event counts. In those paths avoid allocations (new collections, streams, lambdas, string building), repeated lookups and work that can be cached or done once. Do the expensive work only when something changed.
- Readability comes first where the code runs rarely (loading, commands, setup). Do not make that code harder to read for a gain nobody can measure.
- These rules apply to the code you write. If existing code you did not touch breaks them, propose the change in your reply (see Scope).

## Review

- The developer reviews every change before it is accepted. Keep changes small and easy to review, and state clearly what you changed and where.
- Report anything you could not verify, or any assumption you made, instead of presenting it as fact.
