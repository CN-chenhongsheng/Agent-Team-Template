---
name: git-commit-zh
description: Generates git commit messages in a required Chinese format with bullet points and a fixed author line. Use when the user asks to write or format git commit messages.
---

# Git Commit Message (Chinese)

## Required Format

Use this exact template:

```
feat: xxxx 中文
  -xxxx 中文
  -xxxx 中文
  -... 中文

提交人：陈鸿昇
```

## Rules
- Subject line starts with `feat:` followed by a short Chinese summary.
- Body has two bullet lines prefixed by two spaces and a hyphen.
- Always include the author line: `提交人：陈鸿昇`.
- Keep wording concise and consistent with the changes.
