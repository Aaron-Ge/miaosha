package com.miaoshaproject.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.miaoshaproject.dao.UserDOMapper;
import com.miaoshaproject.dao.UserPasswordDOMapper;
import com.miaoshaproject.dataobject.UserDO;
import com.miaoshaproject.dataobject.UserPasswordDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import com.miaoshaproject.validator.ValidationResult;
import com.miaoshaproject.validator.ValidatorImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.alibaba.druid.util.StringUtils.*;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

//组装核心模型
@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserDOMapper userDOMapper;
    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;
    @Autowired
    private ValidatorImpl validator;
    @Override
    public UserModel getUserById(Integer id){
        //调用userdomapper获取到对应用户的dataobject
    UserDO userDO = userDOMapper.selectByPrimaryKey(id);
    if (userDO == null){
        return  null;
    }
    //通过用户id获取对应的用户加密密码信息
    UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
    return convertFromDataObject(userDO,userPasswordDO);
    }


    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException{
        if (userModel == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

   ValidationResult result =  validator.validate(userModel);
   if (result.isHasErrors()){
       throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,result.getErrMsg());
   }

       //实现model->dataobject
        UserDO userDO = convertFromModel(userModel);
        try{
            userDOMapper.insertSelective(userDO);
        }catch (DuplicateKeyException ex){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"手机号已重复注册");
        }


        userModel.setId(userDO.getId());

        UserPasswordDO userPasswordDO =convertPasswordFromModel(userModel);
        userPasswordDOMapper.insertSelective(userPasswordDO);

        return;
    }

    @Override
    public UserModel validateLogin(String telephone, String encrptPassword) throws BusinessException {
      //通过用户的手机获取用户信息
       UserDO userDO =  userDOMapper.selectByTelephone(telephone);
       if (userDO == null){
           throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
       }
       UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        UserModel userModel = convertFromDataObject(userDO,userPasswordDO);



      //比对用户信息内加密的密码是否和传输进来的密码相匹配
      if (!org.apache.commons.lang3.StringUtils.equals(encrptPassword,userModel.getEncrptPassword())) {
          throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
      }
      return userModel;

    }

    private UserPasswordDO convertPasswordFromModel(UserModel userModel){
        if (userModel == null){
            return null;
        }
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setEncrptPassword(userModel.getEncrptPassword());
        userPasswordDO.setUserId(userModel.getId());
        return userPasswordDO;
    }
    private UserDO convertFromModel(UserModel userModel){
        if (userModel == null){
            return null;
        }
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel,userDO);
        return  userDO;
    }
    private UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO){
        if (userDO == null){
            return  null;
        }
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO,userModel);
        if (userPasswordDO != null){
        userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());}
        return  userModel;
    }

}
