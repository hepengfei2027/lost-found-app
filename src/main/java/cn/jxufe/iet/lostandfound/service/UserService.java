package cn.jxufe.iet.lostandfound.service;

import cn.jxufe.iet.lostandfound.entity.User;

public interface UserService {
    User register(String phone, String password, String nickname);

    User login(String phone, String password);

    void updateAvatar(Integer userId, Integer mediaId);

    User getById(Integer userId);

    void updateProfile(Integer userId, String nickname, String phone);
}
