package org.example.servlet;

import jakarta.servlet.Servlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.dao.UserDAO;
import org.example.model.User;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/wallet")
public class WalletServlet extends HttpServlet {

    private final ObjectMapper mapper = new ObjectMapper();
    private final UserDAO userDAO = new UserDAO();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", "Not authenticated"));
            return;
        }

        Map<String, Object> result = new HashMap<>();
        String username = (String) session.getAttribute("user");
        User user = userDAO.getUser(username);
        result.put("walletBal", user.walletBal());
        result.put("loyaltyPts", user.loyaltyPts());

        mapper.writeValue(response.getWriter(), result);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", "Not authenticated"));
            return;
        }

        String username = (String) session.getAttribute("user");
        if ("load".equals(request.getParameter("type"))) {
            Map<String, Object> payload = mapper.readValue(request.getReader(), Map.class);
            userDAO.loadWallet(username, (int) payload.get("amount"));
        } else {
            userDAO.redeemLoyaltyPoints(username);
        }
    }
}
