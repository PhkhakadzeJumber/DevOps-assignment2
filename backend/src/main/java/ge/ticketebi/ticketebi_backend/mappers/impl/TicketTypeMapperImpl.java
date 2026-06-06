package ge.ticketebi.ticketebi_backend.mappers.impl;

import ge.ticketebi.ticketebi_backend.domain.dto.TicketTypeRequest;
import ge.ticketebi.ticketebi_backend.domain.dto.TicketTypeResponse;
import ge.ticketebi.ticketebi_backend.domain.entities.TicketTypeEntity;
import ge.ticketebi.ticketebi_backend.mappers.ReqResMapper;
import org.springframework.stereotype.Component;

@Component
public class TicketTypeMapperImpl implements ReqResMapper<TicketTypeEntity, TicketTypeRequest, TicketTypeResponse> {

    @Override
    public TicketTypeEntity toEntity(TicketTypeRequest req) {
        TicketTypeEntity entity = new TicketTypeEntity();
        entity.setName(req.getName());
        entity.setDescription(req.getDescription());
        entity.setPrice(req.getPrice());
        entity.setQuantityTotal(req.getQuantityTotal());
        entity.setMaxPurchase(req.getMaxPurchase());
        entity.setSaleStartTime(req.getSaleStartTime());
        entity.setSaleEndTime(req.getSaleEndTime());

        entity.setQuantityReserved(0);
        entity.setQuantitySold(0);

        return entity;
    }

    @Override
    public TicketTypeResponse toResponseDto(TicketTypeEntity entity) {
        return new TicketTypeResponse(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getQuantityTotal(),
                entity.getQuantityReserved(),
                entity.getQuantitySold(),
                entity.getMaxPurchase(),
                entity.getAvailableQuantity(),
                entity.getSaleStartTime(),
                entity.getSaleEndTime()
        );
    }
}
