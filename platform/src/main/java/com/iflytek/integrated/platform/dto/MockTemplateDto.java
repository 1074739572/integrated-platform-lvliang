package com.iflytek.integrated.platform.dto;

import lombok.Data;

/**
 * @author czzhan
 * @version 1.0
 * @date 2021/1/11 16:57
 */
@Data
public class MockTemplateDto {

    String id;

    String mockTemplate;

    String mockStatus;

    Integer mockIsUse;

    Integer excErrOrder;

    public MockTemplateDto(String id, String mockTemplate, String mockStatus,
                           Integer mockIsUse, Integer excErrOrder){
        this.id = id;
        this.mockTemplate = mockTemplate;
        this.mockStatus = mockStatus;
        this.mockIsUse = mockIsUse;
        this.excErrOrder = excErrOrder;
    }
}
