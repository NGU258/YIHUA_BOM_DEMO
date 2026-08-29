package com.yihua.bom.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EsopFile{
        private String documentId;
        private String fileName;
        private String fileType;
        private String version;
        private String onlineViewUrl;
        private OffsetDateTime urlExpiresAt; //URL失效时间 | LocalDateTime不含时区 得用OffsetDateTime  这里在BOM_Demo项目中已验证通过  2026-08-21T12:30:00+08:00用LocalDateTime接收会解析失败
        private OffsetDateTime effectiveFrom;//生效时间 | 这里AI说 fastjson 1.x（当前项目1.2.76） 对 OffsetDateTime 的支持是出了名的不稳定 可能会反序列化失败(已验证当前项目不会出现这种情况)
        private OffsetDateTime effectiveTo;//失效时间 | 分析： 这里会带时区 所以不能用LocalDateTime 然后这里因为fastJson版本的问题用OffsetDateTime会有反序列化失败的风险 如果前面解析失败的话最稳的做法就是用字符串类型String来接收(因为JSON里面是没有日期这个类型的，所以传过来的日期其实还是个字符串，这里的逻辑是后端不会对时间字段进行逻辑处理 都统一交给前端来解析处理显示）
        private String status;
        private Long fileSize;
        private String checksum;
}