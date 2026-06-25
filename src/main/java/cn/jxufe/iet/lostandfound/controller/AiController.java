package cn.jxufe.iet.lostandfound.controller;

import cn.jxufe.iet.lostandfound.common.Result;
import cn.jxufe.iet.lostandfound.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping("/recognize")
    public Result<Map<String, String>> recognizeImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error("请上传图片");
        }

        try {
            String base64Image = imageToBase64(file);
            String dataUrl = "data:" + file.getContentType() + ";base64," + base64Image;
            
            Map<String, String> result = aiService.recognizeImage(dataUrl);
            
            if (result.containsKey("error")) {
                return Result.error(result.get("error"));
            }
            
            return Result.success(result);
        } catch (IOException e) {
            return Result.error("图片处理失败: " + e.getMessage());
        }
    }

    @PostMapping("/recognize-url")
    public Result<Map<String, String>> recognizeImageByUrl(@RequestBody Map<String, String> params) {
        String imageUrl = params.get("imageUrl");
        if (imageUrl == null || imageUrl.isEmpty()) {
            return Result.error("请提供图片URL");
        }

        Map<String, String> result = aiService.recognizeImage(imageUrl);
        
        if (result.containsKey("error")) {
            return Result.error(result.get("error"));
        }
        
        return Result.success(result);
    }

    private String imageToBase64(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        return Base64.getEncoder().encodeToString(bytes);
    }
}
