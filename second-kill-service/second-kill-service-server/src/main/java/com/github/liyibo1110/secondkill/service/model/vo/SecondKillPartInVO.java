package com.github.liyibo1110.secondkill.service.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 秒杀下单响应。
 * @author liyibo
 * @date 2026-06-24 11:03
 */
@Data
@Accessors(chain = true)
public class SecondKillPartInVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 参与结果状态码 */
    private String code;

    /** 结果描述 */
    private String message;

    /** 排队令牌，前端用于轮询结果 */
    private String token;

    /** 链路追踪ID */
    private String traceId;

    /** SKU编号 */
    private String skuId;
}
