package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;

import java.math.BigDecimal;
import java.util.List;

public interface TransferDao {

    // Creates new transfer, returns transfer_id
    String createTransfer(String sendingUser, String recipientUser, BigDecimal transferAmount);

    // Returns the response Pending, Approved or Denied
    Transfer getTransfer(Integer userId, Integer transferId);

    List<Transfer> getAllTransfers(Integer userId);

    String requestMoney(String requestingUser, String sendingUser, BigDecimal transferAmount);

    List<Transfer> getPendingTransfers(Integer userId);

    String approveRequest (Integer transferId, String status);


}
