package org.example.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.dao.MovieVoteDAO;
import org.example.dao.UserDAO;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

@WebServlet("/vote_movie")
public class MovieVoteServlet extends HttpServlet {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final UserDAO userDAO = new UserDAO();
    private static final MovieVoteDAO movieVoteDAO = new MovieVoteDAO();

    @Override
    public void doPost(HttpServletRequest request,
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
            int movie_id = Integer.parseInt(request.getParameter("id"));
            int user_id = userDAO.getUser(
                    session.getAttribute("user").toString()
            ).id();

            movieVoteDAO.addVote(movie_id, user_id);
            response.setStatus(HttpServletResponse.SC_OK);
            mapper.writeValue(response.getWriter(), Map.of("status", "success"));

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            mapper.writeValue(response.getWriter(), Map.of("error", "Some error occurred"));
        }
    }
}
