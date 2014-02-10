package annotations;

import java.lang.annotation.ElementType;

/**
 * User: 刘建力(liujianli@gtadata.com))
 * Date: 13-3-26
 * Time: 下午2:58
 * 功能描述:
 */
@java.lang.annotation.Target({ElementType.TYPE})
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface Process {

    String name()  default "";
    boolean singleton() default true;
}
