package org.example.servlet;

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import org.example.dao.ShowDAO;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private final ShowDAO showDAO = new ShowDAO();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request,
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

        boolean isAdmin = Boolean.TRUE.equals(session.getAttribute("isAdmin"));

        Map<String, Object> result = new HashMap<>();
        result.put("isAdmin", isAdmin);
        result.put("shows", showDAO.fetchShows());

        mapper.writeValue(response.getWriter(), result);
    }
}
