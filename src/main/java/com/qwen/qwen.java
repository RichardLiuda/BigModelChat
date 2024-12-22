package com.qwen;

import java.sql.Time;
import java.util.ArrayList;
// 建议dashscope SDK的版本 >= 2.12.0
import java.util.Arrays;
import java.util.List;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class qwen {
    public String text;
    public Boolean flag;
    public String returnText;
    public Boolean isOver;

    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();

    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    public void setFlag(Boolean _flag) throws Exception {
        if (_flag) {
            flag = _flag;
            for (ChangeListener listener : listeners) {
                listener.onChange(flag);
            }
        }
    }

    public interface ChangeListener {
        void onChange(Boolean flag) throws Exception;
    }

    public GenerationResult callWithMessage(
            String query) throws ApiException, NoApiKeyException, InputRequiredException {
        Generation gen = new Generation();
        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content("You are a helpful assistant.")
                .build();
        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content(query)
                .build();
        GenerationParam param = GenerationParam.builder()
                // 若没有配置环境变量，请用百炼API Key将下行替换为：.apiKey("sk-xxx")
                .apiKey("sk-7bdafe872e794284b0084d23d9934034")
                .model("qwen-max")
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .build();
        return gen.call(param);
    }

    public void getChat() throws JsonProcessingException {
        // Scanner scanner = new Scanner(System.in);
        System.out.println("Qwen-plus Chosen.");
        isOver = false;
        flag = false;

        addChangeListener(newFlag -> {
            flag = false;
            String query = text;
            Time time1 = new Time(System.currentTimeMillis());
            try {
                System.out.println(query);
                GenerationResult result = callWithMessage(query);
                String res = JsonUtils.toJson(result);
                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode = mapper.readTree(res);
                JsonNode transResult = rootNode.path("output").path("choices");
                if (transResult.isArray() && transResult.size() > 0) {
                    String dst = transResult.get(0).path("message").path("content").asText();
                    System.out.println("Qwen: " + dst);
                    returnText = dst;
                    isOver = true;
                } else {
                    System.out.println("未找到翻译结果！");
                }
                Time time2 = new Time(System.currentTimeMillis());
                System.out.println("耗时: " + (time2.getTime() - time1.getTime()) + "ms");
            } catch (ApiException | NoApiKeyException | InputRequiredException e) {
                System.err.println("An error occurred while calling the generation service: " + e.getMessage());
            }
        });
    }
}