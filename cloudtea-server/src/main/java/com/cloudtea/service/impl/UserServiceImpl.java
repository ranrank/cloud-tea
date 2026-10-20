package com.cloudtea.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloudtea.constant.MessageConstant;
import com.cloudtea.dto.UserLoginDTO;
import com.cloudtea.entity.User;
import com.cloudtea.exception.LoginFailedException;
import com.cloudtea.mapper.UserMapper;
import com.cloudtea.properties.JwtProperties;
import com.cloudtea.properties.WeChatProperties;
import com.cloudtea.service.UserService;
import com.cloudtea.utils.HttpClientUtil;
import com.cloudtea.utils.WeChatPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    public static final String WX_LOGIN ="https://api.weixin.qq.com/sns/jscode2session";

    /**
     * 模拟登录开关，复用 cloudtea.wechat.mock 这一个开关，和模拟支付同一套思路。
     * 浏览器里的 H5 拿不到微信小程序的 code，开启时直接用 code 派生一个固定的 openid，
     * 同一个 code 每次都登录成同一个用户，方便反复调试；接入真实小程序后改成 false 即可。
     */
    @Value("${cloudtea.wechat.mock:true}")
    private boolean mock;

    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;
    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        String openid = getOpenid(userLoginDTO.getCode());

        //判断openid是否为空，如果为空表示登录失败，抛出业务异常
        if(openid == null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        // 判断当前用户是否为新用户
        User user = userMapper.getByOpenid(openid);
        if(user == null){
            // 新用户
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }

        //如果是新用户，自动完成注册

        //返回这个用户对象

        return user;
    }
    /**
     * 调用微信接口,获取openid
     * @param code
     * @return
     */
    private String getOpenid(String code){
        //== 模拟登录：不请求微信，用 code 派生一个稳定的 openid ==
        if (mock) {
            log.info("【模拟登录】跳过微信 jscode2session，code={}", code);
            return "mock_" + code;
        }

        Map<String,String> map = new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
        map.put("js_code",code);
        map.put("grant_type","authorization_code");

        String json = HttpClientUtil.doGet(WX_LOGIN,map);

        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");
        return openid;
    }
}
