package com.trading.cripto.exception;

public class InsufficientFundsExeption extends RuntimeException   {
  public InsufficientFundsExeption(String message) {
    super(message);
  }
}
