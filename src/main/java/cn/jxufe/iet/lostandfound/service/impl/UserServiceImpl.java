package cn.jxufe.iet.lostandfound.service.impl;

import cn.jxufe.iet.lostandfound.entity.User;
import cn.jxufe.iet.lostandfound.mapper.UserMapper;
import cn.jxufe.iet.lostandfound.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public User register(String phone, String password, String nickname) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        User existUser = userMapper.selectOne(wrapper);
        if (existUser != null) {
            throw new RuntimeException("手机号已注册");
        }
        User user = new User();
        user.setPhone(phone);
        user.setPassword(password);
        user.setNickname(nickname);
        userMapper.insert(user);
        return user;
    }

    @Override
    public User login(String phone, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone).eq(User::getPassword, password);
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new RuntimeException("手机号或密码错误");
        }
        return user;
    }

    @Override
    public void updateAvatar(Integer userId, Integer mediaId) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setAvatarId(mediaId);
            userMapper.updateById(user);
        }
    }

    @Override
    public User getById(Integer userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public void updateProfile(Integer userId, String nickname, String phone) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (nickname != null && !nickname.isEmpty()) {
            user.setNickname(nickname);
        }
        if (phone != null && !phone.isEmpty()) {
            // 检查手机号是否已被其他用户使用
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getPhone, phone).ne(User::getId, userId);
            User existUser = userMapper.selectOne(wrapper);
            if (existUser != null) {
                throw new RuntimeException("该手机号已被其他用户使用");
            }
            user.setPhone(phone);
        }
        userMapper.updateById(user);
    }
}
