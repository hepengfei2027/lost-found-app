package cn.jxufe.iet.lostandfound.controller;

import cn.jxufe.iet.lostandfound.common.Result;
import cn.jxufe.iet.lostandfound.config.ApiKeyCache;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    public static final String KEY_AI_API_URL = "ai_api_url";
    public static final String KEY_AI_API_KEY = "ai_api_key";
    public static final String KEY_AI_MODEL = "ai_ai_model";

    @GetMapping("/ai")
    public Result<Map<String, String>> getAiConfig() {
        Map<String, String> config = new HashMap<>();
        String apiKey = ApiKeyCache.get(KEY_AI_API_KEY);
        String apiUrl = ApiKeyCache.get(KEY_AI_API_URL);
        String model = ApiKeyCache.get(KEY_AI_MODEL);

        // 密钥只返回是否存在，不返回具体值
        config.put("hasApiKey", apiKey != null && !apiKey.isEmpty() ? "true" : "false");
        config.put("apiUrl", apiUrl != null ? apiUrl : "");
        config.put("model", model != null ? model : "");
        return Result.success(config);
    }

    @PostMapping("/ai")
    public Result<Void> saveAiConfig(@RequestBody Map<String, String> params) {
        String apiKey = params.get("apiKey");
        String apiUrl = params.get("apiUrl");
        String model = params.get("model");

        if (apiKey != null && !apiKey.isEmpty()) {
            ApiKeyCache.put(KEY_AI_API_KEY, apiKey);
        }

        if (apiUrl != null && !apiUrl.isEmpty()) {
            ApiKeyCache.put(KEY_AI_API_URL, apiUrl);
        } else {
            ApiKeyCache.remove(KEY_AI_API_URL);
        }

        if (model != null && !model.isEmpty()) {
            ApiKeyCache.put(KEY_AI_MODEL, model);
        } else {
            ApiKeyCache.remove(KEY_AI_MODEL);
        }

        return Result.success(null);
    }
}
