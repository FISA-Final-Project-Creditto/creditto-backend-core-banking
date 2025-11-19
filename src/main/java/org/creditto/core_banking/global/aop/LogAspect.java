package org.creditto.core_banking.global.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.http.ResponseEntity;
import org.creditto.core_banking.global.response.BaseResponse;

import java.util.Arrays;

@Aspect
@Slf4j
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final ObjectMapper objectMapper;

    @Pointcut("execution(* org.creditto.core_banking.domain..controller.*.*(..))")
    private void onRequest() {}

    @Pointcut("execution(* org.creditto.core_banking.domain..service.*.*(..))")
    private void onService() {}

    @Around("onRequest()")
    public Object test(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        String className = joinPoint.getTarget().getClass().getSimpleName();

        log.info("[{}] Request IP : {} | Request URI : {} | Request Method : {}",
                className,
                request.getRemoteAddr(),
                request.getRequestURI(),
                request.getMethod()
        );

        Object[] args = Arrays.stream(joinPoint.getArgs())
                .filter(arg -> !(arg instanceof HttpServletRequest))
                .filter(arg -> !(arg instanceof jakarta.servlet.http.HttpServletResponse))
                .filter(arg -> !(arg instanceof org.springframework.validation.BindingResult))
                .toArray();

        String argsAsString;
        try {
            argsAsString = objectMapper.writeValueAsString(args);
        } catch (Exception e) {
            argsAsString = Arrays.toString(args);
        }

        // Request Body (DTO) 로깅
        if (args.length > 0) {
            log.info("[{}] {} {} - RequestBody: {}",
                    className,
                    request.getMethod(),
                    request.getRequestURI(),
                    argsAsString
            );
        }

        // 실제 비즈니스 실행
        Object result = joinPoint.proceed();

        // Response Body 로깅
        Object responseBody = result;
        Object dataForLog;

        if (result instanceof ResponseEntity<?> responseEntity) {
            responseBody = responseEntity.getBody();
        }

        if (responseBody instanceof BaseResponse<?> baseResponse) {
            dataForLog = baseResponse.getData();
        } else {
            dataForLog = responseBody;
        }

        String dataAsString;
        try {
            dataAsString = objectMapper.writeValueAsString(dataForLog);
        } catch (Exception e) {
            dataAsString = String.valueOf(dataForLog);
        }

        log.info("[{}] {} {} - ResponseData: {}",
                className,
                request.getMethod(),
                request.getRequestURI(),
                dataAsString
        );

        return result;
    }

    @Before("onService()")
    public void beforeServiceLog(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.info("[{}] {}() called", className, methodName);

        if (args.length > 0) {
            log.debug("[{}] Parameters: {}", className, Arrays.toString(args));
        }
    }
}
