package com.techelevator.tenmo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "User ID Not Found")
public class UserIdNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    public UserIdNotFoundException() {
        super("User ID Not Found");
    }

}
