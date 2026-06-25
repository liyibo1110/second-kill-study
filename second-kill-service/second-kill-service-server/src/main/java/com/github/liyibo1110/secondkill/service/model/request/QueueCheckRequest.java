package com.github.liyibo1110.secondkill.service.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 排队结果轮询请求。
 * @author liyibo
 * @date 2026-06-24 10:57
 */
@Data
public class QueueCheckRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 排队令牌 */
    private String token;

    /** 链路追踪ID */
    private String traceId;
}
