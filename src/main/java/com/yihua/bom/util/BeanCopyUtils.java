package com.yihua.bom.util;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Set;

/**
 * <h1 style="color:red;font-family:楷体">
 *     <center>Bean 属性复制工具类</center>
 * </h1>
 * <p style="color:green;font-family:楷体">
 * 封装常用的属性复制操作，提供只复制非 null 属性的方法，
 * 解决 {@link BeanUtils#copyProperties} 会覆盖目标对象中已有值的痛点
 * </p>
 * <br>
 * @author 罗永庆
 * @date 2026/8/2
 */
public class BeanCopyUtils {

    /**
     * 只复制源对象中不为 null 的属性到目标对象
     * <p>
     * 前端没传的字段（DTO 中为 null）不会被复制，
     * 目标对象中已有的值会被保留
     * </p>
     *
     * @param src  源对象（如 DTO）
     * @param dest 目标对象（如 Entity）
     */
    public static void copyNonNull(Object src, Object dest) {
        // 第一步：收集源对象中所有值为 null 的属性名
        Set<String> nullPropertyNames = getNullPropertyNames(src);

        // 第二步：利用 Spring 原生方法，把这些 null 属性排除掉再复制
        BeanUtils.copyProperties(src, dest, nullPropertyNames.toArray(new String[0]));
    }

    /**
     * 获取对象中值为 null 的属性名集合
     */
    private static Set<String> getNullPropertyNames(Object source) {
        BeanWrapper beanWrapper = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = beanWrapper.getPropertyDescriptors();
        Set<String> nullNames = new HashSet<>();

        for (PropertyDescriptor pd : pds) {
            // getClass 是 Object 的 getter，跳过
            if ("class".equals(pd.getName())) {
                continue;
            }
            Object value = beanWrapper.getPropertyValue(pd.getName());
            if (value == null) {
                nullNames.add(pd.getName());
            }
        }
        return nullNames;
    }
}
