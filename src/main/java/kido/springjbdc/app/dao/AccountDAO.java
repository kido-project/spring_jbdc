package kido.springjbdc.app.dao;

import kido.springjbdc.app.mapper.AccountMapper;
import kido.springjbdc.app.model.Account;
import kido.springjbdc.app.exception.TransactionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;

@Repository
@Transactional
public class AccountDAO extends JdbcDaoSupport {
    @Autowired
    public AccountDAO(DataSource dataSource) {
        this.setDataSource(dataSource);
    }

    public List<Account> getBankAccounts() {
        // Select ba.Id, ba.Full_Name, ba.Balance From Bank_Account ba
        String sql = AccountMapper.BASE_SQL;

        Object[] params = new Object[] {};
        AccountMapper mapper = new AccountMapper();
        List<Account> accounts = this.getJdbcTemplate().query(sql, mapper, params);
        return accounts;
    }

    private Account findBankAccount(Long id) {
        // Select ba.Id, ba.Full_Name, ba.Balance From Bank_Account ba
        // Where ba.Id = ?
        String sql = AccountMapper.BASE_SQL + " where Id = ? ";

        Object[] params = new Object[] { id };
        AccountMapper mapper = new AccountMapper();
        try {
            Account account = this.getJdbcTemplate().queryForObject(sql, mapper, params);
            return account;
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    // MANDATORY: Giao dịch bắt buộc phải được tạo sẵn trước đó.
    @Transactional(propagation = Propagation.MANDATORY)
    public void addAmount(Long id, double amount) throws TransactionException {
        Account accountInfo = this.findBankAccount(id);
        if (accountInfo == null) {
            throw new TransactionException("Account not found " + id);
        }
        double newBalance = accountInfo.getBalance() + amount;
        if (accountInfo.getBalance() + amount < 0) {
            throw new TransactionException(
                    "The money in the account '" + id + "' is not enough (" + accountInfo.getBalance() + ")");
        }
        accountInfo.setBalance(newBalance);
        // Update to DB
        String sqlUpdate = "Update Bank_Account set Balance = ? where Id = ?";
        this.getJdbcTemplate().update(sqlUpdate, accountInfo.getBalance(), accountInfo.getId());
    }

    // Không được bắt BankTransactionException trong phương thức này.
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = TransactionException.class)
    public void sendMoney(Long fromAccountId, Long toAccountId, double amount) throws TransactionException {

        addAmount(toAccountId, amount);
        addAmount(fromAccountId, -amount);
    }
}
