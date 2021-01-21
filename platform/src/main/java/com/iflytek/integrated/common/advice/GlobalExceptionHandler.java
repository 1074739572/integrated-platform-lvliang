package com.iflytek.integrated.common.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iflytek.integrated.common.dto.ResultDto;
import com.iflytek.medicalboot.core.dto.Response;
import com.iflytek.medicalboot.core.exception.MedicalBusinessException;
import com.iflytek.medicalboot.core.exception.MedicalFatalException;
import com.iflytek.medicalboot.core.exception.MedicalHttpStatusCodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.HttpStatusCodeException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 全局统一异常处理
 * @author
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    public static final Integer ERROR_CODE = 500;
    public static final String ERROR_MSG = "系统异常，请联系管理员。";

    @Value("true")
    private boolean isPrintStackTrace;
    @Value("true")
    private boolean isPrintValidationJson;
    @Value("true")
    private boolean isPrintBeanValidation;
    @Value("true")
    private boolean isPrintBindException;
    @Autowired
    private ObjectMapper objectMapper;

    @ResponseBody
    @ExceptionHandler(value = {Exception.class})
    public ResultDto exceptionHandler(Exception e) {
        ResultDto resultDto = new ResultDto(ERROR_CODE,ERROR_MSG,"");
        if (e instanceof MedicalBusinessException) {
            resultDto.setData(((MedicalBusinessException) e).getResponse());
        } else if (e instanceof MedicalHttpStatusCodeException) {
            MedicalHttpStatusCodeException statusCodeException = (MedicalHttpStatusCodeException) e;
            resultDto.setData(statusCodeException.getResponse());
        } else if (e instanceof MedicalFatalException) {
            // 监控平台 配置监控MedicalFatalException并告警
            resultDto.setData(getStackTrace(e));
        } else if (e instanceof HttpStatusCodeException) {
            HttpStatusCodeException ex = (HttpStatusCodeException) e;
            String body = ex.getResponseBodyAsString();
            // RestTemplate调用错误, 尽可能将上级错误信息返回
            if (StringUtils.hasText(body)) {
                try {
                    resultDto.setData(objectMapper.readValue(body, Response.class));
                } catch (Exception exception) {
                    resultDto.setData(getStackTrace(ex));
                }
            } else {
                resultDto.setData(getStackTrace(ex));
            }
        } else if (e instanceof MethodArgumentNotValidException) {
            // BeanValidation exception
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            resultDto.setData(getStackTrace(ex));
            if (isPrintBeanValidation) {
                setValidationErrorsMsg(resultDto, ex.getBindingResult());
            }
        } else if (e instanceof ConstraintViolationException) {
            // BeanValidation GET simple param
            ConstraintViolationException ex = (ConstraintViolationException) e;
            resultDto.setData(getStackTrace(ex));
            if (isPrintBeanValidation) {
                setValidationConstraintViolationMsg(resultDto, ex.getConstraintViolations());
            }
        } else if (e instanceof BindException) {
            // BeanValidation GET object param
            BindException ex = (BindException) e;
            resultDto.setData(getStackTrace(ex));
            if (isPrintBindException) {
                setValidationErrorsMsg(resultDto, ex.getBindingResult());
            }
        } else if(e instanceof RuntimeException){
            //如果是后端抛出的错误，返回抛出的原因
            resultDto.setData(e.getMessage());
        }
        else {
            resultDto.setData(e.getMessage());
        }
        return resultDto;
    }

    private void setValidationConstraintViolationMsg(ResultDto resultDto, Set<ConstraintViolation<?>> constraintViolations) {
        if (isPrintValidationJson) {
            try {
                List<AbstractMap.SimpleEntry<String, String>> errors = constraintViolations.stream()
                        .map(cv -> {
                            Iterator<Path.Node> it = cv.getPropertyPath().iterator();
                            Path.Node lastNode = it.next();
                            while (it.hasNext()) {
                                lastNode = it.next();
                            }
                            return new HashMap.SimpleEntry<>(lastNode.getName(), cv.getMessage());
                        })
                        .collect(Collectors.toList());

                resultDto.setMessage(objectMapper.writeValueAsString(errors));
            } catch (Exception exc) {
                logger.error("setValidationConstraintViolationMsg", exc);
            }
        } else {
            resultDto.setMessage(constraintViolations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", ")));
        }
    }

    private void setValidationErrorsMsg(ResultDto resp, BindingResult bindingResult) {
        if (isPrintValidationJson) {
            try {
                List<AbstractMap.SimpleEntry<String, String>> errors = bindingResult.getAllErrors().stream()
                        .map(oe -> new HashMap.SimpleEntry<>(((FieldError) oe).getField(), oe.getDefaultMessage()))
                        .collect(Collectors.toList());

                resp.setMessage(objectMapper.writeValueAsString(errors));
            } catch (Exception exc) {
                logger.error("setValidationErrorsMsg", exc);
            }
        } else {
            resp.setMessage(bindingResult.getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining(", ")));
        }
    }

    private String getStackTrace(Exception e) {
        if (isPrintStackTrace) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            return sw.toString();
        } else {
            return ERROR_MSG;
        }
    }

}
