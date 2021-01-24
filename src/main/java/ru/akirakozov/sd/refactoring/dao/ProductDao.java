package ru.akirakozov.sd.refactoring.dao;

import ru.akirakozov.sd.refactoring.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ProductDao {
    final private static String DATABASE = "jdbc:sqlite:test.db";

    public static void ensureTableExists() {
        try (Connection c = DriverManager.getConnection(DATABASE)) {
            String sql = "CREATE TABLE IF NOT EXISTS PRODUCT" +
                    "(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    " NAME           TEXT    NOT NULL, " +
                    " PRICE          INT     NOT NULL)";
            try(Statement stmt = c.createStatement()) {
                stmt.executeUpdate(sql);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addProduct(Product product) {
        try {
            try (Connection c = DriverManager.getConnection(DATABASE)) {
                String sql = "INSERT INTO PRODUCT (NAME, PRICE) VALUES (?, ?)";
                try (PreparedStatement preparedStatement = c.prepareStatement(sql)) {
                    preparedStatement.setString(1, product.getName());
                    preparedStatement.setInt(2, product.getPrice());
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Product> getProducts() {
        return runProductListQuery("SELECT * FROM PRODUCT");
    }

    public static List<Product> getMaxPriceProduct() {
        return runProductListQuery("SELECT * FROM PRODUCT ORDER BY PRICE DESC LIMIT 1");
    }

    public static List<Product> getMinPriceProduct() {
        return runProductListQuery("SELECT * FROM PRODUCT ORDER BY PRICE LIMIT 1");
    }

    public static long getProductCount() {
        return runNumberQuery("SELECT COUNT(*) FROM PRODUCT");
    }

    public static long getSummaryPrice() {
        return runNumberQuery("SELECT SUM(PRICE) FROM PRODUCT");
    }

    private static <T> T runSqlQuery(String sql, Function<ResultSet, T> resultGetter) {
        T result;
        try {
            try (Connection c = DriverManager.getConnection("jdbc:sqlite:test.db")) {
                try (Statement stmt = c.createStatement()) {
                    try (ResultSet rs = stmt.executeQuery(sql)) {
                        result = resultGetter.apply(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private static List<Product> runProductListQuery(String sql) {
        return runSqlQuery(sql, rs -> {
            List<Product> result = new ArrayList<>();
            try {
                while (rs.next()) {
                    result.add(new Product(rs.getString("name"), rs.getInt("price")));
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return result;
        });
    }

    private static long runNumberQuery(String sql) {
        return runSqlQuery(sql, rs -> {
            long result = 0;
            try {
                if (rs.next()) {
                    result = rs.getLong(1);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return result;
        });
    }
}
