package com.yihua.bom.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Step{
    private String operationStepId;
    private Long operationStepNum;
    private String operationStepCode;
    private String operationStepName;
    private String operationStepAlias;
    private String description;
    private String operationStepType;
    private Boolean keyStepFlag;
    private String processRule;
    private String collectorId;
    private Object collector;
    private String remark;
    private Boolean enabled;
}