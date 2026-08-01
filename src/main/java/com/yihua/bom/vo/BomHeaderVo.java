package com.yihua.bom.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BomHeaderVo {
    private Long id;
    private String bomCode;
    private String bomName;
    private String status;
}
