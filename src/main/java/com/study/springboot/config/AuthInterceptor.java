package com.study.springboot.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.study.springboot.dto.SessionUser;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        SessionUser loginUser = (session != null)
                ? (SessionUser) session.getAttribute(SessionUser.SESSION_KEY)
                : null;

        if (loginUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }

        String requestUri = request.getRequestURI();
        if (requestUri.startsWith(request.getContextPath() + "/admin") && !loginUser.isAdmin()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            request.getRequestDispatcher("/WEB-INF/views/access-denied.jsp").forward(request, response);
            return false;
        }

        return true;
    }
}
