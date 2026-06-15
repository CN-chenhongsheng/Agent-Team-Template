---
name: team-cleanup
description: Gracefully shutdown all teammates and cleanup the sushe-fullstack-team resources. Use when the user wants to end the team session.
---

# Cleanup Full-Stack Development Team

## What This Does

Gracefully shuts down all three teammates and cleans up the `sushe-fullstack-team` resources:
1. Sends shutdown requests to all teammates
2. Waits for confirmation from each teammate
3. Deletes team configuration and task list

## Usage

User simply types:
```
/team-cleanup
```

## Implementation Steps

When this skill is invoked:

1. Send shutdown requests to all teammates using `SendMessage` tool:
   - Send to `backend-java-expert`
   - Send to `manager-frontend-expert`
   - Send to `miniprogram-expert`

   Message type: `shutdown_request`
   Content: "团队任务完成，准备关闭会话"

2. Wait for shutdown confirmations from all teammates

3. After all teammates have shut down, use `TeamDelete` tool to cleanup:
   - Removes `~/.claude/teams/sushe-fullstack-team/`
   - Removes `~/.claude/tasks/sushe-fullstack-team/`

## Important Notes

- MUST wait for all teammates to shut down before calling `TeamDelete`
- If a teammate rejects shutdown, ask user whether to force cleanup or keep working
- Only the team lead should run this cleanup (not teammates)

## Output Message

After cleanup is complete, display:
```
✅ 团队已成功清理

已关闭的队友：
- backend-java-expert
- manager-frontend-expert
- miniprogram-expert

团队资源已删除。
```

## Error Handling

If `TeamDelete` fails because teammates are still active:
1. List which teammates are still running
2. Ask user to confirm force cleanup or wait for teammates to finish
3. Retry after confirmation
