package org.geektimes.projects.user.validator.bean.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

/**
 * UserValid
 *
 * @author Ma
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UserValidAnnotationValidator.class)
public @interface UserValid {

    String message() default "用户校验失败：";

    boolean required() default true;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Map<Object, String> ERROR_LIST = new HashMap() {
        private static final long serialVersionUID = -8040490414751218663L;

        {
            put("name", "用户名过短");
            put("email", "邮箱格式不正确");
            put("password", "密码长度必须在【6～32】位");
            put("phoneNumber", "手机号不合法");
        }
    };
}
