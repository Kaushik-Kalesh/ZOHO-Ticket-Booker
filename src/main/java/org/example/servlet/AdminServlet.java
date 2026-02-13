package org.example.servlet;

import org.example.dao.ScreenDAO;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import org.example.dao.ShowDAO;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/admin")
public class AdminServlet extends HttpServlet {
    private final ScreenDAO screenDAO = new ScreenDAO();
    private final ShowDAO showDAO = new ShowDAO();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        if (session == null || Boolean.FALSE.equals(session.getAttribute("isAdmin"))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", "Not authenticated"));
            return;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("screens", screenDAO.fetchScreens());
        result.put("shows", showDAO.fetchShows());

        mapper.writeValue(response.getWriter(), result);
    }

    @Override
    protected void doPost(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {
        Map<String, Object> payload = mapper.readValue(request.getReader(), Map.class);
        if ("screen".equals(request.getParameter("type"))) {

            String name = (String) payload.get("name");
            int capacity = (int) payload.get("capacity");
            int price = (int) payload.get("price");
            screenDAO.addScreen(name, capacity, price);

        } else {

            String title = (String) payload.get("title");
            String time = (String) payload.get("startTime");
            int screenId = (int) payload.get("screenId");

            //TODO: Ensure time doesn't conflict with existing shows on the same screen

            showDAO.addShow(title, LocalDateTime.parse(time), screenId);

        }
    }
}
