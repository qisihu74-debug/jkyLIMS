package com.lims.manage.erp.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@RestController
public class LDocController {

    private static final String ROUTE_PREFIX = "/l-docs/";

    @Value("${docstore.proxy-url:http://127.0.0.1:8898}")
    private String proxyUrl;

    @RequestMapping("/l-docs/**")
    public void proxy(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String requestUri = request.getRequestURI();
        String contextPrefix = request.getContextPath() + ROUTE_PREFIX;
        if (!requestUri.startsWith(contextPrefix)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String objectPath = requestUri.substring(contextPrefix.length());
        if (objectPath.isEmpty() || objectPath.contains("..") || objectPath.contains("\\")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        HttpURLConnection connection = (HttpURLConnection) new URL(
                proxyUrl + "/" + objectPath
        ).openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(60000);
        connection.setRequestMethod(request.getMethod());

        String range = request.getHeader("Range");
        if (range != null) {
            connection.setRequestProperty("Range", range);
        }

        int status = connection.getResponseCode();
        response.setStatus(status);
        copyHeader(connection, response, "Content-Type");
        copyHeader(connection, response, "Content-Length");
        copyHeader(connection, response, "Content-Range");
        copyHeader(connection, response, "Last-Modified");
        copyHeader(connection, response, "Accept-Ranges");
        response.setHeader("Cache-Control", "private, max-age=3600");

        if ("HEAD".equalsIgnoreCase(request.getMethod())) {
            connection.disconnect();
            return;
        }

        InputStream input = status >= 400
                ? connection.getErrorStream()
                : connection.getInputStream();
        if (input == null) {
            connection.disconnect();
            return;
        }

        try (InputStream source = input; OutputStream target = response.getOutputStream()) {
            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = source.read(buffer)) != -1) {
                target.write(buffer, 0, read);
            }
            target.flush();
        } finally {
            connection.disconnect();
        }
    }

    private void copyHeader(
            HttpURLConnection connection,
            HttpServletResponse response,
            String name
    ) {
        String value = connection.getHeaderField(name);
        if (value != null) {
            response.setHeader(name, value);
        }
    }
}
