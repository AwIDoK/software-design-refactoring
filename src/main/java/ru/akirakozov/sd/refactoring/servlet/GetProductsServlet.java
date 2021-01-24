package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.ResponseBuilder;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static ru.akirakozov.sd.refactoring.dao.ProductDao.getProducts;

/**
 * @author akirakozov
 */
public class GetProductsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ResponseBuilder builder = new ResponseBuilder();

        builder.addProducts(getProducts());

        response.getWriter().println(builder.getResult());
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
