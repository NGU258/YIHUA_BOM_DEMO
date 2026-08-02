package com.yihua.bom.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Date;

@Builder
@Data //解决因为没有get方法导致的序列化(对象转JSON)失败问题  （如果有手写的构造器lombok会跳过 不会生成后重新覆盖）
public class Result<cat> {

    //状态码
    private String code;

    //响应信息（错误或成功）
    private String message;

    //响应数据（具体的值）
    private cat data;

    //请求接口后的响应时间 记载响应的时间戳
    private String timestamp;

    //    将时间戳(直接返回的毫秒值)转成可观的日期格式
    private String timestampFormat(long timestamp){
        return new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒")
                .format(new Date(timestamp)); //这里需要把毫秒值先转成日期格式
    }

    //私有化构造函数（无参 需要自动填充时间戳）
    private Result(){
        timestamp = timestampFormat(System.currentTimeMillis());
    }

    //私有化构造函数（全参 这里不能省 不然使用构造者模式时就会报错 需要自动填充时间戳）
    private Result(String code,String message,cat data,String timestamp){
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = timestampFormat(System.currentTimeMillis()); //不指定的话就是null
    }

    //响应成功信息 这个是比较灵活的写法 可以自己指定传啥 message
    public static<cat> Result<cat> success(String message, cat data){
        return (Result<cat>)Result.builder()
                .code("200")
                .message(message)
                .data(data)
                .build();
    }

    //响应成功信息  只需要传data
    public static<cat> Result<cat> success(cat data){
        //这里进行了相关的优化 直接调用前面写好的方法就可以了
        return success("响应成功",data);
    }

    //响应失败信息 比较灵活的写法
    public static<cat> Result<cat> fail(String code,String message){
        return (Result<cat>)Result.builder()
                .code(code)
                .message(message)
                .build();
    }

    //响应失败信息 偷懒的写法 默认客户端的问题用400来表示、服务端的问题都用500来表示
    //常识： 响应失败就不需要返回数据了
    public static<cat> Result<cat> fail(String message){

        //服务端的问题500就在全局异常处理那个位置捕获进行处理
        return fail("500",message);
    }

}
