package org.example.servlet;

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import org.example.dao.UserDAO;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        Map<String, Object> result = new HashMap<>();

        if (userDAO.validateUser(username, password)) {

            HttpSession session = request.getSession();
            session.setAttribute("user", username);
            session.setAttribute("isAdmin", "admin".equals(username));

            result.put("status", "success");
            result.put("isAdmin", session.getAttribute("isAdmin"));

            mapper.writeValue(response.getWriter(), result);
            return;
        }

        boolean created = userDAO.createUser(username, password);

        if (created) {
            result.put("status", "registered");
            mapper.writeValue(response.getWriter(), result);
            return;
        }


        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        result.put("status", "invalid");
        mapper.writeValue(response.getWriter(), result);
    }
}
