package org.example.servlet;

import org.example.util.PasswordUtil;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import org.example.dao.UserDAO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
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

        Map<String, String> payload = mapper.readValue(request.getReader(), Map.class);
        String username = payload.get("username");
        String password = PasswordUtil.hashPassword(payload.get("password"), username);
        String type = request.getParameter("type");

        Map<String, Object> result = new HashMap<>();

        if ("new".equals(type)) {

            String email = payload.get("email");
            if (userDAO.createUser(username, email, password)) {
                HttpSession session = request.getSession();
                session.setAttribute("user", username);
                session.setAttribute("isAdmin", false);
                result.put("status", "success");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("status", "user_exists");
            }
            mapper.writeValue(response.getWriter(), result);

        } else if ("bot".equals(type)) {

            String email = payload.get("email");
            if (userDAO.createUser(username, email, password)) {
                HttpSession session = request.getSession();
                session.setAttribute("user", username);
                session.setAttribute("isAdmin", false);
                result.put("status", "success");
                result.put("user_id", userDAO.getUser(username).id());
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.put("status", "user_exists");
            }
            mapper.writeValue(response.getWriter(), result);

        } else {

            if (userDAO.validateUser(username, password)) {
                HttpSession session = request.getSession();
                session.setAttribute("user", username);
                session.setAttribute("isAdmin", "admin".equals(username));
                result.put("status", "success");
                result.put("isAdmin", "admin".equals(username));
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                result.put("status", "invalid");
            }

            mapper.writeValue(response.getWriter(), result);
        }
    }
}
