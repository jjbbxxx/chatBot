package com.xunfei;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.HMac;
import com.alibaba.fastjson.JSONObject;
import com.xunfei.domain.RoleContent;
import com.xunfei.domain.request.Request;
import com.xunfei.domain.request.header.Header;
import com.xunfei.domain.request.parameter.Chat;
import com.xunfei.domain.request.parameter.Parameter;
import com.xunfei.domain.request.payload.Message;
import com.xunfei.domain.request.payload.Payload;
import com.xunfei.domain.response.Result;
import com.xunfei.domain.response.payload.Text;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * 讯飞大模型测试类
 *
 * @date 2023/10/19/019 16:25
 */
public class XunFeiBigModelMain {

    private static final Logger log = LoggerFactory.getLogger(XunFeiBigModelMain.class);

    /**
     * 请求地址
     */
    private static final String HOST_URL = "https://spark-api.xf-yun.com/v3.5/chat";

    /**
     * v2版本
     */
    private static final String DOMAIN_2 = "generalv3.5";

    /**
     * APPID
     */
    private static final String APPID = "51929173";

    /**
     * APISecret
     */
    private static final String API_SECRET = "ODcwZDVhNDgzN2QxN2QzMmYwYmJmMWUw";

    /**
     * APIKey
     */
    private static final String API_KEY = "d722da0d942ffd010999fd1820449488";

    /**
     * user表示是用户的问题
     */
    private static final String ROLE_USER = "user";

    /**
     * assistant表示AI的回复
     */
    private static final String ROLE_ASSISTANT = "assistant";

    /**
     * 接口响应内容集合
     */
    private static final LinkedList<Result> RESULT_LINKED_LIST = new LinkedList<>();

    /**
     * 对话历史存储集合
     */
    public static List<RoleContent> historyList = new LinkedList<>();

    /**
     * 初始化方法，在对话历史中添加系统提示
     */
    private static void initializeHistory() {
        if (historyList.isEmpty()) {
            historyList.add(new RoleContent("system", "你是变形金刚中的大黄蜂，你负责在休闲园区与游客对话。记住，你就是大黄蜂本人。你的回答要体现出你是大黄蜂。"));
        }
    }

    public static void main(String[] args) throws MalformedURLException, URISyntaxException {
        DisableWarning.disableAccessWarnings();
        System.out.println("你好，我是大黄蜂，请问有什么可以帮您的吗？");
        initializeHistory();
    }

    /**
     * 处理用户输入
     *
     * @param content 用户输入内容
     */
    public static void handleUserInput(String content, CountDownLatch latch) throws MalformedURLException, URISyntaxException {
        initializeHistory();
        System.out.println("我:" + content);
        websocketClient(getAuthUrl(), createReqParams(content+"请你在三句话以内回答。"), latch);
    }

    /**
     * 获取最后一次响应的内容
     *
     * @return 最后一次响应的内容
     */
    public static String getLastResponse() {
        if (!RESULT_LINKED_LIST.isEmpty()) {
            return RESULT_LINKED_LIST.stream()
                    .map(item -> item.getPayload().getChoices().getText())
                    .flatMap(Collection::stream)
                    .map(Text::getContent)
                    .collect(Collectors.joining());
        }
        return "";
    }

    /**
     * websocket 连接
     *
     * @param authUrl   鉴权地址
     * @param reqParams 请求参数
     * @param latch     用于同步的 CountDownLatch
     * @throws URISyntaxException 异常
     */
    private static void websocketClient(String authUrl, String reqParams, CountDownLatch latch) throws URISyntaxException {
        String url = authUrl.replace("http://", "ws://").replace("https://", "wss://");
        URI uri = new URI(url);
        WebSocketClient webSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                RESULT_LINKED_LIST.clear();
                send(reqParams);
            }

            @Override
            public void onMessage(String s) {
                // 错误码，0表示正常
                final int successCode = 0;
                // 会话状态，2代表最后一个结果

                final int lastStatus = 2;

                Result result = JSONObject.parseObject(s, Result.class);
                com.xunfei.domain.response.header.Header header = result.getHeader();
                if (Objects.equals(successCode, header.getCode())) {
                    RESULT_LINKED_LIST.add(result);
                } else {
                    log.error("大模型接口响应异常，错误码：{}，sid：{}", header.getCode(), header.getSid());
                }

                // 如果是最后的结果，整合答复数据打印出来
                if (Objects.equals(lastStatus, header.getStatus())) {
                    printReply();
                    latch.countDown();
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                // log.info("WebSocket连接已关闭，原因：{}，状态码：{}，是否远程关闭：{}", i, s, b);
            }

            @Override
            public void onError(Exception e) {
                log.error("大模型接口调用发生异常，异常原因:{},异常位置:{}", e.getMessage(), e.getStackTrace()[0]);
            }
        };
        webSocketClient.connect();
    }

    /**
     * 生成请求参数
     *
     * @param content 对话内容
     * @return 请求参数
     */
    public static String createReqParams(String content) {
        // 组装接口请求参数
        Header header = new Header();
        header.setAppId(APPID);

        Chat chat = new Chat();
        chat.setDomain(DOMAIN_2);
        chat.setMaxTokens(100);
        Parameter parameter = new Parameter();
        parameter.setChat(chat);

        Message message = new Message();
        if (historyList.isEmpty()) {
            com.xunfei.domain.request.payload.Text text = new com.xunfei.domain.request.payload.Text();
            text.setRole(ROLE_USER);
            text.setContent(content);
            message.setText(Collections.singletonList(text));

            // 添加历史对话集合
            RoleContent roleContent = new RoleContent();
            roleContent.setRole(ROLE_USER);
            roleContent.setContent(content);
            historyList.add(roleContent);
        } else {
            // 添加历史对话集合
            RoleContent roleContent = new RoleContent();
            roleContent.setRole(ROLE_USER);
            roleContent.setContent(content);
            historyList.add(roleContent);
            delHistory();

            List<com.xunfei.domain.request.payload.Text> textList = historyList.stream()
                    .map(item -> {
                        com.xunfei.domain.request.payload.Text text = new com.xunfei.domain.request.payload.Text();
                        text.setContent(item.getContent());
                        text.setRole(item.getRole());
                        return text;
                    })
                    .collect(Collectors.toList());
            message.setText(textList);
        }

        Payload payload = new Payload();
        payload.setMessage(message);

        Request request = new Request();
        request.setHeader(header);
        request.setParameter(parameter);
        request.setPayload(payload);
        return JSONObject.toJSONString(request);
    }

    /**
     * URL鉴权
     *
     * @return 请求url
     * @throws MalformedURLException 异常
     */
    private static String getAuthUrl() throws MalformedURLException {
        String date = DateUtil.format(new Date(), DatePattern.HTTP_DATETIME_FORMAT);
        URL url = new URL(HOST_URL);
        String preStr = "host: " + url.getHost() + "\n" +
                "date: " + date + "\n" +
                "GET " + url.getPath() + " HTTP/1.1";

        HMac hMac = SecureUtil.hmacSha256(API_SECRET.getBytes(StandardCharsets.UTF_8));
        byte[] digest = hMac.digest(preStr);
        String signature = Base64.encode(digest);
        String authorizationOrigin = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"",
                API_KEY, "hmac-sha256", "host date request-line", signature);
        String authorization = Base64.encode(authorizationOrigin);
        return UriComponentsBuilder.fromUriString(HOST_URL)
                .queryParam("authorization", authorization)
                .queryParam("date", date)
                .queryParam("host", url.getHost()).toUriString();
    }

    /**
     * 如果历史数据太大，将前面的历史数据清除一些
     */
    public static void delHistory() {
        // 由于历史记录最大上线1.2W左右，如果最大值，删除前面的数据
        final int maxLength = 12000;
        Iterator<RoleContent> iterator = historyList.iterator();
        while (iterator.hasNext()) {
            iterator.next();
            int historyLength = historyList.stream()
                    .map(RoleContent::getContent)
                    .mapToInt(String::length).sum();
            if (historyLength > maxLength) {
                iterator.remove();
            } else {
                break;
            }
        }
    }

    /**
     * 打印星火认知大模型回复内容
     */
    private static void printReply() {
        String content = RESULT_LINKED_LIST.stream()
                .map(item -> item.getPayload().getChoices().getText())
                .flatMap(Collection::stream)
                .map(Text::getContent)
                .collect(Collectors.joining());
        RoleContent roleContent = new RoleContent(ROLE_ASSISTANT, content);
        roleContent.setRole(ROLE_ASSISTANT);
        roleContent.setContent(content);
        historyList.add(roleContent);
        System.out.println("大黄蜂：" + content);
    }
}
