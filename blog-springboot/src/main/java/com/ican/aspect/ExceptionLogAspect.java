package com.ican.aspect;

import cn.hutool.extra.servlet.ServletUtil;
import com.alibaba.fastjson2.JSON;
import com.ican.entity.ExceptionLog;
import com.ican.manager.AsyncManager;
import com.ican.manager.factory.AsyncFactory;
import com.ican.utils.IpUtils;
import com.ican.utils.UserAgentUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * AOP记录异常日志
 *
 * @author Aric
 */
@Aspect
@Component
public class ExceptionLogAspect {

    /**
     * 设置操作异常日志切入点，扫描所有controller包下的操作
     */
    @Pointcut("execution(* com.ican.controller..*.*(..))")
    public void exceptionLogPointCut() {
    }

    /**
     * 异常通知，只有连接点异常后才会执行
     *
     * @param joinPoint 切面方法的信息
     * @param e         异常
     */
    @AfterThrowing(pointcut = "exceptionLogPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Throwable e) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = Objects.requireNonNull(attributes).getRequest();
            Api api = (Api) signature.getDeclaringType().getAnnotation(Api.class);
            ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
            ExceptionLog exceptionLog = new ExceptionLog();
            exceptionLog.setModule(api != null && api.tags().length > 0 ? api.tags()[0] : "未知模块");
            exceptionLog.setUri(request.getRequestURI());
            exceptionLog.setName(e.getClass().getName());
            exceptionLog.setDescription(apiOperation != null ? apiOperation.value() : method.getName());
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = method.getName();
            methodName = className + "." + methodName;
            exceptionLog.setErrorMethod(methodName);
            exceptionLog.setMessage(stackTraceToString(e.getClass().getName(), e.getMessage(), e.getStackTrace()));
            if (joinPoint.getArgs().length > 0 && joinPoint.getArgs()[0] instanceof MultipartFile) {
                exceptionLog.setParams(((MultipartFile) joinPoint.getArgs()[0]).getOriginalFilename());
            } else {
                exceptionLog.setParams(JSON.toJSONString(joinPoint.getArgs()));
            }
            exceptionLog.setRequestMethod(Objects.requireNonNull(request).getMethod());
            String ip = ServletUtil.getClientIP(request);
            exceptionLog.setIpAddress(ip);
            exceptionLog.setIpSource(IpUtils.getIpSource(ip));
            Map<String, String> userAgentMap = UserAgentUtils.parseOsAndBrowser(request.getHeader("User-Agent"));
            exceptionLog.setOs(userAgentMap.get("os"));
            exceptionLog.setBrowser(userAgentMap.get("browser"));
            AsyncManager.getInstance().execute(AsyncFactory.recordException(exceptionLog));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String stackTraceToString(String exceptionName, String exceptionMessage, StackTraceElement[] elements) {
        StringBuilder stringBuilder = new StringBuilder();
        for (StackTraceElement stet : elements) {
            stringBuilder.append(stet).append("\n");
        }
        return exceptionName + ":" + exceptionMessage + "\n" + stringBuilder;
    }

}
