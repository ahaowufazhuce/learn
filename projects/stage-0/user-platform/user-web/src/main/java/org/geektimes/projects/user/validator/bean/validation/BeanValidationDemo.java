package org.geektimes.projects.user.validator.bean.validation;

import org.geektimes.projects.user.domain.User;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

/**
 * BeanValidationDemo
 *
 * @author Ma
 */
public class BeanValidationDemo {

    public static void main(String[] args) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        User user = new User();
        user.setName("测测");
        user.setPassword("q1111");
        user.setEmail("1111@11111");
        user.setPhoneNumber("18100171897");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        violations.forEach(c -> {
            System.out.println(c.getMessage());
        });
    }
}
