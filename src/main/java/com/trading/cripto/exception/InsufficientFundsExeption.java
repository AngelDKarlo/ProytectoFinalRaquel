package com.trading.cripto.exception;

public class InsufficientFundsExeption extends RuntimeException {
    public InsufficientFundsExeption(String message) {
        super(message);
    }

    public InsufficientFundsExeption(String message, Throwable cause) {
        super(message, cause);
    }
}
