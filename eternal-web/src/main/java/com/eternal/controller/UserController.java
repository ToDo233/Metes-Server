package com.eternal.controller;

import com.eternal.common.utils.StringUtils;
import com.eternal.common.web.controller.BaseController;
import com.eternal.common.web.domain.AjaxResult;
import com.eternal.domain.UserEntity;
import com.eternal.domain.UserKeyEntity;
import com.eternal.service.IUserService;
import com.eternal.service.TokenService;
import com.eternal.utils.RSAUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;


/**
 * @author Eternal
 * @version 1.0
 * @description: TODO
 * @date 2021/9/23 4:33 下午
 */
@RestController
@RequestMapping("/user")
public class UserController extends BaseController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private IUserService userService;

    @PostMapping("/getToken")
    @ResponseBody
    public AjaxResult login (@RequestBody UserEntity user) throws Exception {
        String userName = user.getUserName();
        if (    StringUtils.isNotEmpty( userName)
                && StringUtils.isNotEmpty( userName)
                && userService.isUserNameExist(userName)){
                UserEntity userEntity = userService.selectUserByUserName(userName);
                Long userId = userEntity.getId();
            UserKeyEntity userKeyEntity = userService.selectUserKeyByUserId(userId);
            String token = tokenService.createToken(userName);
                System.out.println(token);
                //加密 token 发送到客户端解密
                String encrypt = RSAUtils.encrypt(token, userKeyEntity.getPublicKey());
                HashMap resultMap = new HashMap();
                resultMap.put("userId",userId);
                resultMap.put("token",encrypt);
                resultMap.put("masterKeyBa",userKeyEntity.getMasterKeyBa());
                resultMap.put("privateKeyBa",userKeyEntity.getPrivateKeyBa());
                return AjaxResult.success("login success",resultMap);
        }
        return  AjaxResult.error("login error");
    }

    @PostMapping("/check")
    @ResponseBody
    public AjaxResult checkToken (@RequestHeader(HttpHeaders.AUTHORIZATION) String token){
        AjaxResult result = new AjaxResult();
        token = token.replace("Bearer","").trim();
        Boolean aBoolean = userService.checkToken(token);
        result.put("checkResult",aBoolean);
        return result;
    }
}
