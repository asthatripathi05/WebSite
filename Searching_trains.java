package Myservlets;

import Myclasses.Trainn;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "Searching_trains", urlPatterns = {"/Searching_trains"})
public class Searching_trains extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get input values from the form
        String sourceStation = request.getParameter("board");
        String destinationStation = request.getParameter("dest");
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Load the database driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Establish the connection
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/railway_info", "root", "root");

            // SQL query to find the trains based on source and destination
            String query = "SELECT " +
                            "t.train_number AS train_no, " +
                            "t.train_name, " +
                            "s1.station_code AS source_station_code, " +
                            "s1.station_name AS source_station_name, " +
                            "s2.station_code AS destination_station_code, " +
                            "s2.station_name AS destination_station_name, " +
                            "SUM(sd.distance) AS total_distance " +
                            "FROM Trains t " +
                            "JOIN Train_Route tr1 ON t.train_id = tr1.train_id " +
                            "JOIN Stations s1 ON tr1.station_id = s1.station_id " +
                            "JOIN Train_Route tr2 ON t.train_id = tr2.train_id " +
                            "JOIN Stations s2 ON tr2.station_id = s2.station_id " +
                            "JOIN Station_Distance sd ON t.train_id = sd.train_id " +
                            "WHERE s1.station_name = ? " +
                            "AND s2.station_name = ? " +
                            "AND tr1.sequence < tr2.sequence " +
                            "AND sd.from_station_id >= tr1.station_id " +
                            "AND sd.to_station_id <= tr2.station_id " +
                            "GROUP BY t.train_number, t.train_name, s1.station_code, s1.station_name, s2.station_code, s2.station_name";

            // Prepare statement and set parameters
            stmt = con.prepareStatement(query);
            stmt.setString(1, sourceStation);
            stmt.setString(2, destinationStation);

            // Execute the query
            rs = stmt.executeQuery();
            List<Trainn> trains = new ArrayList<>();

            // Process the result set and populate the list of Trainn objects
            while (rs.next()) {
                Trainn train = new Trainn();
                train.setTrainNumber(rs.getString("train_no"));
                train.setTrainName(rs.getString("train_name"));
                train.setSourceStationName(rs.getString("source_station_name"));
                train.setDestinationStationName(rs.getString("destination_station_name"));
                train.setTotalDistance(rs.getDouble("total_distance"));
                trains.add(train);
            }

            // Set the attributes to be forwarded to the JSP
            request.setAttribute("trains", trains);
            request.setAttribute("board", sourceStation);
            request.setAttribute("dest", destinationStation);

            // Forward the request to the trainShow.jsp page to display results
            RequestDispatcher dispatcher = request.getRequestDispatcher("/Myjsp/trainShow.jsp");
            dispatcher.forward(request, response);

        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(Searching_trains.class.getName()).log(Level.SEVERE, null, ex);
            response.getWriter().println("Error: " + ex.getMessage());  // Simple error handling
        } finally {
            // Clean up resources
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (con != null) con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
