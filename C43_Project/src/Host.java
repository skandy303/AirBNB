import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class Host {
    static Scanner scan = new Scanner(System.in);
    static Connection con = null;
    static Integer user = null;

    public static void createListing() {
        String query = "";
        ResultSet rs;
        PreparedStatement query1;
        Double lon, lat, price = 0.0;
        Integer sNo, unitNo, opt = 0;
        List x = new ArrayList<>();
        Hashtable<Integer, String> listingTypes = new Hashtable<Integer, String>() {
            {
                put(1, "Full house");
                put(2, "Apartment");
                put(3, "Room");
            }
        };
        String sName, city, province, country, code = "";
        try {
            while (true) {
                System.out.println("Listing Type:");
                System.out.println("  (1) Full house");
                System.out.println("  (2) Apartment");
                System.out.println("  (3) Room");
                opt = scan.nextInt();
                if (opt > 3 || opt < 1) {
                    System.out.println("Invalid Option");
                    continue;
                }
                System.out.println("Longitude:");
                lon = scan.nextDouble();
                if (lon.doubleValue() > 180 || lon.doubleValue() < -180) {
                    System.out.println("Invalid Option");
                    continue;
                }
                System.out.println("Latitude:");
                lat = scan.nextDouble();
                if (lat.doubleValue() > 90 || lat.doubleValue() < -90) {
                    System.out.println("Invalid Option");
                    continue;
                }
                query = "SELECT AVG(price) FROM Listing WHERE ST_Distance_Sphere(point(?,?), point(longitude,latitude))/1000 <= 50";
                query1 = con.prepareStatement(query);
                query1.setDouble(1, lon);
                query1.setDouble(2, lat);
                rs = query1.executeQuery();
                Double sugPrice = 0.0;
                String prnt = "(No Listings near by)";
                Boolean bo = rs.next();
                sugPrice = rs.getDouble(1);
                if (sugPrice != 0.0) {
                    prnt = "(" + sugPrice + ")";
                }
                ;
                System.out.println("Price" + prnt);
                price = scan.nextDouble();
                System.out.println("Street Number:");
                sNo = scan.nextInt();
                scan.nextLine();
                System.out.println("Street Name:");
                sName = scan.nextLine();
                System.out.println("City:");
                city = scan.nextLine();
                System.out.println("Province:");
                province = scan.nextLine();
                System.out.println("Country:");
                country = scan.nextLine();
                System.out.println("Postal Code:");
                code = scan.nextLine();
                if (code.length() > 6) {
                    System.out.println("Postal/Zip code too long");
                    continue;
                }
                System.out.println("Unit Number (-1 if no unit number):");
                unitNo = scan.nextInt();
                String subQuery = "(SELECT * FROM Listing join Provides using(listID) join Amenities using(amenityID) WHERE ST_Distance_Sphere(point(" + lon + "," + lat + "), point(longitude,latitude))/1000 <= 50 )";
                query = "SELECT SUBSTRING_INDEX(GROUP_CONCAT(name ORDER BY total DESC ), ',', 5)  result\n" +
                    "FROM (\n" +
                    "  SELECT name, SUM(col) total\n" +
                    "  FROM (\n" +
                    "    SELECT 'wifi' name, wifi col FROM " + subQuery + " tbl1\n" +
                    "    UNION ALL\n" +
                    "    SELECT 'washer', washer FROM " + subQuery + " tbl2\n" +
                    "    UNION ALL\n" +
                    "    SELECT 'ac', ac FROM " + subQuery + " tbl3\n" +
                    "    UNION ALL\n" +
                    "    SELECT 'heating', heating FROM " + subQuery + " tbl24\n" +
                    "    UNION ALL\n" +
                    "    SELECT 'tv', tv FROM " + subQuery + " tbl4\n" +
                    "        UNION ALL\n" +
                    "    SELECT 'iron', iron FROM " + subQuery + " tbl5\n" +
                    "        UNION ALL\n" +
                    "    SELECT 'kitchen', kitchen FROM " + subQuery + " tbl6\n" +
                    "UNION ALL\n" +
                    "    SELECT 'dryer', dryer FROM " + subQuery + " tbl8\n" +
                    "    UNION ALL\n" +
                    "    SELECT 'workspace', workspace FROM " + subQuery + " tbl9\n" +
                    "    UNION ALL\n" +
                    "    SELECT 'hairdryer', hairdryer FROM " + subQuery + " tbl10\n" +
                    "        UNION ALL\n" +
                    "    SELECT 'pool', pool FROM " + subQuery + " tbl11\n" +
                    "        UNION ALL\n" +
                    "    SELECT 'parking', parking FROM " + subQuery + " tbl12\n" +
                    "UNION ALL\n" +
                    "    SELECT 'crib', crib FROM " + subQuery + " tbl13\n" +
                    "    UNION ALL\n" +
                    "    SELECT 'grill', grill FROM " + subQuery + " tbl14\n" +
                    "    UNION ALL\n" +
                    "    SELECT 'indoorFireplace', indoorFireplace FROM " + subQuery + " tbl15\n" +
                    "        UNION ALL\n" +
                    "    SELECT 'hotTub', evCharger FROM " + subQuery + " tbl16\n" +
                    "        UNION ALL\n" +
                    "    SELECT 'gym', gym FROM " + subQuery + " tbl17\n" +
                    "UNION ALL\n" +
                    "    SELECT 'breakfast', breakfast FROM " + subQuery + " tbl18\n" +
                    "    UNION ALL\n" +
                    "    SELECT 'smoking', smoking FROM " + subQuery + " tbl19\n" +
                    "    UNION ALL\n" +
                    "    SELECT 'beachfront', beachfront FROM " + subQuery + " tbl20\n" +
                    "        UNION ALL\n" +
                    "    SELECT 'waterfront', waterfront FROM " + subQuery + "tbl21\n" +
                    "        UNION ALL\n" +
                    "    SELECT 'smokeAlarm', smokeAlarm FROM " + subQuery + "tbl22\n" +
                    "        UNION ALL\n" +
                    "    SELECT 'carbonMonoxideAlarm', carbonMonoxideAlarm FROM " + subQuery + " tbl23\n" +
                    "  ) t\n" +
                    "  GROUP BY name\n" +
                    ") t;  ";
                rs = query1.executeQuery(query);
                rs.next();
                String tup = rs.getString(1);
                if (tup == null) System.out.println("No Listings nearby to compare by.");
                else System.out.println("Suggested Amenities Based on Listings nearby:\n" + tup);
                System.out.println("Choose Amenities:");
                System.out.println("(1) Wifi, (2) Washer, (3) Air Conditioning, (4) Heating,\n" +
                    "(5) Television, (6) Iron, (7) Kitchen, (8) Dryer, (9) Workspace,\n" +
                    "(10) Hair Dryer, (11) Pool, (12) Parking, (13) Crib, (14) Grill,\n" +
                    "(15) Indoor Fireplace, (16) Hot Tub, (17) EV Charger, (18) Gym,\n" +
                    "(19) Breakfast, (20) Smoking, (21) Beachfront, (22) Waterfront\n" +
                    "(23) Smoke Alarm, (24) Carbonmononxide Alarm");
                System.out.println("Type 0 to stop");
                int bre = 0;
                while (bre == 0) {
//                    System.out.println(x);
                    Integer option = scan.nextInt();
                    if (option == 0) {
                        bre = 1;
                    } else {
                        if (!x.contains(option)) {
                            x.add(option);
                        } else {
                            System.out.println("Option already selected.");
                        }
                    }
                }
                break;
            }
            String amenQuery = "INSERT INTO Amenities (wifi, washer, ac, heating, tv, iron," +
                "kitchen,dryer,workspace, hairDryer, pool, parking,crib, grill, indoorFireplace, hotTub," +
                "evCharger, gym, breakfast, smoking, beachfront, waterfront, smokeAlarm, carbonMonoxideAlarm)" +
                " Values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            String amenValues = "(";
            PreparedStatement query3 = con.prepareStatement(amenQuery, Statement.RETURN_GENERATED_KEYS);
            for (int i = 1; i <= 24; i++) {
                boolean b = false;
                if (x.contains(i)) {
//                    System.out.println("Hye");
                    b = true;
                }
                query3.setBoolean(i, b);
                amenValues += b;
            }
            Integer chk = 0;
            query = "INSERT INTO Listing (listingType,longitude,latitude,price) VALUES (?,?,?,?)";
            query1 = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            query1.setString(1, listingTypes.get(opt));
            query1.setDouble(2, lon);
            query1.setDouble(3, lat);
            query1.setDouble(4, price);
            chk += query1.executeUpdate();
            rs = query1.getGeneratedKeys();
            rs.next();
            Integer listingKey = (rs.getInt(1));

            query = "INSERT INTO Address (streetNo, streetName, city, country, province, postalCode, unitNo) VALUES (?,?,?,?,?,?,?)";
            if (unitNo == -1)
                query = "INSERT INTO Address (streetNo, streetName, city, country, province, postalCode) VALUES (?,?,?,?,?,?)";
            PreparedStatement query2 = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            query2.setInt(1, sNo);
            query2.setString(2, sName);
            query2.setString(3, city);
            query2.setString(4, country);
            query2.setString(5, province);
            query2.setString(6, code);
            if (unitNo != -1) query2.setInt(7, unitNo);
            chk += query2.executeUpdate();
            rs = query2.getGeneratedKeys();
            rs.next();
            Integer addressKey = rs.getInt(1);

            chk += query3.executeUpdate();
            rs = query3.getGeneratedKeys();
            rs.next();
            Integer amenityKey = rs.getInt(1);

            query = "INSERT INTO Located VALUES (?,?)";
            PreparedStatement query4 = con.prepareStatement(query);
            query4.setInt(1, listingKey);
            query4.setInt(2, addressKey);
            chk += query4.executeUpdate();

            query = "INSERT INTO Provides VALUES (?, ?)";
            PreparedStatement query5 = con.prepareStatement(query);
            query5.setInt(1, amenityKey);
            query5.setInt(2, listingKey);
            chk += query5.executeUpdate();

            query = "INSERT INTO Owns VALUES (?,?)";
            PreparedStatement query6 = con.prepareStatement(query);
            query6.setInt(1, listingKey);
            query6.setInt(2, user);
            chk += query6.executeUpdate();
            if (chk == 6) System.out.println("Successfully created a listing");
            else System.out.println("Listing creation unsuccessful");
//            handleHost(user, con);
        } catch (Exception e) {
            System.out.println("Error " + e);
        }
    }

    public static void viewListing() {
        String query = "SELECT * FROM Listing join " +
            "Owns using(listID) join " +
            "Hosts using(hostID) join " +
            "Located using(listID) join " +
            "Address using(addressID) " +
            "WHERE hostID =  ?";
        try {
            PreparedStatement statement = con.prepareStatement(query);
            statement.setInt(1, user);
            ResultSet rs = statement.executeQuery();
            printListings(rs);
        } catch (SQLException e) {
            System.out.println("Error" + e);
        }

    }

    public static Integer validListingID() {
        System.out.println("Enter Listing ID");
        Integer listId = scan.nextInt();
        String query = "SELECT * FROM Listing join " +
            "Owns using(listID) join " +
            "Hosts using(hostID) WHERE " +
            "listID = ? and hostID = ? ";
        try {
            PreparedStatement query1 = con.prepareStatement(query);
            query1.setInt(1, listId);
            query1.setInt(2, user);
            ResultSet rs = query1.executeQuery();
            if (rs.next()) {
                return listId;
            } else System.out.println("Listing not owned by host or Invalid Listing ID.\nTry Again");
            return 0;
        } catch (Exception e) {
            System.out.println("Error:" + e);
        }
        return 0;
    }

    public static void deleteListing() {
        Integer listId = validListingID();
        if (listId == 0) return;
        String query;
        try {
            PreparedStatement query1;
            ResultSet rs;
            query = "SET SQL_SAFE_UPDATES = 0";
            query1 = con.prepareStatement(query);
            query1.executeUpdate();
            query = "DELETE Listing, Located, Amenities, Address, Provides FROM " +
                "Listing join Located USING (listID) join " +
                "Address using (addressID) join Provides using(listID) join Amenities using (amenityID) \n" +
                "WHERE listID = ?";
            query1 = con.prepareStatement(query);
            query1.setInt(1, listId);
            Integer status = query1.executeUpdate();
            query = "DELETE FROM Reserved WHERE listID = ?";
            query1.setInt(1,listId);
            status += query1.executeUpdate();
            if (5 <= query1.executeUpdate()) System.out.println("Deleted Listing " + listId);
            else System.out.println("Unsuccessful");
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }

    }

    public static int printListings(ResultSet rs) {
        Integer i = 0;
        String fmt = "|%-9s|%-20s|%-10s|%-10s|%-25s|%-20s|%-20s|%-20s|%-20s|%-10s|";
        Integer check = 0;
        try {
            while (rs.next()) {
                if (i == 0)
                    System.out.println(String.format(fmt, "ListID", "Listing Type", "Longitude", "Latitude", "Address", "City", "Province", "Country", "Postal Code", "Price"));
                Integer listId = rs.getInt("listID");
                String type = rs.getString("listingType");
                Double longitude = rs.getDouble("longitude");
                Double latitude = rs.getDouble("latitude");
                Integer sNo = rs.getInt("streetNo");
                String sName = rs.getString("streetName");
                String city = rs.getString("city");
                String province = rs.getString("province");
                String country = rs.getString("country");
                String pc = rs.getString("postalCode");
                Double price = rs.getDouble("price");
                System.out.println(String.format(fmt,  listId, type,longitude, latitude, sNo + " " + sName, city, province, country, pc, price));
                i += 1;
            }
        } catch (Exception E) {
            check = 1;
            System.out.println(E);
        }

        if (i == 0) System.out.println("No Available listings");
        return check;
    }

    public static void changeAvailability() {
        Integer listId = validListingID();
        if (listId == 0) return;
        System.out.println("Start Date (yyyy-mm-dd):");
        String startDate = scan.nextLine();
        System.out.println("End Date (yyyy-mm-dd):");
        String endDate = scan.nextLine();

        String query = "SELECT * FROM Reserved WHERE ? <= endDate AND ? >= startDate AND statusAvailable = false";
        try {
            PreparedStatement query1 = con.prepareStatement(query);
            query1.setString(1, startDate);
            query1.setString(2, endDate);
            ResultSet rs = query1.executeQuery();
            if (rs.next()) {
                System.out.println("Interferes with existing bookings");
                return;
            }
            //get information needed from the listing id
            PreparedStatement s = con.prepareStatement("select * from Listing join Owns using(listID) join hosts using(hostID) where listID = ?");
            s.setInt(1, listId);
            rs = s.executeQuery();
            rs.next();
            int price = rs.getInt("price");
            int hostID = rs.getInt("hostID");

            //add an entry to reserved
            PreparedStatement s2 = con.prepareStatement("insert into reserved (hostID, renterID, listID, " +
                "startDate, endDate, statusAvailable, hostCancelled,price) values (?,?,?,?,?, false,false,?)");
            s2.setInt(1, user);
            s2.setInt(2, user);
            s2.setInt(3, listId);
            s2.setString(4, startDate);
            s2.setString(5, endDate);
            s2.setInt(6, price);

            int status = s2.executeUpdate();
            if (status == 1) {
                System.out.println("Successfully booked!");
            } else {
                System.out.println("Unable to complete booking. Please try again.");
//                    bookListing(listingSet, startDate, endDate);
            }
        } catch (Exception E) {
            System.out.println(E);
        }
    }

    public static void viewBookings() {
        Integer listID = validListingID();
        if (listID == 0) return;
        String query = "Select * FROM Reserved join Located using(listID) join Address using(addressID) WHERE hostID = ? and listID = ? and hostID != renterID";
        try {
            PreparedStatement query1 = con.prepareStatement(query);
            query1.setInt(1, user);
            query1.setInt(2, listID);
            ResultSet rs = query1.executeQuery();
            String format = "%1$-20s| %2$-10s | %3$-15s | %4$-15s | %5$-15s | %6$-15s | %7$-15s " +
                "| %8$-15s | %9$-15s | %10$-15s | %11$-15s | %12$-15s";
            Integer i = 0;
            while (rs.next()) {

                System.out.println(String.format(format, "ReservationID", "ListID", "HostID", "Price", "Start Date", "End Date",
                    "House Number", "Street Name", "City", "Province", "Postal Code", "Unit No."));
                while (rs.next()) {
                    int reservationID = rs.getInt("reservationID");
                    int hostID = rs.getInt("hostID");
                    int price = rs.getInt("price");
                    String startDate = rs.getString("startDate");
                    String endDate = rs.getString("endDate");
                    int streetNo = rs.getInt("streetNo");
                    String streetName = rs.getString("streetName");
                    String city = rs.getString("city");
                    String province = rs.getString("province");
                    String postalCode = rs.getString("postalCode");
                    int unitNo = rs.getInt("unitNo");
                    System.out.println(String.format(format, reservationID + "", listID + "", hostID + "", price + "", startDate, endDate,
                        streetNo + "", streetName, city, province, postalCode, unitNo + ""));
                }
            }

        } catch (Exception E) {

            System.out.println(E);
        }

    }

    public static void updatePrice() {
        Integer listID = validListingID();
        if (listID == 0) return;
        Double price;
        try {
            while (true) {
                System.out.println("Enter new price:");
                price = scan.nextDouble();
                if (price < 0) {
                    System.out.println("Invalid Price");
                    continue;
                }
                break;
            }
            String query = "SET SQL_SAFE_UPDATES = 0";
            PreparedStatement query1 = con.prepareStatement(query);
            query1.executeUpdate();
            query = "UPDATE Listing SET price = ? WHERE listID = ?";
            PreparedStatement q = con.prepareStatement(query);
            q.setDouble(1, price);
            q.setInt(2, listID);
            if (q.executeUpdate() != 1) {
                System.out.println("Update Successful");
            } else {
                System.out.println("Update unsuccessful");
            }
//            handleHost(user, con);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static int validResID() {
        Integer resID;
        while (true) {
            try {
                System.out.println("Reservation ID:");
                resID = scan.nextInt();
            } catch (Exception E) {
                System.out.println("Must be an Integer. Try again.");
                continue;
            }
            break;
        }
        try {
            String query = "SELECT * FROM reserved WHERE reservationID = ? AND hostID = ? and renterID != ? and statusAvailable = false";
            PreparedStatement q1 = con.prepareStatement(query);
            q1.setInt(1, resID);
            q1.setInt(2, user);
            q1.setInt(3, user);
            ResultSet rs = q1.executeQuery();
            if (!rs.next()) {
                System.out.println("reservation ID does not exist or has already been cancelled");
                return 0;
            }
            return resID;
        } catch (Exception E) {
            System.out.println("reservation ID does not exist or has already been cancelled");
            return 0;
        }
    }

    public static void cancelBooking() {
        Integer resID = validResID();
        try {
            String query;
            query = "SET SQL_SAFE_UPDATES = 0";
            PreparedStatement query1 = con.prepareStatement(query);
            query1.executeUpdate();
            query = "UPDATE reserved SET statusAvailable = true, hostCancelled = true WHERE reservationID = ?";
            PreparedStatement q = con.prepareStatement(query);
            q.setDouble(1, resID);
            if (q.executeUpdate() != 1) {
                System.out.println("Update Successful");
            } else {
                System.out.println("Update Unsuccessful");
            }
//            handleHost(user,con);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void leaveReview() {
        Integer resID = validResID();
        Integer score = 0;
        if (resID == 0) return;
        try {
            while (true) {
                System.out.println("Rate your stay from (1-5):");
                try {
                    score = scan.nextInt();
                    if (score < 1 && score > 5) {
                        System.out.println("Score must be in the range of (1-5)");
                        continue;
                    }
                    break;
                } catch (Exception e) {
                    System.out.println("Must be an integer.");
                }
            }
            String review;
            while (true) {
                System.out.println("Leave a review:");
                scan = new Scanner(System.in);
                review = scan.nextLine();
                if (review.length() > 199) {
                    System.out.println("Review too long. Keep it under 200 characters. Try again");
                    continue;
                }
                break;
            }
            try {
                //change status on reservation
                String query = "SET SQL_SAFE_UPDATES = 0";
                PreparedStatement query1 = con.prepareStatement(query);
                query1.executeUpdate();
                PreparedStatement s = con.prepareStatement("update reserved set " +
                    "hostReview = ?, hostScore = ? WHERE endDate < CURDATE() and reservationID = ?");
                s.setInt(2, score);
                s.setString(1, review);
                s.setInt(3,resID);
                if (s.executeUpdate() != 1) {
                    System.out.println("The booking date has yet to be passed");
                    return;
                }
                System.out.println("Successfully updated review.");
            } catch (SQLException e) {
                System.out.println(e);
            }
        } catch (Exception E) {
            System.out.println("The booking date has yet to be passed" + E);
        }
    }

    public static void handleHost(int input_username, Connection conn) {
        user = input_username;
        con = conn;
        //print out all the options
        while (true) {
            System.out.println("(0) Return to main menu");
            System.out.println("(1) Create listing");
            System.out.println("(2) View listings");
            System.out.println("(3) View Bookings");
            System.out.println("(4) Change Availability");
            System.out.println("(5) Delete listing");
            System.out.println("(6) Update price");
            System.out.println("(7) Cancel Booking");
            System.out.println("(8) Leave Review");
            try {
                Integer opt = scan.nextInt();
                if (opt == 1) createListing();
                else if (opt == 2) viewListing();
                else if (opt == 3) viewBookings();
                else if (opt == 4) changeAvailability();
                else if (opt == 5) deleteListing();
                else if (opt == 6) updatePrice();
                else if (opt == 7) cancelBooking();
                else if (opt == 8) leaveReview();
                else if (opt == 0) break;
                else System.out.println("Invalid Option. Try again.");
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        User.handleUserMainMenu(user,con);
    }
}
