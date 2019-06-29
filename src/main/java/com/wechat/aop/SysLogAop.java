package com.wechat.aop;

import com.wechat.annotation.SysLog;
import com.wechat.global.UserContext;
import com.wechat.po.SysLogPO;
import com.wechat.util.IPUtils;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
public class SysLogAop {

    @Pointcut(value = "@annotation(com.wechat.annotation.SysLog)")
    public void logPointCut() {
    }

    @Around(value = "logPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) {
        Object proceed = null;
        long beginTime = System.currentTimeMillis();
        // 执行方法
        try {
            proceed = joinPoint.proceed();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        // 执行时长(毫秒)
        int time = (int) (endTime - beginTime);

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
            .getRequest();
        for (Object methodArg : joinPoint.getArgs()) {
            if (methodArg instanceof HttpServletRequest) {
                request = (HttpServletRequest) methodArg;
                break;
            }
        }
        //保存日志
        saveSysLogPO(joinPoint, request, time, proceed);

        return proceed;
    }

    private void saveSysLogPO(ProceedingJoinPoint joinPoint, HttpServletRequest request, int time, Object proceed) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();

            SysLogPO sysLogPO = new SysLogPO();
            SysLog SysLogPO = method.getAnnotation(SysLog.class);
            if (SysLogPO != null) {
                //注解上的描述
                sysLogPO.setOperation(SysLogPO.value());
            }

            //请求的方法名
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = signature.getName();
            sysLogPO.setMethod(request.getServletPath() + "【" + className + "." + methodName + "()】");

            //请求的参数
            Map<String, String[]> requestParams = request.getParameterMap();
            Map<String, String> args = new HashMap<>();
            if (requestParams != null) {
                for (Entry<String, String[]> entry : requestParams.entrySet()) {
                    String name = entry.getKey();
                    String[] values = entry.getValue();
                    String valueStr = "";
                    for (int i = 0; i < values.length; i++) {
                        valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
                    }
                    // 乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
                    // valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
                    args.put(name, valueStr);
                }
            }

            sysLogPO.setParams(args.toString());

            //设置IP地址
            sysLogPO.setIp(IPUtils.getIpAddr(request));

            //用户名
            sysLogPO.setUsername(UserContext.getUserName());

            sysLogPO.setTime(time);
            sysLogPO.setResult(proceed.toString());

            //保存系统日志
            log.info(sysLogPO.toString());
        } catch (Exception e) {
            log.error("add SysLogPO error: {}", e.getMessage());
        }
    }
}
