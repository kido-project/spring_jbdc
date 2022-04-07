package kido.springjbdc.app.mapper;

import kido.springjbdc.app.model.Account;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountMapper implements RowMapper<Account> {
    public static final String BASE_SQL
            = "Select Id, Full_Name, Balance From Bank_Account";

    @Override
    public Account mapRow(ResultSet rs, int rowNum) throws SQLException {
        Long id = rs.getLong("Id");
        String fullName = rs.getString("Full_Name");
        Double balance = rs.getDouble("Balance");

        return new Account(id, fullName, balance);
    }
}
