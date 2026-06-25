package com.github.liyibo1110.secondkill.service.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 秒杀预检响应，包含随机字符串和时间戳，前端用于计算机审校验值。
 * @author liyibo
 * @date 2026-06-24 11:05
 */
@Data
@Accessors(chain = true)
public class PreCheckVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 随机字符串（原始值，未插入标记字符） */
    private String result;

    /** 服务端时间戳，前端用它计算标记字符的插入位置 */
    private long key;
}
