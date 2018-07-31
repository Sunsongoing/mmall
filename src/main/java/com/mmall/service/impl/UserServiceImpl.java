package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.UserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * @author Sunsongoing
 */
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录成功返回user对象除了password之外的所有字段，status=0
     * 登录失败 status = 1
     */
    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUserName(username);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        //密码登录
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, md5Password);

        if (null == user) {
            return ServerResponse.createByErrorMessage("密码错误");
        }
        //将密码置空
        user.setPassword(StringUtils.EMPTY);

        return ServerResponse.createBySuccess("登录成功", user);
    }

    /**
     * 注册
     *
     * @param user 注册的用户
     * @return 成功 status = 0
     * 失败 status = 1
     */
    @Override
    public ServerResponse<String> register(User user) {
        ServerResponse<String> checkValidResponse = this.checkValid(user.getUsername(), Const.USERNAME);
        //如果校验不通过
        if (!checkValidResponse.isSuccess()) {
            return checkValidResponse;
        }
        checkValidResponse = this.checkValid(user.getEmail(), Const.EMAIL);
        if (!checkValidResponse.isSuccess()) {
            return checkValidResponse;
        }
        //默认为普通用户
        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        //插入用户失败
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    /**
     * 校验 用户名和 email
     *
     * @param str
     * @param type
     * @return
     */
    @Override
    public ServerResponse<String> checkValid(String str, String type) {
        if (StringUtils.isNoneBlank(type)) {
            //开始校验
            int resultCount;
            //按类型校验
            if (Const.USERNAME.equals(type)) {
                resultCount = userMapper.checkUserName(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            if (Const.EMAIL.equals(type)) {
                resultCount = userMapper.checkEmail(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("邮箱已存在");
                }
            }

        } else {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    /**
     * 获取密码提示问题
     *
     * @param username
     * @return
     */
    @Override
    public ServerResponse<String> selectQuestion(String username) {
        ServerResponse<String> response = this.checkValid(username, Const.USERNAME);
        if (response.isSuccess()) {
            //用户名不存在
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        String question = userMapper.selectQuestionByUserName(username);
        if (StringUtils.isNotBlank(question)) {
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("找回密码的问题是空的");
    }

    /**
     * 校验 密码提示问题答案和用户是否一致
     *
     * @param username
     * @param question
     * @param answer
     * @return
     */
    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if (resultCount > 0) {
            //说明问题及答案是这个用户的，并且是正确的
            //生成token
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(Const.TOKEN_PREFIX + username, forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题的答案错误");
    }

    /**
     * 基于本地token修改密码
     *
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    @Override
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        if (StringUtils.isBlank(forgetToken)) {
            return ServerResponse.createByErrorMessage("参数错误，token是必需的");
        }
        ServerResponse<String> response = this.checkValid(username, Const.USERNAME);
        if (response.isSuccess()) {
            //用户名不存在
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        String token = TokenCache.getKey(Const.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(token)) {
            return ServerResponse.createByErrorMessage("token无效或者过期");
        }
        //判断缓存的token和传递过来的token是否一致
        if (StringUtils.equals(token, forgetToken)) {
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUserName(username, md5Password);
            if (rowCount > 0) {
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }
        } else {
            return ServerResponse.createByErrorMessage("token错误，请重新获取重置密码的token");
        }

        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    /**
     * 登录状态下修改密码
     *
     * @param user
     * @param passwordOld
     * @param passwordNew
     * @return
     */
    @Override
    public ServerResponse<String> resetPassword(User user, String passwordOld, String passwordNew) {
        //为了防止横向越权，要校验这个用户的旧密码，一定要指定是这个用户
        if (null == user) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        if (StringUtils.equals(passwordNew, passwordOld)) {
            return ServerResponse.createByErrorMessage("旧密码与新密码一致，不需要修改");
        }

        int resultCount = userMapper.checkPassword(user.getId(), MD5Util.MD5EncodeUtf8(passwordOld));
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("旧密码错误");
        }

        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        resultCount = userMapper.updateByPrimaryKeySelective(user);
        if (resultCount > 0) {
            return ServerResponse.createBySuccessMessage("密码重置成功");
        }
        return ServerResponse.createByErrorMessage("重置密码失败");
    }

    /**
     * 登录状态更新用户信息
     * 1.username 不能被更新
     * 2.校验邮箱，邮箱不能是存在的，也不能是当前用户的邮箱
     *
     * @param user
     * @param
     * @return
     */
    @Override
    public ServerResponse<User> updateInformation(User user) {
        int resultCount = userMapper.checkEmailByUserId(user.getId(), user.getEmail());
        if (resultCount > 0) {
            return ServerResponse.createByErrorMessage("email已经存在，请更换email再尝试更新");
        }
        User userNew = new User();
        //值唯一字段
        userNew.setId(user.getId());
        userNew.setUsername(user.getUsername());
        userNew.setEmail(user.getEmail());
        //值可以重复的字段
        userNew.setPhone(user.getPhone());
        userNew.setQuestion(user.getQuestion());
        userNew.setAnswer(user.getAnswer());
        resultCount = userMapper.updateByPrimaryKeySelective(userNew);
        if (resultCount > 0) {
            return ServerResponse.createBySuccess("更新用户信息成功", userNew);
        }
        return ServerResponse.createByErrorMessage("更新用户信息失败");
    }

    /**
     * 获取用户的详细信息，并强制登录
     *
     * @param userId
     * @return
     */
    @Override
    public ServerResponse<User> getInformation(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (null == user) {
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }
        user.setPassword(StringUtils.EMPTY);

        return ServerResponse.createBySuccess(user);
    }

    /**
     * 校验用户是否是管理员
     *
     * @param user
     * @return
     */
    @Override
    public ServerResponse checkAdminRole(User user) {
        if (null != user && Const.Role.ROLE_ADMIN == user.getRole()) {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

}
