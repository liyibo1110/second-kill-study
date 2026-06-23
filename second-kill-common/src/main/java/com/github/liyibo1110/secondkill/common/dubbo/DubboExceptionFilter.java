package com.github.liyibo1110.secondkill.common.dubbo;

import com.github.liyibo1110.secondkill.common.exception.BizException;
import com.github.liyibo1110.secondkill.common.exception.ValidationException;
import com.github.liyibo1110.secondkill.common.log.StructuredLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;

/**
 * 这个Dubbo过滤器，用来统一后处理Provider端的异常。
 * 通过Dubbo的SPI机制来加载，BizException和ValidationException这两个，属于业务逻辑的正常分支，直接透传回Consumer端来处理，
 * 其他会被当作未预期的异常（NPE、数据库连接失败等等），会在Provider端记录完整日志，方便定位问题。
 * @author liyibo
 * @date 2026-06-22 16:19
 */
@Slf4j
@Activate(group = CommonConstants.PROVIDER)
public class DubboExceptionFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        Result result = invoker.invoke(invocation);
        if (result.hasException()) {
            Throwable exception = result.getException();
            // 只有BizException和ValidationException会被直接返回给Consumer端
            if (exception instanceof BizException || exception instanceof ValidationException)
                return result;

            // 其余所有异常，都要额外加一条log输出
            StructuredLog.error(log)
                    .message("Dubbo provider exception")
                    .put("service", invoker.getInterface().getName())
                    .put("method", invocation.getMethodName())
                    .exception(exception)
                    .log();

        }
        return result;
    }
}
