package alchemy.aspects;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alchemy.annotations.Logged;

@Aspect
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("@annotation(logged)")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint, Logged logged) throws Throwable {
        
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String description = logged.value().isEmpty() ? "Execution" : logged.value();

        if (logged.logEntry()) {
            logger.info("-> [{}]: {} for {}.{}() with args : {}", description, "Start", className, methodName, Arrays.toString(joinPoint.getArgs()));
        }

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            if (logged.logExit() && logged.logSuccess()) {
            	logger.info("<- [{}]: {} for {}.{}() with result : {} (Duration: {} ms)", description, "Success", className, methodName, result, duration);
            }
            
            return result;
            
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - startTime;
            
            if (logged.logExit() && logged.logFailure()) {
                logger.error("!! [{}]: {} for {}.{}() with thrown exception: {} (Duration: {} ms)", description, "Failure", className, methodName, e.getMessage(), duration);
            }
            
            throw e;
        }
    }
}