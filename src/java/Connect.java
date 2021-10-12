import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * this class handles the database that is connected through a local mysql server.
 * It retrieves route information and stores ticket information to and from this database.
 */
public class Connect {

    /**
     * a list that holds all the connections that the user can go to and the time they take to reach
     */
    private static Map<String, Double> routes = new HashMap<>();

    /**
     * establishes a connection to the database on a local mysql server
     */
    private static Connection establishConnection() throws SQLException {

        Connection conn = null;
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/boarding_pass", "root", "root");
        return conn;
    }

    /**
     * gathers information stored in the database and places it into the route instance variable
     */
    public static void readInfo() {
        Connection conn = null;
        try {

            conn = establishConnection();
            Statement statement = conn.createStatement();
            ResultSet results = statement.executeQuery("SELECT destination FROM boarding_pass.route");

            ArrayList<String> destinations = new ArrayList<>();
            while (results.next()) {
                destinations.add(results.getString(1));
            }

            results = statement.executeQuery("SELECT trip_time FROM boarding_pass.route");

            ArrayList<String> times = new ArrayList<>();
            while (results.next()) {
                times.add(results.getString(1));
            }

            if (destinations.size() == times.size()) {
                for (int i = 0; i < destinations.size(); i++) {
                    routes.put(destinations.get(i), Double.parseDouble(times.get(i)));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Insert into the ticket table
     *
     */
    public static int saveTicket(User u, String eta, double ticketPrice) {

        String boardingPassNumber = "";
        Connection conn = null;
        try {
            conn = establishConnection();
            if (conn != null) {
                Statement statement = conn.createStatement();
                ResultSet results = statement.executeQuery("SELECT * FROM ticket WHERE boardingPassNumber=" +
                        "(SELECT max(boardingPassNumber) FROM ticket)");
                if(!results.next()) boardingPassNumber = "100";
                else boardingPassNumber = results.getString(1);

                System.out.println(boardingPassNumber);

                String temp = "INSERT INTO ticket (" +
                        "name, email, phoneNumber, gender, " +
                        "age, destination, departureTime, eta, ticketPrice) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement prep = conn.prepareStatement(temp);

                prep.setInt(1, Integer.parseInt(boardingPassNumber));
                prep.setString(2, u.getName());
                prep.setString(3, u.getEmail());
                prep.setString(4, u.getPhoneNumber());
                prep.setString(5, u.getGender());
                prep.setString(6, String.valueOf(u.getAge()));
                prep.setString(7, u.getDestination());
                prep.setString(8, u.getDepartureTime());
                prep.setString(9, eta);
                prep.setString(10, String.valueOf(ticketPrice));
                prep.execute();

            }else System.out.println("failed");
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return Integer.parseInt(boardingPassNumber);
    }

    public static Map<String, Double> getRoutes() {
        return routes;
    }

    public static void setRoutes(Map<String, Double> routes) {
        Connect.routes = routes;
    }
}
