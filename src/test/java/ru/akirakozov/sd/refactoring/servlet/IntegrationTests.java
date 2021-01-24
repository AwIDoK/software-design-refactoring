package ru.akirakozov.sd.refactoring.servlet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import ru.akirakozov.sd.refactoring.servlet.AddProductServlet;
import ru.akirakozov.sd.refactoring.servlet.GetProductsServlet;
import ru.akirakozov.sd.refactoring.servlet.QueryServlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class IntegrationTests {
    final static private String TEST_DB = "fortest.db";


    @Spy
    private AddProductServlet addProductServlet;
    @Spy
    private GetProductsServlet getProductsServlet;
    @Spy
    private QueryServlet queryServlet;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private StringWriter stringWriter;
    private PrintWriter printWriter;
    private AutoCloseable mocks;

    @Before
    public void setUp() throws SQLException, IOException {
        createTable();
        mocks = MockitoAnnotations.openMocks(this);
        addWriterToResponse();
    }

    private void addWriterToResponse() throws IOException {
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
    }

    @After
    public void clean() throws Exception {
        stringWriter.close();
        printWriter.close();
        mocks.close();
        Files.deleteIfExists(Path.of(TEST_DB));
    }

    private Connection getTestMemoryConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + TEST_DB);
    }

    private void createTable() throws SQLException {
        try (Connection c = getTestMemoryConnection()) {
            String sql = "CREATE TABLE IF NOT EXISTS PRODUCT" +
                    "(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    " NAME           TEXT    NOT NULL, " +
                    " PRICE          INT     NOT NULL)";
            Statement stmt = c.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        }
    }

    // Такое странное решение, так как DriverManager.getConnection не заработал внутри answer для mockito,
    // при этом connection можно использовать только один раз.
    private void invokeMethodWithTestDatabase(Callable<Void> method) throws Exception {
        try (Connection connection = getTestMemoryConnection()) {
            try (MockedStatic<DriverManager> driverManagerMock = Mockito.mockStatic(DriverManager.class)) {
                driverManagerMock.when(() ->
                        DriverManager.getConnection("jdbc:sqlite:test.db"))
                        .thenReturn(connection);

                method.call();
            }
        }
        resetMocks();
    }

    private void addProduct(String name, Long price, int expectedCode, String expectedResponse) throws Exception {
        invokeMethodWithTestDatabase(() -> {
            when(request.getParameter("name")).thenReturn(name);
            when(request.getParameter("price")).thenReturn(String.valueOf(price));

            addProductServlet.doGet(request, response);

            verify(request).getParameter("name");
            verify(request).getParameter("price");

            verify(response).setStatus(expectedCode);
            verify(response).setContentType("text/html");
            assertEquals(expectedResponse + System.lineSeparator(), stringWriter.toString());
            return null;
        });
    }


    private void getProducts(int expectedCode, String expectedResponse) throws Exception {
        invokeMethodWithTestDatabase(() -> {
            getProductsServlet.doGet(request, response);

            verify(response).setStatus(expectedCode);
            verify(response).setContentType("text/html");
            assertEquals(expectedResponse + System.lineSeparator(), stringWriter.toString());
            return null;
        });
    }

    private void query(String command, int expectedCode, String expectedResponse) throws Exception {
        invokeMethodWithTestDatabase(() -> {
            when(request.getParameter("command")).thenReturn(command);

            queryServlet.doGet(request, response);

            verify(request).getParameter("command");

            verify(response).setStatus(expectedCode);
            verify(response).setContentType("text/html");
            assertEquals(expectedResponse + System.lineSeparator(), stringWriter.toString());
            return null;
        });
    }

    @Test
    public void testEmptyQuery() throws Exception {
        query("max", HttpServletResponse.SC_OK,
                "<html><body>" + System.lineSeparator() +
                        "<h1>Product with max price: </h1>" + System.lineSeparator() +
                        "</body></html>");

        query("min", HttpServletResponse.SC_OK,
                "<html><body>" + System.lineSeparator() +
                        "<h1>Product with min price: </h1>" + System.lineSeparator() +
                        "</body></html>");

        query("count", HttpServletResponse.SC_OK,
                "<html><body>" + System.lineSeparator() +
                        "Number of products: " + System.lineSeparator() +
                        "0" + System.lineSeparator() +
                        "</body></html>");

        query("sum", HttpServletResponse.SC_OK,
                "<html><body>" + System.lineSeparator() +
                        "Summary price: " + System.lineSeparator() +
                        "0" + System.lineSeparator() +
                        "</body></html>");
    }

    @Test
    public void testInvalidQuery() throws Exception {
        query("unknown", HttpServletResponse.SC_OK, "Unknown command: unknown");
    }

    @Test(expected = NumberFormatException.class)
    public void testInvalidAddPrice() throws Exception {
        addProduct("object1", null, HttpServletResponse.SC_OK, "OK");
    }

    @Test
    public void integrationTest() throws Exception {
        addProduct("object1", 1L, HttpServletResponse.SC_OK, "OK");
        addProduct("object2", 2L, HttpServletResponse.SC_OK, "OK");
        addProduct("object3", 3L, HttpServletResponse.SC_OK, "OK");

        query("max", HttpServletResponse.SC_OK,
                "<html><body>" + System.lineSeparator() +
                        "<h1>Product with max price: </h1>" + System.lineSeparator() +
                        "object3\t3</br>" + System.lineSeparator() +
                        "</body></html>");

        query("min", HttpServletResponse.SC_OK,
                "<html><body>" + System.lineSeparator() +
                        "<h1>Product with min price: </h1>" + System.lineSeparator() +
                        "object1\t1</br>" + System.lineSeparator() +
                        "</body></html>");

        query("count", HttpServletResponse.SC_OK,
                "<html><body>" + System.lineSeparator() +
                        "Number of products: " + System.lineSeparator() +
                        "3" + System.lineSeparator() +
                        "</body></html>");

        query("sum", HttpServletResponse.SC_OK,
                "<html><body>" + System.lineSeparator() +
                        "Summary price: " + System.lineSeparator() +
                        "6" + System.lineSeparator() +
                        "</body></html>");

        getProducts(HttpServletResponse.SC_OK,
                "<html><body>" + System.lineSeparator() +
                        "object1\t1</br>" + System.lineSeparator() +
                        "object2\t2</br>" + System.lineSeparator() +
                        "object3\t3</br>" + System.lineSeparator() +
                        "</body></html>");
    }

    private void resetMocks() throws IOException {
        Mockito.reset(request);
        Mockito.reset(response);
        stringWriter.close();
        printWriter.close();
        addWriterToResponse();
    }
}
