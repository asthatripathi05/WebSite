/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package Myservlets;

import Myclasses.Route;
import Myclasses.RouteResult;
import Myclasses.Trainn;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

@WebServlet(name = "Sorting_trains", urlPatterns = {"/Sorting_trains"})
public class Sorting_trains extends HttpServlet {
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
        String sourceStation = request.getParameter("board");
        String destinationStation = request.getParameter("dest");
        String sortBy = request.getParameter("sortBy");

        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Load the database driver and establish connection
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/railway_info", "root", "root");

            // Fetch all trains and routes for the Dijkstra algorithm
            String query = "SELECT " +
                            "tr.train_id, " +
                            "s1.station_id AS source_id, " +
                            "s2.station_id AS destination_id, " +
                            "sd.distance, " +
                            "tr.arrival_time, " +
                            "tr.departure_time " +
                            "FROM Train_Route tr " +
                            "JOIN Station_Distance sd ON tr.train_id = sd.train_id " +
                            "JOIN Stations s1 ON sd.from_station_id = s1.station_id " +
                            "JOIN Stations s2 ON sd.to_station_id = s2.station_id";

            stmt = con.prepareStatement(query);
            rs = stmt.executeQuery();

            // Build the graph for Dijkstra's algorithm
            Map<Integer, List<Route>> graph = new HashMap<>();
            while (rs.next()) {
                int trainId = rs.getInt("train_id");
                int sourceId = rs.getInt("source_id");
                int destinationId = rs.getInt("destination_id");
                int distance = rs.getInt("distance");
                Time arrivalTime = rs.getTime("arrival_time");
                Time departureTime = rs.getTime("departure_time");

                graph.computeIfAbsent(sourceId, k -> new ArrayList<>())
                     .add(new Route(trainId, destinationId, distance, arrivalTime, departureTime));
            }

            // Find station IDs for source and destination
            int sourceId = getStationId(con, sourceStation);
            int destinationId = getStationId(con, destinationStation);

            // Apply Dijkstra's algorithm based on the selected sort criteria
            List<RouteResult> routes = applyDijkstra(graph, sourceId, destinationId, sortBy);

            // Fetch train details for display
            List<Trainn> trains = new ArrayList<>();
            for (RouteResult route : routes) {
                Trainn train = getTrainDetails(con, route.getTrainId());
                train.setTotalDistance(route.getDistance());
                train.setSourceStationName(sourceStation);
                train.setDestinationStationName(destinationStation);
                trains.add(train);
            }

            // Set attributes and forward to trainPath.jsp
            request.setAttribute("trains", trains);
            request.setAttribute("board", sourceStation);
            request.setAttribute("dest", destinationStation);

            RequestDispatcher dispatcher = request.getRequestDispatcher("/Myjsp/trainPath.jsp");
            dispatcher.forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("Error: " + e.getMessage());
        } finally {
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
    private int getStationId(Connection con, String stationName) throws SQLException {
        String query = "SELECT station_id FROM Stations WHERE station_name = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, stationName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("station_id");
            }
        }
        throw new SQLException("Station not found: " + stationName);
    }

    private List<RouteResult> applyDijkstra(Map<Integer, List<Route>> graph, int source, int target, String sortBy) {
        PriorityQueue<RouteResult> pq = new PriorityQueue<>(Comparator.comparingInt(RouteResult::getDistance));
        Map<Integer, Integer> distances = new HashMap<>();
        Map<Integer, Integer> prev = new HashMap<>();

        // Initialize distances
        for (int node : graph.keySet()) {
            distances.put(node, Integer.MAX_VALUE);
        }
        distances.put(source, 0);

        pq.add(new RouteResult(source, 0, null));

        while (!pq.isEmpty()) {
            RouteResult current = pq.poll();
            int currentNode = current.getStationId();

            if (currentNode == target) break;

            for (Route neighbor : graph.getOrDefault(currentNode, new ArrayList<>())) {
                int newDistance = distances.get(currentNode) + neighbor.getDistance();
                if (newDistance < distances.getOrDefault(neighbor.getDestination(), Integer.MAX_VALUE)) {
                    distances.put(neighbor.getDestination(), newDistance);
                    prev.put(neighbor.getDestination(), currentNode);
                    pq.add(new RouteResult(neighbor.getDestination(), newDistance, neighbor));
                }
            }
        }

        // Reconstruct path
        List<RouteResult> path = new ArrayList<>();
        Integer currentNode = target;
        while (currentNode != null && prev.containsKey(currentNode)) {
            RouteResult route = new RouteResult(currentNode, distances.get(currentNode), null);
            path.add(route);
            currentNode = prev.get(currentNode);
        }

        Collections.reverse(path);
        return path;
    }

    private Trainn getTrainDetails(Connection con, int trainId) throws SQLException {
        String query = "SELECT train_number, train_name FROM Trains WHERE train_id = ?";
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setInt(1, trainId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Trainn train = new Trainn();
                train.setTrainNumber(rs.getString("train_number"));
                train.setTrainName(rs.getString("train_name"));
                return train;
            }
        }
        throw new SQLException("Train not found: ID " + trainId);
    }

    
        }


