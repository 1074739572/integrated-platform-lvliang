package com.iflytek.integrated.platform.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iflytek.medicalboot.core.dto.Response;
import com.iflytek.medicalboot.core.exception.MedicalBusinessException;
import com.iflytek.medicalboot.core.exception.MedicalFatalException;
import com.iflytek.medicalboot.core.exception.MedicalHttpStatusCodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private static final Logger logger = LoggerFactory.getLogger(com.iflytek.medicalboot.core.exception.GlobalExceptionHandler.class);
    public static final String ERROR_CODE = "500";
    public static final String ERROR_MSG = "系统异常，请联系管理员。";

    @Value("${medical.isPrintStackTrace:false}")
    private boolean isPrintStackTrace;
    @Value("${medical.isPrintValidationJson:false}")
    private boolean isPrintValidationJson;
    @Value("${medical.isPrintBeanValidation:true}")
    private boolean isPrintBeanValidation;
    @Value("${medical.isPrintBindException:${medical.isPrintBeanValidation:true}}")
    private boolean isPrintBindException;
    @Autowired
    private ObjectMapper objectMapper;

    @ResponseBody
    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<Object> exceptionHandler(Exception e) {
        Response<Object> resp = null;
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (e instanceof MedicalBusinessException) {
            resp = ((MedicalBusinessException) e).getResponse();
        } else if (e instanceof MedicalHttpStatusCodeException) {
            MedicalHttpStatusCodeException statusCodeException = (MedicalHttpStatusCodeException) e;
            resp = statusCodeException.getResponse();
            status = HttpStatus.valueOf(statusCodeException.getStatusCode());
        } else if (e instanceof MedicalFatalException) {
            // 监控平台 配置监控MedicalFatalException并告警
            resp = new Response<>(ERROR_CODE, ERROR_MSG, getStackTrace(e));
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        } else if (e instanceof HttpStatusCodeException) {
            HttpStatusCodeException ex = (HttpStatusCodeException) e;
            String body = ex.getResponseBodyAsString();
            // RestTemplate调用错误, 尽可能将上级错误信息返回
            if (StringUtils.hasText(body)) {
                try {
                    resp = objectMapper.readValue(body, Response.class);
                } catch (Exception exception) {
                    resp = new Response<>(Integer.toString(ex.getRawStatusCode()), body, getStackTrace(ex));
                }
            } else {
                resp = new Response<>(Integer.toString(ex.getRawStatusCode()), ex.getMessage(), getStackTrace(ex));
            }
            status = HttpStatus.valueOf(ex.getRawStatusCode());
        } else if (e instanceof MethodArgumentNotValidException) {
            // BeanValidation exception
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            resp = new Response<>(Integer.toString(HttpStatus.BAD_REQUEST.value()), ERROR_MSG, getStackTrace(ex));
            if (isPrintBeanValidation) {
                setValidationErrorsMsg(resp, ex.getBindingResult());
            }
        } else if (e instanceof ConstraintViolationException) {
            // BeanValidation GET simple param
            ConstraintViolationException ex = (ConstraintViolationException) e;
            resp = new Response<>(Integer.toString(HttpStatus.BAD_REQUEST.value()), ERROR_MSG, getStackTrace(ex));
            if (isPrintBeanValidation) {
                setValidationConstraintViolationMsg(resp, ex.getConstraintViolations());
            }
        } else if (e instanceof BindException) {
            // BeanValidation GET object param
            BindException ex = (BindException) e;
            resp = new Response<>(Integer.toString(HttpStatus.BAD_REQUEST.value()), ERROR_MSG, getStackTrace(ex));
            if (isPrintBindException) {
                setValidationErrorsMsg(resp, ex.getBindingResult());
            }
        } else if(e instanceof RuntimeException){
            //如果是后端抛出的错误，返回抛出的原因
            resp = new Response<>(Integer.toString(HttpStatus.EXPECTATION_FAILED.value()), e.getMessage(), null);
        }
        else {
            resp = new Response<>(ERROR_CODE, ERROR_MSG, getStackTrace(e));
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            if (isPrintStackTrace) {
                resp.setMsg(e.getMessage());
            }
        }
        return new ResponseEntity(resp, status);
    }

    private void setValidationConstraintViolationMsg(Response<Object> resp, Set<ConstraintViolation<?>> constraintViolations) {
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

                resp.setMsg(objectMapper.writeValueAsString(errors));
            } catch (Exception exc) {
                logger.error("setValidationConstraintViolationMsg", exc);
            }
        } else {
            resp.setMsg(constraintViolations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", ")));
        }
    }

    private void setValidationErrorsMsg(Response<Object> resp, BindingResult bindingResult) {
        if (isPrintValidationJson) {
            try {
                List<AbstractMap.SimpleEntry<String, String>> errors = bindingResult.getAllErrors().stream()
                        .map(oe -> new HashMap.SimpleEntry<>(((FieldError) oe).getField(), oe.getDefaultMessage()))
                        .collect(Collectors.toList());

                resp.setMsg(objectMapper.writeValueAsString(errors));
            } catch (Exception exc) {
                logger.error("setValidationErrorsMsg", exc);
            }
        } else {
            resp.setMsg(bindingResult.getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining(", ")));
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
