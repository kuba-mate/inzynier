package com.example.inzynier.configuration;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@WebFilter("/*")
public class RoleAccessFilter implements Filter {

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;

        final String role = (String) httpRequest.getSession().getAttribute("role");
        final String requestURI = httpRequest.getRequestURI();

        final List<String> clientPages = Arrays.asList("/karnety", "/konto", "/jak-zaczac");

        if ("administrator".equals(role) || "coach".equals(role)) {
            if (clientPages.stream().anyMatch(requestURI::contains)) {
                httpResponse.sendRedirect("brak-dostepu");
                return;
            }
        }

        if(requestURI.contains("/admin") && !"administrator".equals(role)){
            httpResponse.sendRedirect("brak-dostepu");
            return;
        }

        if(requestURI.contains("/coach") && !"coach".equals(role)){
            httpResponse.sendRedirect("brak-dostepu");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
