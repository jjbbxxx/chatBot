package com.xunfei.domain.request;


import com.xunfei.domain.request.header.Header;
import com.xunfei.domain.request.parameter.Parameter;
import com.xunfei.domain.request.payload.Payload;

/**
 * 大模型请求实体
 *
 * @author Linzj
 * @date 2023/10/20/020 10:23
 */
public class Request {

    /**
     * header 部分
     */
    private Header header;

    /**
     * parameter 部分
     */
    private Parameter parameter;

    /**
     * payload 部分
     */
    private Payload payload;

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }
}
