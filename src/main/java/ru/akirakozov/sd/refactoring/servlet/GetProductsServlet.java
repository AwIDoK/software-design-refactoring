package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.ResponseBuilder;
import ru.akirakozov.sd.refactoring.model.Product;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @author akirakozov
 */
public class GetProductsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ResponseBuilder builder = new ResponseBuilder();

        try {
            try (Connection c = DriverManager.getConnection("jdbc:sqlite:test.db")) {
                Statement stmt = c.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM PRODUCT");

                while (rs.next()) {
                    String name = rs.getString("name");
                    int price  = rs.getInt("price");
                    builder.addProduct(new Product(name, price));
                }
                rs.close();
                stmt.close();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        response.getWriter().println(builder.getResult());
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
