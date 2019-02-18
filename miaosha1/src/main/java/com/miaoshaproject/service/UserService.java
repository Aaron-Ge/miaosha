package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.UserModel;

public interface UserService {
    //通过用户ID获取用户对象的方法
    UserModel getUserById(Integer id);
    void register(UserModel userModel) throws BusinessException;
    /*
    * telephone:用户注册手机
    *password:用户加密后的密码
    * */
    UserModel validateLogin(String telephone,String encrptPassword) throws BusinessException;
}
