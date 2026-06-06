package ge.ticketebi.ticketebi_backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SalePeriodValidator.class)
@Documented
public @interface ValidSalePeriod {

    String message() default "Sale end time must be after the start time.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}