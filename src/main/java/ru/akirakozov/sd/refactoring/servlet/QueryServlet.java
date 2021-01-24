package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.ResponseBuilder;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static ru.akirakozov.sd.refactoring.dao.ProductDao.*;

/**
 * @author akirakozov
 */
public class QueryServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String command = request.getParameter("command");
        ResponseBuilder builder = new ResponseBuilder();
        if ("max".equals(command)) {
            builder.appendLine("<h1>Product with max price: </h1>");
            builder.addProducts(getMaxPriceProduct());
        } else if ("min".equals(command)) {
            builder.appendLine("<h1>Product with min price: </h1>");
            builder.addProducts(getMinPriceProduct());
        } else if ("sum".equals(command)) {
            builder.appendLine("Summary price: ");
            builder.appendLine(Long.toString(getSummaryPrice()));
        } else if ("count".equals(command)) {
            builder.appendLine("Number of products: ");
            builder.appendLine(Long.toString(getProductCount()));
        } else {
            response.getWriter().println("Unknown command: " + command);
            builder = null;
        }

        if (builder != null) {
            response.getWriter().println(builder.getResult());
        }

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
