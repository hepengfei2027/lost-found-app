package cn.jxufe.iet.lostandfound.service;

import java.util.Map;

public interface AiService {
    Map<String, String> recognizeImage(String imageUrl);
}
