package com.trading.cripto.exception;

public class TradingExeption extends RuntimeException {
    public TradingExeption(String message) {
        super(message);
    }

    public TradingExeption(String message, Throwable cause) {
        super(message, cause);
    }
}
