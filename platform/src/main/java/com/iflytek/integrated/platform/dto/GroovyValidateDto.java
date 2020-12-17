package com.iflytek.integrated.platform.dto;

import lombok.Data;

/**
 * @author czzhan
 * 校验groovy脚本格式是否正确
 */
@Data
public class GroovyValidateDto {

    private String validResult;

    private String validStatus;

    public enum RESULT{
        //返回成功
        SUCCESS(Boolean.TRUE,"success"),
        //返回失败
        ERROR(Boolean.FALSE,"");

        private Boolean flag;
        private String type;

        RESULT(Boolean flag, String type){
            this.flag = flag;
            this.type = type;
        }

        public Boolean getFlag(Boolean flag, String type) {
            return flag;
        }

        public void setFlag(Boolean flag) {
            this.flag = flag;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}