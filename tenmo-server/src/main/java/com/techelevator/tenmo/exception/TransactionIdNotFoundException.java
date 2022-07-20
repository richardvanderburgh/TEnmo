package com.techelevator.tenmo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Transaction ID Not Found")
public class TransactionIdNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    public TransactionIdNotFoundException() {
        super("Transaction ID Not Found");
    }

}
