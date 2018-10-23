package com.mmall.dao;

import com.mmall.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {

    /**
     * 根据主键id删除用户
     *
     * @param id
     * @return
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * 插入用户
     *
     * @param record
     * @return
     */
    int insert(User record);

    /**
     * 有选择的插入用户字段
     *
     * @param record
     * @return
     */
    int insertSelective(User record);

    /**
     * 根据主键id查找用户
     *
     * @param id
     * @return
     */
    User selectByPrimaryKey(Integer id);


    /**
     * 根据主键id有选择的更新用户字段
     *
     * @param record
     * @return
     */
    int updateByPrimaryKeySelective(User record);

    /**
     * 根据主键id更新用户全部字段
     *
     * @param record
     * @return
     */
    int updateByPrimaryKey(User record);

    /**
     * 根据用户名密码登录
     *
     * @param username
     * @param password
     * @return
     */
    User selectLogin(@Param("username") String username, @Param("password") String password);

    /**
     * 检查用户名是否存在
     *
     * @param userName 用户名
     * @return 查找该用户名在数据库中count
     */
    int checkUserName(String userName);

    /**
     * 检查email是否存在
     *
     * @param email
     * @return
     */
    int checkEmail(String email);

    /**
     * 根据用户名查找密码提示问题
     *
     * @param username
     * @return
     */
    String selectQuestionByUserName(String username);

    /**
     * 查询是否有username+question+answer的这一用户
     *
     * @param username
     * @param question 密码提示问题
     * @param answer   问题答案
     * @return 满足条件的用户count
     */
    int checkAnswer(@Param("username") String username, @Param("question") String question, @Param("answer") String answer);

    /**
     * 根据用户名修改密码
     *
     * @param username
     * @param password
     * @return
     */
    int updatePasswordByUserName(@Param("username") String username, @Param("passwordNew") String password);

    /**
     * 校验密码和用户id，是否存在该用户
     *
     * @param userId
     * @param password
     * @return
     */
    int checkPassword(@Param("userId") Integer userId, @Param("password") String password);

    /**
     * 根据用户id来校验email
     *
     * @param userId
     * @param email
     * @return
     */
    int checkEmailByUserId(@Param("userId") Integer userId, @Param("email") String email);

    /**
     * 管理员-查询用户总数
     *
     * @return 用户总数
     */
    int selectCount();
}
