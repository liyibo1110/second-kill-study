package com.github.liyibo1110.secondkill.support.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author liyibo
 * @date 2026-06-23 17:30
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String phone;

    private String nickname;

    private String avatar;

    private Integer memberLevel;

    private Integer status;
}
