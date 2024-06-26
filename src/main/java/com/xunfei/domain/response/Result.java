package com.xunfei.domain.response;

import com.xunfei.domain.response.header.Header;
import com.xunfei.domain.response.payload.Payload;

import java.util.StringJoiner;

/**
 * 接口响应对象
 *
 * @author Linzj
 * @date 2023/10/19/019 16:52
 */
public class Result {

    private Header header;
    private Payload payload;

    public void setHeader(Header header) {
        this.header = header;
    }

    public Header getHeader() {
        return header;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public Payload getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Result.class.getSimpleName() + "[", "]")
                .add("header=" + header)
                .add("payload=" + payload)
                .toString();
    }
}