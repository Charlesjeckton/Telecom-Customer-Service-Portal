package filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import beans.LoginBean;
import model.User;

@WebFilter("/*")
public class RoleFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        String path = req.getRequestURI();

        // ---------------------------
        // Public pages that don't require login
        // ---------------------------
        if (path.endsWith("login.xhtml") ||
            path.endsWith("register.xhtml") ||
            path.endsWith("index.xhtml") ||
            path.contains("/assets/") ||
            path.contains("/css/") ||
            path.contains("/js/") ||
            path.contains("/images/")) {

            chain.doFilter(request, response);
            return;
        }

        // ---------------------------
        // Require login
        // ---------------------------
        if (session == null) {
            res.sendRedirect(req.getContextPath() + "/login.xhtml");
            return;
        }

        LoginBean loginBean = (LoginBean) session.getAttribute("loginBean");
        if (loginBean == null || loginBean.getLoggedInUser() == null) {
            res.sendRedirect(req.getContextPath() + "/login.xhtml");
            return;
        }

        User loggedUser = loginBean.getLoggedInUser();
        String role = loggedUser.getRole();

        // ---------------------------
        // Role-based access control
        // ---------------------------
        // Admin pages
        if (path.contains("/admin/") && !"ADMIN".equalsIgnoreCase(role)) {
            res.sendRedirect(req.getContextPath() + "/accessDenied.xhtml");
            return;
        }

        // Customer pages
        // Customer pages are in /customer/ folder
        if (path.contains("/customer/") && !"CUSTOMER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            res.sendRedirect(req.getContextPath() + "/accessDenied.xhtml");
            return;
        }

        // ---------------------------
        // Prevent back button after logout
        // ---------------------------
        res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        res.setHeader("Pragma", "no-cache");
        res.setDateHeader("Expires", 0);

        // ---------------------------
        // Continue the request chain
        // ---------------------------
        chain.doFilter(request, response);
    }
}
