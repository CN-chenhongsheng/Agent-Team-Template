---
name: team-start
description: Quickly start the full-stack development team with backend, manager frontend, and miniprogram frontend experts. Use when the user wants to launch the development team.
---

# Start Full-Stack Development Team

## What This Does

Automatically creates the `sushe-fullstack-team` with three specialized teammates:

1. **backend-java-expert** - Senior Backend Engineer
   - Uses `senior-backend-engineer` agent type
   - Handles Application backend development, API design, database optimization
   - Follows backend-java skill standards

2. **manager-frontend-expert** - Manager Frontend Developer
   - Uses `frontend-admin-dev` agent type
   - Handles manager project development, Vue 3 components, routing
   - Follows manager-frontend skill standards

3. **miniprogram-expert** - Miniprogram Frontend Developer
   - Uses `miniprogram-frontend-dev` agent type
   - Handles UniApp miniprogram development, glass-morphism design
   - Follows miniprogram-standards skill standards

## Usage

User simply types:
```
/team-start
```

## Implementation Steps

When this skill is invoked:

1. Create team with `TeamCreate`:
   - team_name: `sushe-fullstack-team`
   - description: `宿舍管理系统全栈开发团队，负责协调后端、管理后台前端和小程序前端的开发工作`

2. Spawn three teammates using `Task` tool in parallel:

   **Backend Expert:**
   - subagent_type: `senior-backend-engineer`
   - name: `backend-java-expert`
   - team_name: `sushe-fullstack-team`
   - model: `sonnet`
   - prompt: "你好！我是宿舍管理系统全栈开发团队的后端 Java 专家。我负责 Application 模块的 Java 后端开发、API 端点设计与实现、数据库优化和查询性能、业务逻辑和数据处理。我已准备好接受任务。技术栈：Java + Spring Boot + MySQL。我将严格遵循项目的 backend-java skill 标准。"

   **Manager Frontend Expert:**
   - subagent_type: `frontend-admin-dev`
   - name: `manager-frontend-expert`
   - team_name: `sushe-fullstack-team`
   - model: `sonnet`
   - prompt: "你好！我是宿舍管理系统全栈开发团队的管理后台前端专家。我负责 manager 项目 (Vue 3 管理后台) 的开发、页面和组件实现、API 集成和数据可视化。我已准备好接受任务。技术栈：Vue 3 + TypeScript + Element Plus + Tailwind CSS。我将严格遵循项目的 manager-frontend skill 标准。"

   **Miniprogram Expert:**
   - subagent_type: `miniprogram-frontend-dev`
   - name: `miniprogram-expert`
   - team_name: `sushe-fullstack-team`
   - model: `sonnet`
   - prompt: "你好！我是宿舍管理系统全栈开发团队的小程序前端专家。我负责 miniprogram 项目 (UniApp 微信小程序) 的开发、页面和组件实现、玻璃态设计系统。我已准备好接受任务。技术栈：UniApp + Vue 3 + UView Plus。我将严格遵循项目的 miniprogram-standards skill 标准。"

3. Confirm team is ready and display team status

## Output Message

After team is created, display:
```
🎉 全栈开发团队已启动！

✅ backend-java-expert (后端 Java 专家) - 就绪
✅ manager-frontend-expert (管理后台前端专家) - 就绪
✅ miniprogram-expert (小程序前端专家) - 就绪

你可以开始分配任务了！
```

## Cleanup

To cleanup the team later, user should run:
```
清理团队
```

Or manually delete team resources:
- Team config: `~/.Codex/teams/sushe-fullstack-team/`
- Task list: `~/.Codex/tasks/sushe-fullstack-team/`
