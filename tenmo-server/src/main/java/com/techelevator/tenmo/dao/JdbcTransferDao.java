package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDao implements TransferDao {

    private JdbcTemplate jdbcTemplate;

    private JdbcAccountDao jdbcAccountDao;
    private JdbcUserDao jdbcUserDao;

    public JdbcTransferDao(DataSource dataSource, JdbcAccountDao jdbcAccountDao, JdbcUserDao jdbcUserDao) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcAccountDao = jdbcAccountDao;
        this.jdbcUserDao = jdbcUserDao;
    }

    @Override
    public String createTransfer(String sendingUser, String recipientUser, BigDecimal transferAmount) {
        boolean senderHasEnoughMoney = false;
        boolean senderIsNotRecipient = false;
        boolean transferAmountIsGreaterThanZero = false;

//        System.out.println("transferAmount = "+ transferAmount);
        BigDecimal senderBalance = jdbcAccountDao.getBalance(jdbcUserDao.findIdByUsername(sendingUser));

        if (senderBalance.compareTo(transferAmount) != -1) {
            senderHasEnoughMoney = true;
            System.out.println("senderHasEnoughMoney");
        }

        if (!sendingUser.equals(recipientUser)) {
            senderIsNotRecipient = true;
            System.out.println("senderIsNotRecipient");
        }

        if (transferAmount.compareTo(BigDecimal.ZERO) == 1) {
            transferAmountIsGreaterThanZero = true;
            System.out.println("transferAmountIsGreaterThanZero");
        }

        // create transfer
        String sql = "INSERT INTO transfer (sender_account_id, recipient_account_id, transfer_amount, date, time, status) " +
                "VALUES ((SELECT account_id as ais FROM tenmo_user as tu " +
                "JOIN account as a ON tu.user_id = a.user_id " +
                "WHERE username = ?), " +
                "(SELECT account_id as air FROM tenmo_user as tu " +
                "JOIN account as a ON tu.user_id = a.user_id " +
                "WHERE username = ?), ?, CURRENT_DATE, CURRENT_TIME, 'Approved') RETURNING status;";

//        System.out.println("account balance = " + account1.getBalance());
        if (!senderHasEnoughMoney || !senderIsNotRecipient || !transferAmountIsGreaterThanZero) {
            return "Invalid transfer";
        } else {

            adjustSenderBalance(sendingUser, transferAmount);
            adjustRecipientBalance(recipientUser, transferAmount);

            String status = jdbcTemplate.queryForObject(sql, String.class, sendingUser, recipientUser, transferAmount);
            return status;
        }
    }

    private void adjustSenderBalance(String sendingUser, BigDecimal withdrawAmount) {
        String sql = "UPDATE account " +
                "SET " +
                "balance = ? " +
                "WHERE user_id = ? RETURNING balance;";

        jdbcTemplate.queryForRowSet(sql, jdbcAccountDao.getBalance(jdbcUserDao.findIdByUsername(sendingUser)).subtract(withdrawAmount), jdbcUserDao.findIdByUsername(sendingUser));
    }

    private void adjustRecipientBalance(String recipientUser, BigDecimal depositAmount) {
        String sql = "UPDATE account " +
                "SET " +
                "balance = ? " +
                "WHERE user_id = ? RETURNING balance;";

        jdbcTemplate.queryForRowSet(sql, jdbcAccountDao.getBalance(jdbcUserDao.findIdByUsername(recipientUser)).add(depositAmount), jdbcUserDao.findIdByUsername(recipientUser));
    }


    @Override
    public Transfer getTransfer(Integer userId, Integer transferId) {

        String sql = "SELECT transfer_id, sender_account_id, recipient_account_id, transfer_amount, date, time, status " +
                "FROM transfer " +
                "JOIN account AS sa ON sa.account_id = transfer.sender_account_id " +
                "JOIN account AS ra ON ra.account_id = transfer.recipient_account_id " +
                "WHERE (sa.user_id = ? OR ra.user_id = ?) AND transfer_id = ?;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, userId, userId, transferId);
        if (rs.next()) {
            return mapRowToTransfer(rs);
        }
        return null;

    }

    @Override
    public List<Transfer> getAllTransfers(Integer userId) {
        List<Transfer> allTransfers = new ArrayList<>();
        String sql = "SELECT * FROM transfer " +
                "JOIN account AS sa ON sa.account_id = transfer.sender_account_id " +
                "JOIN account AS ra ON ra.account_id = transfer.recipient_account_id " +
                "WHERE sa.user_id = ? OR ra.user_id = ?;";
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, userId, userId);
        while (rs.next()) {
            allTransfers.add(mapRowToTransfer(rs));
        }
        return allTransfers;
    }

    @Override
    public String requestMoney(String requestingUser, String sendingUser, BigDecimal transferAmount) {
        boolean senderHasEnoughMoney = false;
        boolean senderIsNotRecipient = false;
        boolean transferAmountIsGreaterThanZero = false;

        BigDecimal senderBalance = jdbcAccountDao.getBalance(jdbcUserDao.findIdByUsername(sendingUser));

        if (senderBalance.compareTo(transferAmount) != -1) {
            senderHasEnoughMoney = true;
            System.out.println("senderHasEnoughMoney");
        }

        if (!sendingUser.equals(requestingUser)) {
            senderIsNotRecipient = true;
            System.out.println("senderIsNotRecipient");
        }

        if (transferAmount.compareTo(BigDecimal.ZERO) == 1) {
            transferAmountIsGreaterThanZero = true;
            System.out.println("transferAmountIsGreaterThanZero");
        }

        // create transfer
        String sql = "INSERT INTO transfer (sender_account_id, recipient_account_id, transfer_amount, date, time, status) " +
                "VALUES ((SELECT account_id as ais FROM tenmo_user as tu " +
                "JOIN account as a ON tu.user_id = a.user_id " +
                "WHERE username = ?), " +
                "(SELECT account_id as air FROM tenmo_user as tu " +
                "JOIN account as a ON tu.user_id = a.user_id " +
                "WHERE username = ?), ?, CURRENT_DATE, CURRENT_TIME, 'Pending') RETURNING status;";

        if (!senderHasEnoughMoney || !senderIsNotRecipient || !transferAmountIsGreaterThanZero) {
            return "Invalid transfer";
        } else {


            String status = jdbcTemplate.queryForObject(sql, String.class, sendingUser, requestingUser, transferAmount);
            return status;
        }
    }

    @Override
    public List<Transfer> getPendingTransfers(Integer userId) {
        List<Transfer> allPendingTransfers = new ArrayList<>();
        String sql = "SELECT * FROM transfer " +
                "JOIN account AS sa ON sa.account_id = transfer.sender_account_id " +
                "JOIN account AS ra ON ra.account_id = transfer.recipient_account_id " +
                "WHERE (sa.user_id = ? OR ra.user_id = ?) AND status = 'Pending';"; //todo: pass status as query parameter
        SqlRowSet rs = jdbcTemplate.queryForRowSet(sql, userId, userId);
        while (rs.next()) {
            allPendingTransfers.add(mapRowToTransfer(rs));
        }
        return allPendingTransfers;
    }

    @Override
    public String approveRequest(Integer transferId, String status) { //todo: get usernames and balance with one SQL query; wrap in try/catch to return 400 error if not found

        String requestingUsername = jdbcTemplate.queryForObject("SELECT username " +
                "FROM transfer " +
                "JOIN account AS ra ON ra.account_id = transfer.recipient_account_id " +
                "JOIN tenmo_user AS tu ON ra.user_id = tu.user_id " +
                "WHERE transfer_id = ?;", String.class, transferId);

        String sendingUsername = jdbcTemplate.queryForObject("SELECT username " +
                "FROM transfer " +
                "JOIN account AS sa ON sa.account_id = transfer.sender_account_id " +
                "JOIN tenmo_user AS tu ON sa.user_id = tu.user_id " +
                "WHERE transfer_id = ?;", String.class, transferId);

        BigDecimal transferAmount = jdbcTemplate.queryForObject("SELECT transfer_amount " +
                "FROM transfer " +
                "JOIN account AS sa ON sa.account_id = transfer.sender_account_id " +
                "JOIN account AS ra ON ra.account_id = transfer.recipient_account_id " +
                "WHERE transfer_id = ?;", BigDecimal.class, transferId);

        String sql = "UPDATE transfer " +
                "SET " +
                "status = ? " +
                "WHERE transfer_id = ?  RETURNING status;";

        if (status.equals("Approved")) {
            jdbcTemplate.queryForObject(sql, String.class, "Approved", transferId);
            adjustSenderBalance(sendingUsername, transferAmount);
            adjustRecipientBalance(requestingUsername, transferAmount);
            return status;
        } else if (status.equals("Denied")) {
            jdbcTemplate.queryForObject(sql, String.class, "Denied", transferId);
            return status;
        } else {
            return "Invalid response.";
        }
    }

    private Transfer mapRowToTransfer(SqlRowSet rs) {
        Transfer transfer = new Transfer();
        transfer.setTransferId(rs.getInt("transfer_id"));
        transfer.setSenderAccountId(rs.getInt("sender_account_id"));
        transfer.setRecipientAccountId(rs.getInt("recipient_account_id"));
        transfer.setAmount(rs.getBigDecimal("transfer_amount"));
        transfer.setDate(rs.getDate("date").toLocalDate());
        transfer.setTime(rs.getTime("time").toLocalTime());
        transfer.setStatus(rs.getString("status"));
        return transfer;
    }
}
