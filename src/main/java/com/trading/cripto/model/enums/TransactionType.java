package com.trading.cripto.model.enums;

public enum TransactionType {
    COMPRA("COMPRA"),
    VENTA("VENTA");

    private final String value;

    TransactionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}