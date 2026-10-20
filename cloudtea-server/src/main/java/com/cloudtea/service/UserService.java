package com.cloudtea.service;

import com.cloudtea.dto.UserLoginDTO;
import com.cloudtea.entity.User;

public interface UserService {
    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    User wxLogin(UserLoginDTO userLoginDTO);
}
