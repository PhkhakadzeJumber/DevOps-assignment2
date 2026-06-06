package ge.ticketebi.ticketebi_backend.validation;

import ge.ticketebi.ticketebi_backend.domain.dto.TicketTypeRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SalePeriodValidator implements ConstraintValidator<ValidSalePeriod, TicketTypeRequest> {

    @Override
    public boolean isValid(TicketTypeRequest dto, ConstraintValidatorContext context) {
        if (dto.getSaleStartTime() == null || dto.getSaleEndTime() == null) {
            return true;
        }

        return dto.getSaleEndTime().isAfter(dto.getSaleStartTime());
    }
}