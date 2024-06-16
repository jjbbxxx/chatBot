package com.xunfei.domain;

/**
 * 对话记录
 *
 * @author Linzj
 * @date 2023/10/20/020 11:10
 */
public class RoleContent {

    /**
     * 角色
     */
    String role;

    /**
     * 内容
     */
    String content;

    public RoleContent() {
        // 无参数构造函数
    }

    public RoleContent(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}