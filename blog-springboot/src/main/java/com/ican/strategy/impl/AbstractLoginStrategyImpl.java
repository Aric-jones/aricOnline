package com.ican.strategy.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ican.entity.User;
import com.ican.entity.UserRole;
import com.ican.enums.RoleEnum;
import com.ican.mapper.UserMapper;
import com.ican.mapper.UserRoleMapper;
import com.ican.model.dto.SocialTokenDTO;
import com.ican.model.dto.SocialUserInfoDTO;
import com.ican.model.vo.request.CodeReq;
import com.ican.strategy.SocialLoginStrategy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 抽象登录模板
 *
 * @author ican
 */
@Service
public abstract class AbstractLoginStrategyImpl implements SocialLoginStrategy {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public String login(CodeReq data) {
        User user;
        SocialTokenDTO socialToken = getSocialToken(data);
        SocialUserInfoDTO socialUserInfoDTO = getSocialUserInfo(socialToken);
        // 先按第三方ID + 登录方式查找
        User existUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .select(User::getId)
                .eq(User::getUsername, socialUserInfoDTO.getId())
                .eq(User::getLoginType, socialToken.getLoginType()));
        if (Objects.isNull(existUser) && StringUtils.isNotBlank(socialUserInfoDTO.getEmail())) {
            // 第三方返回了邮箱，按邮箱匹配已有账号
            existUser = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .select(User::getId)
                    .eq(User::getEmail, socialUserInfoDTO.getEmail()));
        }
        if (Objects.isNull(existUser)) {
            user = saveLoginUser(socialToken, socialUserInfoDTO);
        } else {
            user = existUser;
        }
        StpUtil.checkDisable(user.getId());
        StpUtil.login(user.getId());
        return StpUtil.getTokenValue();
    }

    /**
     * 获取第三方Token
     *
     * @param data data
     * @return {@link SocialTokenDTO} 第三方token
     */
    public abstract SocialTokenDTO getSocialToken(CodeReq data);

    /**
     * 获取第三方用户信息
     *
     * @param socialToken 第三方token
     * @return {@link SocialUserInfoDTO} 第三方用户信息
     */
    public abstract SocialUserInfoDTO getSocialUserInfo(SocialTokenDTO socialToken);

    /**
     * 新增用户账号
     *
     * @param socialToken 第三方Token
     * @return {@link User} 登录用户身份权限
     */
    private User saveLoginUser(SocialTokenDTO socialToken, SocialUserInfoDTO socialUserInfoDTO) {
        String email = socialUserInfoDTO.getEmail();
        String username = StringUtils.isNotBlank(email) ? email : socialUserInfoDTO.getId();
        User newUser = User.builder()
                .avatar(socialUserInfoDTO.getAvatar())
                .nickname(socialUserInfoDTO.getNickname())
                .username(username)
                .email(email)
                .password(socialToken.getAccessToken())
                .loginType(socialToken.getLoginType())
                .build();
        userMapper.insert(newUser);
        // 新增用户角色
        UserRole userRole = UserRole.builder()
                .userId(newUser.getId())
                .roleId(RoleEnum.USER.getRoleId())
                .build();
        userRoleMapper.insert(userRole);
        return newUser;
    }

}
