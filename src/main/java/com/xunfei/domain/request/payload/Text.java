package com.xunfei.domain.request.payload;

/**
 * text 部分
 *
 * @author Linzj
 * @date 2023/10/20/020 10:20
 */
public class Text {

    /**
     * 取值为[user,assistant]
     * user表示是用户的问题，assistant表示AI的回复
     */
    private String role;

    /**
     * 所有content的累计tokens需控制8192以内
     * 用户和AI的对话内容
     */
    private String content;

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
