package com.gempukku.lotro.db;

import java.sql.*;
import java.util.Date;

public class DbMerchantDAO implements MerchantDAO {
    private DbAccess _dbAccess;

    public DbMerchantDAO(DbAccess dbAccess) {
        _dbAccess = dbAccess;
    }

    @Override
    public void addTransaction(String blueprintId, float price, Date date, TransactionType transactionType) {
        final Transaction lastTransaction = getLastTransaction(blueprintId);
        if (lastTransaction == null) {
            insertTransaction(blueprintId, price, date, transactionType);
        } else {
            updateTransaction(blueprintId, price, date, transactionType);
        }
    }

    private void updateTransaction(String blueprintId, float price, Date date, TransactionType transactionType) {
        try {
            Connection connection = _dbAccess.getDataSource().getConnection();
            try {
                PreparedStatement statement = connection.prepareStatement("update merchant set transaction_price=?, transaction_date=?, transaction_type=? where blueprint_id=?");
                try {
                    statement.setFloat(1, price);
                    statement.setTimestamp(2, new Timestamp(date.getTime()));
                    statement.setString(3, transactionType.name());
                    statement.setString(4, blueprintId);
                    statement.executeUpdate();
                } finally {
                    statement.close();
                }
            } finally {
                connection.close();
            }
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to update last transaction from DB", exp);
        }
    }

    private void insertTransaction(String blueprintId, float price, Date date, TransactionType transactionType) {
        try {
            Connection connection = _dbAccess.getDataSource().getConnection();
            try {
                PreparedStatement statement = connection.prepareStatement("insert into merchant (transaction_price, transaction_date, transaction_type, blueprint_id) values (?,?,?,?)");
                try {
                    statement.setFloat(1, price);
                    statement.setTimestamp(2, new Timestamp(date.getTime()));
                    statement.setString(3, transactionType.name());
                    statement.setString(4, blueprintId);
                    statement.execute();
                } finally {
                    statement.close();
                }
            } finally {
                connection.close();
            }
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to insert last transaction from DB", exp);
        }
    }

    @Override
    public Transaction getLastTransaction(String blueprintId) {
        try {
            Connection connection = _dbAccess.getDataSource().getConnection();
            try {
                PreparedStatement statement = connection.prepareStatement("select blueprint_id, transaction_price, transaction_date, transaction_type from merchant where blueprint_id=?");
                try {
                    statement.setString(1, blueprintId);
                    ResultSet rs = statement.executeQuery();
                    try {
                        if (rs.next()) {
                            float price = rs.getFloat(2);
                            Date date = rs.getTimestamp(3);
                            String type = rs.getString(4);

                            return new Transaction(date, price, TransactionType.valueOf(type));
                        } else {
                            return null;
                        }
                    } finally {
                        rs.close();
                    }
                } finally {
                    statement.close();
                }
            } finally {
                connection.close();
            }
        } catch (SQLException exp) {
            throw new RuntimeException("Unable to get last transaction from DB", exp);
        }
    }
}
