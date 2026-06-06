package ge.ticketebi.ticketebi_backend.domain.entities;

public enum OrderStatus {
    DRAFT("draft"),
    PENDING("pending"),
    CONFIRMED("confirmed"),
    CANCELLED("cancelled"),
    COMPLETED("completed"),
    REFUNDED("refunded");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
