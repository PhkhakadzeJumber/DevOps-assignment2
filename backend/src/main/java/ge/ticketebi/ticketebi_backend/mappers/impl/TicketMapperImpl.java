package ge.ticketebi.ticketebi_backend.mappers.impl;

import ge.ticketebi.ticketebi_backend.domain.dto.TicketResponse;
import ge.ticketebi.ticketebi_backend.domain.entities.TicketEntity;
import ge.ticketebi.ticketebi_backend.mappers.Mapper;
import org.springframework.stereotype.Component;

@Component
public class TicketMapperImpl implements Mapper<TicketEntity, TicketResponse> {

    @Override
    public TicketResponse mapTo(TicketEntity ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .qrCode(ticket.getQrCode())
                .seatInfo(ticket.getSeatInfo())
                .email(ticket.getEmail())
                .status(ticket.getStatus())
                .checkedInAt(ticket.getCheckedInAt())
                .ticketTypeId(ticket.getTicketType().getId())
                .ticketTypeName(ticket.getTicketType().getName())
                .build();
    }

    @Override
    public TicketEntity mapFrom(TicketResponse ticketResponse) {
        throw new UnsupportedOperationException("TicketResponse to TicketEntity mapping is not supported");
    }
}
