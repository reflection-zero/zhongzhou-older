package com.zzyl.framework.config;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * SPA fallback — 非 API 路径的 404 转发到 /index.html 由 Vue Router 接管
 */
@Controller
public class SpaFallbackController implements ErrorController {

    private static final String[] API_PREFIXES = {
        "/nursing/", "/member/", "/system/", "/monitor/", "/tool/",
        "/ws/", "/dev-api/", "/captcha", "/login", "/register",
        "/swagger", "/v3/", "/druid/", "/profile/"
    };

    @RequestMapping("${server.error.path:${error.path:/error}}")
    public void handleError(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Object statusObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String path = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        boolean isApi = path != null && startsWithAny(path, API_PREFIXES);

        if (isApi) {
            int status = (statusObj != null) ? Integer.parseInt(statusObj.toString()) : 500;
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(status);
            response.getWriter().write("{\"msg\":\"Error\",\"code\":" + status + "}");
            return;
        }

        // SPA route — serve index.html
        response.setStatus(200);
        request.getRequestDispatcher("/index.html").forward(request, response);
    }

    private boolean startsWithAny(String path, String[] prefixes) {
        for (String p : prefixes) {
            if (path.startsWith(p)) return true;
        }
        return false;
    }
}
