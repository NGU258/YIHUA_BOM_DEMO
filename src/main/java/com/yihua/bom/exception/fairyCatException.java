package com.yihua.bom.exception;


import lombok.Data;

//继承RuntimeExcepiton的话 就不需要让别人手动写try-catch捕获异常了
//因为RuntimeException是运行时异常，或者说是非受检异常，所以可以不用手动去调用try-catch块来捕获异常
@Data //这里并不会生成父类的message属性对应的get/set 只会生成当前类已经有的 比如这个code
public class fairyCatException  extends RuntimeException{

//    状态码
    private String code;

    //偷懒写法 状态码默认400表示调用方问题
    public fairyCatException(String message){
        super(message); //调用父类的构造方法 ，将消息传递上去 这样调用getMessage方法的时候就有值了
        code = "400";
    }

    //灵活写法 自定义状态码
    public fairyCatException(String code,String message){
        super(message);
        this.code = code;
    }

}
