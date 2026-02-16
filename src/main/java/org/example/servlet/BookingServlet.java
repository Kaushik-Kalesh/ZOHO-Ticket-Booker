package org.example.servlet;

import org.example.dao.BookingDAO;
import org.example.dao.UserDAO;
import org.example.model.User;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/book")
public class BookingServlet extends HttpServlet {

    private final ObjectMapper mapper = new ObjectMapper();
    private final UserDAO userDAO = new UserDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

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

        Map<String, Object> result = new HashMap<>();
        result.put("bookings", bookingDAO.getAllBookings((String) session.getAttribute("user")));

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

        try {
            Map<String, Object> payload = mapper.readValue(request.getReader(), Map.class);
            String username = (String) session.getAttribute("user");
            User user = userDAO.getUser(username);
            int screenId = (int) payload.get("screenId");
            int showId = (int) payload.get("showId");
            int seatQty = (int) payload.get("seatQty");

            bookingDAO.addBooking(user.id(), screenId, showId, seatQty);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", e.getMessage()));
        }
    }

    @Override
    public void doDelete(HttpServletRequest request,
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

        try {
            bookingDAO.cancelBooking(Integer.parseInt(request.getParameter("bookingId")));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(),
                    Map.of("error", e.getMessage()));
        }
    }
}
