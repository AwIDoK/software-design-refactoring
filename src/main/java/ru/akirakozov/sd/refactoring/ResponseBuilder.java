package ru.akirakozov.sd.refactoring;

import ru.akirakozov.sd.refactoring.model.Product;

import java.util.List;

public class ResponseBuilder {
    StringBuffer stringBuilder = new StringBuffer();
    public ResponseBuilder() {
        appendLine("<html><body>");
    }

    public void addProduct(Product product) {
        stringBuilder.append(product.getName()).append("\t").append(product.getPrice()).append("</br>");
        stringBuilder.append(System.lineSeparator());
    }

    public void addProducts(List<Product> products) {
        for (Product product : products) {
            addProduct(product);
        }
    }

    public void appendLine(String line) {
        stringBuilder.append(line);
        stringBuilder.append(System.lineSeparator());
    }

    public String getResult() {
        stringBuilder.append("</body></html>");
        return stringBuilder.toString();
    }
}
