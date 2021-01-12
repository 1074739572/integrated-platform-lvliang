package com.iflytek.integrated.common;

import lombok.Data;
import org.apache.commons.lang.StringUtils;

/**
 * @author czzhan
 * @version 1.0
 * @date 2021/1/12 9:18
 */
@Data
public class UserDto {

    String loginUserId;

    String loginUserName;

    String name;

    public String getLoginUserName() {
        return StringUtils.isNotEmpty(name)?name:loginUserName;
    }
}
