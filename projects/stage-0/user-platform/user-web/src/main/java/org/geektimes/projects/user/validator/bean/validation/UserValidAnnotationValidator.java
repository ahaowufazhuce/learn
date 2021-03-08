package org.geektimes.projects.user.validator.bean.validation;

import org.apache.commons.lang.StringUtils;
import org.geektimes.projects.user.domain.User;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.net.IDN;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * UserValidAnnotationValidator
 *
 * @author Ma
 */
public class UserValidAnnotationValidator implements ConstraintValidator<UserValid, User> {

    private static final String EMAIL_ATOM = "[a-z0-9!#$%&'*+/=?^_`{|}~-]";
    private static final String EMAIL_DOMAIN;
    private static final String EMAIL_IP_DOMAIN;
    private static final Pattern EMAIL_LOCAL_PATTERN;
    private static final Pattern EMAIL_DOMAIN_PATTERN;
    private static final Pattern MOBILE;
    private static final Pattern NAME;
    private static final Pattern PASSWORD;


    static {
        EMAIL_DOMAIN = EMAIL_ATOM + "+(\\." + EMAIL_ATOM + "+)*";
        EMAIL_IP_DOMAIN = "\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\]";
        EMAIL_LOCAL_PATTERN = Pattern.compile(EMAIL_ATOM + "+(\\." + EMAIL_ATOM + "+)*", 2);
        EMAIL_DOMAIN_PATTERN = Pattern.compile(EMAIL_DOMAIN + "|" + EMAIL_IP_DOMAIN, 2);
        MOBILE = Pattern.compile("(?:0|86|\\+86)?1[3-9]\\d{9}");
        NAME = Pattern.compile(".{2,20}$");
        PASSWORD = Pattern.compile(".{6,33}$");
    }

    @Override
    public void initialize(UserValid annotation) {
    }

    @Override
    public boolean isValid(User value, ConstraintValidatorContext context) {
        String message = context.getDefaultConstraintMessageTemplate();
        List<String> messageList = new ArrayList<>();
        if (value.getName() == null || !NAME.matcher(value.getName()).matches()) {
            messageList.add(UserValid.ERROR_LIST.get("name"));
        }
        if (value.getPassword() == null || !PASSWORD.matcher(value.getPassword()).matches()) {
            messageList.add(UserValid.ERROR_LIST.get("password"));
        }
        if (value.getEmail() == null || !this.validEmail(value.getEmail(), messageList)) {
            messageList.add(UserValid.ERROR_LIST.get("email"));
        }
        if (value.getPhoneNumber() == null || !MOBILE.matcher(value.getPhoneNumber()).matches()) {
            messageList.add(UserValid.ERROR_LIST.get("phoneNumber"));
        }
        message = message + StringUtils.join(messageList, "，");
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        if (messageList.size() != 0) {
            throw new IllegalArgumentException(message);
        }
        return true;
    }

    private boolean validEmail(String email, List<String> messageList) {
        if (email != null && email.length() != 0) {
            String[] emailParts = email.split("@", 3);
            if (emailParts.length != 2) {
                return false;
            } else if (!emailParts[0].endsWith(".") && !emailParts[1].endsWith(".")) {
                return !this.matchPart(emailParts[0], EMAIL_LOCAL_PATTERN) ? false : this.matchPart(emailParts[1], EMAIL_DOMAIN_PATTERN);
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    private boolean matchPart(String part, Pattern pattern) {
        try {
            part = IDN.toASCII(part);
        } catch (IllegalArgumentException var4) {
            return false;
        }
        Matcher matcher = pattern.matcher(part);
        return matcher.matches();
    }
}
