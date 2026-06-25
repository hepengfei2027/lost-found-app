package cn.jxufe.iet.lostandfound.controller;

import cn.jxufe.iet.lostandfound.common.Result;
import cn.jxufe.iet.lostandfound.entity.Media;
import cn.jxufe.iet.lostandfound.entity.User;
import cn.jxufe.iet.lostandfound.mapper.MediaMapper;
import cn.jxufe.iet.lostandfound.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private MediaMapper mediaMapper;

    @Value("${upload.path}")
    private String uploadPath;

    @PostMapping("/register")
    public Result<User> register(@RequestBody Map<String, String> params) {
        try {
            String phone = params.get("phone");
            String password = params.get("password");
            String nickname = params.get("nickname");
            User user = userService.register(phone, password, nickname);
            return Result.success(user);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        try {
            String phone = params.get("phone");
            String password = params.get("password");
            User user = userService.login(phone, password);
            Map<String, Object> data = new HashMap<>();
            data.put("id", user.getId());
            data.put("phone", user.getPhone());
            data.put("nickname", user.getNickname());
            if (user.getAvatarId() != null) {
                Media media = mediaMapper.selectById(user.getAvatarId());
                if (media != null) {
                    data.put("avatarUrl", media.getUrl());
                }
            }
            return Result.success(data);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/avatar")
    public Result<Map<String, Object>> uploadAvatar(@RequestParam("file") MultipartFile file,
                                                    @RequestParam("userId") Integer userId) {
        try {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String filename = System.currentTimeMillis() + extension;
            File dest = new File(uploadPath + filename);
            file.transferTo(dest);

            String url = "/uploads/" + filename;

            Media media = new Media();
            media.setParentId(userId);
            media.setParentType("user");
            media.setType(1);
            media.setUrl(url);
            mediaMapper.insert(media);

            userService.updateAvatar(userId, media.getId());

            Map<String, Object> result = new HashMap<>();
            result.put("url", url);
            result.put("mediaId", media.getId());
            return Result.success(result);
        } catch (IOException e) {
            return Result.error("上传失败");
        }
    }

    @GetMapping("/info/{userId}")
    public Result<Map<String, Object>> getUserInfo(@PathVariable Integer userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("nickname", user.getNickname());
        data.put("phone", user.getPhone());
        if (user.getAvatarId() != null) {
            Media media = mediaMapper.selectById(user.getAvatarId());
            if (media != null) {
                data.put("avatarUrl", media.getUrl());
            }
        }
        return Result.success(data);
    }

    @PutMapping("/update")
    public Result<Void> updateProfile(@RequestBody Map<String, String> params) {
        try {
            String userIdStr = params.get("userId");
            String nickname = params.get("nickname");
            String phone = params.get("phone");
            if (userIdStr == null || userIdStr.isEmpty()) {
                return Result.error("用户ID不能为空");
            }
            Integer userId = Integer.parseInt(userIdStr);
            userService.updateProfile(userId, nickname, phone);
            return Result.success(null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
