package com.xunfei.domain.request.payload;

/**
 * payload 部分
 *
 * @author Linzj
 * @date 2023/10/20/020 10:19
 */
public class Payload {

    /**
     * message 部分
     */
    private Message message;

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
