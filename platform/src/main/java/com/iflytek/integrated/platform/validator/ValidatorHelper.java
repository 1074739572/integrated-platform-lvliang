package com.iflytek.integrated.platform.validator;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

/**
 * @description: InitializingBean接口为bean提供了初始化方法的方式，它只包括afterPropertiesSet方法，
 * 凡是继承该接口的类，在初始化bean的时候都会执行该方法。Spring初始化完成后，
 * 会回调ValidatorImpl的afterPropertiesSet()方法
 * @author:
 */
@Component
public class ValidatorHelper implements InitializingBean {
    @Autowired
    private Validator validator;

    @Override
    public void afterPropertiesSet() throws Exception {
        // 将hibernate validator通过工厂的初始化方式使其实例化
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    /**
     * 实现校验方法并返回校验结果
     * @param bean
     * @return
     */
    public ValidationResult validate(Object bean) {
        ValidationResult validationResult = new ValidationResult();
        Set<ConstraintViolation<Object>> constraintViolationSet = validator.validate(bean);
        if (constraintViolationSet.size() > 0) {
            // 大于0 表示有错误
            validationResult.setHasErrors(true);
            for (ConstraintViolation<Object> constraintViolation : constraintViolationSet) {
                String errorMsg = constraintViolation.getMessage();
                String propertyName = constraintViolation.getPropertyPath().toString();
                validationResult.getErrorMsgMap().put(propertyName, errorMsg);
            }
        }
        return validationResult;
    }

}
