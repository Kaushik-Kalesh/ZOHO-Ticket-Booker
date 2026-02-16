package org.example.servlet;

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
        String password = hashPassword(payload.get("password"), username);
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
            return;
        }

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

    private String hashPassword(String password, String usernameSalt) {
        if (password == null || usernameSalt == null) return null;
        try {
            byte[] salt = usernameSalt.getBytes(StandardCharsets.UTF_8);
            int iterations = 120_000;
            int keyLength = 256;
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLength);
            byte[] derived = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(derived);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2WithHmacSHA512 not available", e);
        }
    }
}
