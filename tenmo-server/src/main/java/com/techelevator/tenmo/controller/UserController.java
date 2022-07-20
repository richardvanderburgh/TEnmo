package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.*;
import com.techelevator.tenmo.exception.UserIdNotFoundException;
import com.techelevator.tenmo.model.User;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
public class UserController {
    private UserDao userDao;
    private AccountDao accountDao;

    public UserController(UserDao userDao, AccountDao accountDao) {
        this.userDao = userDao;
        this.accountDao = accountDao;
    }

    @ApiOperation("Display user balance")
    @RequestMapping(path = "{userId}/accounts/balance", method = RequestMethod.GET) //todo: url /users/userId/accounts; return all account info
    public BigDecimal getBalance(@PathVariable int userId, Principal principal) throws UserIdNotFoundException {
        if (principal.getName().equals(accountDao.getUsername(userId))) {
            return accountDao.getBalance(userId);
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "SWIPER NO SWIPING");
        }
    }

    @RequestMapping(path = "/users", method = RequestMethod.GET)
    public List<String> listOfUsers() throws UserIdNotFoundException {
        return userDao.getAllUsernames();

    }


}
