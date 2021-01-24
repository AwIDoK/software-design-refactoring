package ru.akirakozov.sd.refactoring.dao;

import ru.akirakozov.sd.refactoring.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ProductDao {
    final private static String DATABASE = "jdbc:sqlite:test.db";

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
}
