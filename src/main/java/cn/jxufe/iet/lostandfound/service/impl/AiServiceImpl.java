package cn.jxufe.iet.lostandfound.service.impl;

import cn.jxufe.iet.lostandfound.config.ApiKeyCache;
import cn.jxufe.iet.lostandfound.controller.ConfigController;
import cn.jxufe.iet.lostandfound.service.AiService;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiServiceImpl implements AiService {

    // 默认API地址和模型
    private static final String DEFAULT_API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    private static final String DEFAULT_MODEL = "qwen3.5-omni-plus";

    // 备选视觉模型
    private static final String[] FALLBACK_MODELS = {
        "qwen3-vl-plus",
        "qwen-vl-plus",
        "qwen3.7-plus"
    };

    @Override
    public Map<String, String> recognizeImage(String imageUrl) {
        Map<String, String> result = new HashMap<>();

        // 从内存缓存读取密钥（不读数据库）
        String apiKey = ApiKeyCache.get(ConfigController.KEY_AI_API_KEY);
        String apiUrl = ApiKeyCache.get(ConfigController.KEY_AI_API_URL);
        String model = ApiKeyCache.get(ConfigController.KEY_AI_MODEL);

        if (apiKey == null || apiKey.isEmpty()) {
            result.put("error", "请先在后台配置AI API密钥（http://localhost:8088）");
            return result;
        }

        // 默认使用阿里云百炼API地址
        if (apiUrl == null || apiUrl.isEmpty()) {
            apiUrl = DEFAULT_API_URL;
            ApiKeyCache.put(ConfigController.KEY_AI_API_URL, apiUrl);
        }

        // 默认使用qwen3.5-omni-plus
        if (model == null || model.isEmpty()) {
            model = DEFAULT_MODEL;
            ApiKeyCache.put(ConfigController.KEY_AI_MODEL, model);
        }

        // 先尝试默认配置
        Map<String, String> tryResult = tryRecognize(apiUrl, apiKey, model, imageUrl);
        if (!tryResult.containsKey("error")) {
            return tryResult;
        }

        String errorMsg = tryResult.get("error");
        System.out.println("默认配置失败: " + errorMsg);

        // 如果是401错误，说明密钥无效
        if (errorMsg.contains("401")) {
            result.put("error", "API密钥无效，请检查密钥是否正确。请到 https://bailian.console.aliyun.com 获取API Key");
            return result;
        }

        // 如果是404（模型不存在），尝试备选模型
        if (errorMsg.contains("404")) {
            System.out.println("模型不存在，尝试备选模型...");
            for (String fallbackModel : FALLBACK_MODELS) {
                System.out.println("尝试模型: " + fallbackModel);
                Map<String, String> fallbackResult = tryRecognize(apiUrl, apiKey, fallbackModel, imageUrl);
                if (!fallbackResult.containsKey("error")) {
                    ApiKeyCache.put(ConfigController.KEY_AI_MODEL, fallbackModel);
                    return fallbackResult;
                }
            }
        }

        result.put("error", errorMsg);
        return result;
    }

    private Map<String, String> tryRecognize(String apiUrl, String apiKey, String model, String imageUrl) {
        Map<String, String> result = new HashMap<>();
        try {
            String prompt = "请分析这张图片中的物品，不需要询问，直接返回以下格式的信息：\n" +
                    "1. 物品名称：（简要描述物品名称）\n" +
                    "2. 物品特征：（详细描述物品的外观、颜色、品牌等特征）\n" +
                    "3. 物品位置：（如果图片中有环境信息，描述可能出现的地点）\n" +
                    "请用简洁的中文回答。";

            String jsonBody = buildRequestBody(imageUrl, prompt, model);
            System.out.println("AI Request - Model: " + model + ", URL: " + apiUrl);

            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(120000);
            conn.setDoOutput(true);
            conn.getOutputStream().write(jsonBody.getBytes("UTF-8"));

            int responseCode = conn.getResponseCode();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(responseCode == 200 ? conn.getInputStream() : conn.getErrorStream(), "UTF-8"));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            conn.disconnect();

            String responseStr = response.toString();
            System.out.println("AI Response - Code: " + responseCode + ", Body: " + responseStr);

            if (responseCode == 200) {
                return parseResponse(responseStr);
            } else {
                String errorMsg = parseErrorResponse(responseStr);
                result.put("error", "API调用失败: " + responseCode + " - " + errorMsg);
                return result;
            }
        } catch (java.net.SocketTimeoutException e) {
            result.put("error", "请求超时，请重试");
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("error", "识别失败: " + e.getMessage());
            return result;
        }
    }

    private String parseErrorResponse(String response) {
        try {
            if (response.contains("\"message\":")) {
                Pattern pattern = Pattern.compile("\"message\"\\s*:\\s*\"([^\"]+)\"");
                Matcher matcher = pattern.matcher(response);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
            if (response.contains("\"error\":")) {
                int start = response.indexOf("\"error\":") + 8;
                int end = response.indexOf("}", start);
                if (end > start) {
                    String errorPart = response.substring(start, end).trim();
                    if (errorPart.startsWith("\"")) {
                        int strEnd = errorPart.indexOf("\"", 1);
                        if (strEnd > 0) {
                            return errorPart.substring(1, strEnd);
                        }
                    }
                    return errorPart;
                }
            }
        } catch (Exception e) {
        }
        return response.length() > 100 ? response.substring(0, 100) + "..." : response;
    }

    private String buildRequestBody(String imageUrl, String prompt, String model) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"model\":\"").append(model).append("\",\"messages\":[{\"role\":\"user\",\"content\":[");
        sb.append("{\"type\":\"image_url\",\"image_url\":{\"url\":\"").append(imageUrl).append("\"}}");
        sb.append(",{\"type\":\"text\",\"text\":\"").append(escapeJson(prompt)).append("\"}]}],\"stream\":false}");
        return sb.toString();
    }

    private Map<String, String> parseResponse(String jsonResponse) {
        Map<String, String> result = new HashMap<>();

        Pattern contentPattern = Pattern.compile("\"content\"\\s*:\\s*\"([\\s\\S]*?)\"\\s*,\\s*\"role\"");
        Matcher contentMatcher = contentPattern.matcher(jsonResponse);

        if (contentMatcher.find()) {
            String aiResponse = contentMatcher.group(1);
            aiResponse = aiResponse.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
            parseAiResponse(aiResponse, result);
        } else {
            Pattern altPattern = Pattern.compile("\"content\"\\s*:\\s*\"([\\s\\S]*?)\"\\s*[,}]");
            Matcher altMatcher = altPattern.matcher(jsonResponse);
            if (altMatcher.find()) {
                String aiResponse = altMatcher.group(1);
                aiResponse = aiResponse.replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
                parseAiResponse(aiResponse, result);
            } else {
                result.put("error", "解析响应失败: " + jsonResponse.substring(0, Math.min(200, jsonResponse.length())));
            }
        }

        return result;
    }

    private void parseAiResponse(String aiResponse, Map<String, String> result) {
        Pattern namePattern = Pattern.compile("物品名称[：:](.*?)(?=物品特征|物品位置|$)", Pattern.DOTALL);
        Pattern featurePattern = Pattern.compile("物品特征[：:](.*?)(?=物品位置|$)", Pattern.DOTALL);
        Pattern placePattern = Pattern.compile("物品位置[：:](.*)", Pattern.DOTALL);

        Matcher nameMatcher = namePattern.matcher(aiResponse);
        if (nameMatcher.find()) {
            result.put("name", nameMatcher.group(1).trim());
        }

        Matcher featureMatcher = featurePattern.matcher(aiResponse);
        if (featureMatcher.find()) {
            result.put("feature", featureMatcher.group(1).trim());
        }

        Matcher placeMatcher = placePattern.matcher(aiResponse);
        if (placeMatcher.find()) {
            result.put("place", placeMatcher.group(1).trim());
        }

        if (result.isEmpty()) {
            result.put("raw", aiResponse);
        }
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
