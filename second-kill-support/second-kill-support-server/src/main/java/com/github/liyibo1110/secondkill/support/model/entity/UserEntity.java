package com.github.liyibo1110.secondkill.support.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.github.liyibo1110.secondkill.common.model.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author liyibo
 * @date 2026-06-23 17:38
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class UserEntity extends BaseEntity {

    private String username;

    private String password;

    private String phone;

    private String nickname;

    private String avatar;

    private Integer memberLevel;

    private Integer status;
}
