    import java.util.ArrayList;
    import java.util.HashSet;
    import java.util.List;
    import java.util.Scanner;
    import java.sql.*;

public class Renter {

    static Connection con = null;
    static Scanner scan = new Scanner(System.in);
    static int username;

    public static void handleRenter(int input_username, Connection connection){
        con = connection;
        username = input_username;
        //take in user input
        int option;
        while (true) {
            System.out.println("\nChoose an option:");
            System.out.println("(0) Back Menu");
            System.out.println("(1) Create Reservation");
            System.out.println("(2) View Reservations");
            System.out.println("(3) Cancel Reservation");
            System.out.println("(4) Write a Review");
            Scanner scan = new Scanner(System.in);
            try {
                if (scan.hasNext()) {
                    option = scan.nextInt();
                    scan = new Scanner(System.in);
                    break;
                }
            } catch (Exception e) {
                System.out.println("Invalid option. Must be an integer.\n");
            }
        }

        if (option == 0) {
            User.handleUserMainMenu(username, con);
        } else if (option == 1) {
            // create reservation
            createReservation(username);
        } else if (option == 2) {
            // view reservation
            showReservations(username);
        } else if (option == 3){
            //delete reservation
            cancelReservation(username);
        } else if (option == 4){
            //write a review on past stay
            writeReview(username);
        }else {
            System.out.println("Invalid option.\n");
            handleRenter(username, con);
        }
    }

    public static HashSet<Integer> printResTable(int username){
        HashSet<Integer> reserveSet = new HashSet<>();
        try {
            PreparedStatement s = con.prepareStatement("select * from listing join located using (listid) join " +
                "address using (addressid) join reserved using (listid) where statusAvailable = false " +
                "and renterID = ?");
            s.setInt(1, username);
            ResultSet rs = s.executeQuery();
            String format = "%1$-20s| %2$-10s | %3$-15s | %4$-15s | %5$-15s | %6$-15s | %7$-15s " +
                "| %8$-15s | %9$-15s | %10$-15s | %11$-15s | %12$-15s";
            System.out.println(String.format(format, "ReservationID", "ListID", "HostID", "Price", "Start Date", "End Date",
                "House Number", "Street Name", "City", "Province", "Postal Code", "Unit No."));
            String under = "_";
            for (int i = 0; i < 190; i++){
                under += "_";
            }
            System.out.println(under);

            while (rs.next()){
                int reservationID = rs.getInt("reservationID");
                reserveSet.add(reservationID);
                int listID = rs.getInt("listID");
                int hostID = rs.getInt("hostID");
                int price = rs.getInt("price");
                String startDate = rs.getString("startdate");
                String endDate = rs.getString("enddate");
                int streetNo = rs.getInt("streetno");
                String streetName = rs.getString("streetname");
                String city = rs.getString("city");
                String province = rs.getString("province");
                String postalCode = rs.getString("postalcode");
                int unitNo = rs.getInt("unitno");
                System.out.println(String.format(format, reservationID+"", listID+"", hostID+"", price+"", startDate, endDate,
                    streetNo+"", streetName, city, province, postalCode, unitNo+""));

            }
        } catch (Exception e){
            System.out.println("Unable to show reservations.");
            handleRenter(username,con);
            return null;
        }
        return reserveSet;
    }

    public static void showReservations(int username){
        printResTable(username);
        handleRenter(username, con);
    }

    public static void cancelReservation(int username) {
        HashSet<Integer> resSet = new HashSet<>();
        try {
//            PreparedStatement s = con.prepareStatement("select reserved.reservationID, listing.listid, " +
//                    "reserved.hostid, listing.price, " +
//                    "startdate, enddate, streetno, streetname, city, province, postalcode, unitno from located join " +
//                    "listing join reserved join address where address.addressid = located.addressid " +
//                    "and listing.listid " + "= reserved.listid and listing.listid = located.listid " +
//                    "and statusAvailable = false and startDate > curdate()");
            PreparedStatement s = con.prepareStatement("select * from listing join located using (listid) join " +
                "address using (addressid) join reserved using (listid) where " +
                "statusAvailable = false and startDate > curDate()");
            ResultSet rs = s.executeQuery();
            String format = "%1$-8s| %2$-8s | %3$-10s | %4$-10s | %5$-10s | %6$-15s | %7$-15s " +
                "| %8$-10s | %9$-10s | %10$-15s | %11$-10s | %12$-10s";
            System.out.println(String.format(format, "ReservationID", "ListID", "HostID", "Price", "Start Date",
                "End Date", "House Number", "Street Name", "City", "Province", "Postal Code", "Unit No."));
            String under = "_";
            for (int i = 0; i < 150; i++) {
                under += "_";
            }
            System.out.println(under);

            while (rs.next()) {
                int reservationID = rs.getInt("reservationID");
                resSet.add(reservationID);
                int listID = rs.getInt("listID");
                int hostID = rs.getInt("hostID");
                int price = rs.getInt("price");
                String startDate = rs.getString("startdate");
                String endDate = rs.getString("enddate");
                int streetNo = rs.getInt("streetno");
                String streetName = rs.getString("streetname");
                String city = rs.getString("city");
                String province = rs.getString("province");
                String postalCode = rs.getString("postalcode");
                int unitNo = rs.getInt("unitno");
                System.out.println(String.format(format, reservationID + "", listID + "",
                    hostID + "", price + "", startDate, endDate,
                    streetNo + "", streetName, city, province, postalCode, unitNo + ""));

            }
        } catch (Exception e) {
            System.out.println("Unable to complete.");
            handleRenter(username, con);
        }

        int option;
        while (true) {
            try {
                System.out.println("Choose a reservation ID to cancel (-1 to exit):");
                option = scan.nextInt();
                scan = new Scanner(System.in);

                if (option == -1){
                    handleRenter(username, con);
                }
                if (resSet.contains(option)) {
                    try{
                        //change status on reservation
                        PreparedStatement s = con.prepareStatement("update reserved set " +
                            "statusAvailable = true where reservationID = ?");
                        s.setInt(1, option);
                        s.executeUpdate();
                        System.out.println("Successfully cancelled.");
                        handleRenter(username, con);
                        return;
                    } catch (SQLException e){
                        System.out.println(e);
                    }
                }
            } catch (Exception e) {
                System.out.println("Invalid.\n");
            }
        }
    }

    public static HashSet<Integer> printListingOptions(ResultSet rs){
        HashSet<Integer> listingSet = new HashSet<>();

        try {

            String format = "%1$-20s| %2$-20s | %3$-15s | %4$-15s | %5$-10s | %6$-15s ";
            System.out.println(String.format(format, "Listing ID", "House Number", "Street Name",
                "City", "Unit Number", "Price"));
            String under = "_";
            for (int i = 0; i < 120; i++){
                under += "_";
            }
            System.out.println(under);

            while (rs.next()){
                int listID = rs.getInt("listID");
                listingSet.add(listID);
//                int hostID = rs.getInt("hostID");
//                int price = rs.getInt("price");
//                double longitude = rs.getDouble("longitude");
//                double latitude = rs.getDouble("latitude");
//                String listingType = rs.getString("listingType");

                //search into listing join located join reserved table for address
                int streetNo, price;
                String streetName;
                String city;
                int unitNo;
                PreparedStatement s1 = con.prepareStatement("select address.streetNo, address.streetName, " +
                    "address.city, address.unitNo, price from listing join located " +
                    "join address where listing.listID = located.listID and located.addressID = " +
                    "address.addressID and listing.listID = ?");
                s1.setInt(1, listID);
                ResultSet rs1 = s1.executeQuery();
                if (rs1.next()){
                    streetNo = rs1.getInt("address.streetNo");
                    streetName = rs1.getString("address.streetName");
                    city = rs1.getString("address.city");
                    unitNo = rs1.getInt("address.unitNo");
                    price = rs1.getInt("price");
                    System.out.println(String.format(format, listID+"", streetNo+"", streetName,
                        city, unitNo+"", price+""));
                }
            }
        } catch (Exception e){
            System.out.println("Unable to complete. Please try again.");
            Renter.handleRenter(username, con);
        }
        return listingSet;
    }

    public static void printAmenityList(HashSet<Integer> listingSet){
        int option;
        while (true){
            System.out.println("Choose listing to display amenities:");

            try {
                option = scan.nextInt();
                scan = new Scanner(System.in);

                if (!listingSet.contains(option)){
                    System.out.println("Option not valid.\n");
                } else{
                    break;
                }
            } catch (Exception e){
                System.out.println("Invalid option. Must be integer.");
            }
        }

        try {
            PreparedStatement s = con.prepareStatement("select * from Amenities join Provides using (amenityID)" +
                " join Listing using (listid) where listing.listID = ?");
            s.setInt(1, option);
            ResultSet rs = s.executeQuery();

            if (rs.next()){

                ArrayList<String> names = new ArrayList<>(List.of("wifi", "washer", "ac", "heating", "tv", "iron", "kitchen",
                    "dryer", "workspace", "hairDryer", "pool", "parking", "crib", "grill", "indoorFireplace", "hotTub", "evCharger",
                    "gym", "breakfast", "smoking", "beachfront", "waterfront", "smokeAlarm", "carbonMonoxideAlarm"));
                ArrayList<Boolean> bools = new ArrayList<>();
                boolean wifi = rs.getBoolean("wifi");
                bools.add(wifi);
                boolean washer = rs.getBoolean("washer");
                bools.add(washer);
                boolean ac = rs.getBoolean("ac");
                bools.add(ac);
                boolean heating = rs.getBoolean("heating");
                bools.add(heating);
                boolean tv = rs.getBoolean("tv");
                bools.add(tv);
                boolean iron = rs.getBoolean("iron");
                bools.add(iron);
                boolean kitchen = rs.getBoolean("kitchen");
                bools.add(kitchen);
                boolean dryer = rs.getBoolean("dryer");
                bools.add(dryer);
                boolean workspace = rs.getBoolean("workspace");
                bools.add(workspace);
                boolean hairDryer = rs.getBoolean("hairDryer");
                bools.add(hairDryer);
                boolean pool = rs.getBoolean("pool");
                bools.add(pool);
                boolean parking = rs.getBoolean("parking");
                bools.add(parking);
                boolean crib = rs.getBoolean("crib");
                bools.add(crib);
                boolean grill = rs.getBoolean("grill");
                bools.add(grill);
                boolean indoorFireplace = rs.getBoolean("indoorFireplace");
                bools.add(indoorFireplace);
                boolean hotTub = rs.getBoolean("hotTub");
                bools.add(hotTub);
                boolean evCharger = rs.getBoolean("evCharger");
                bools.add(evCharger);
                boolean gym = rs.getBoolean("gym");
                bools.add(gym);
                boolean breakfast = rs.getBoolean("breakfast");
                bools.add(breakfast);
                boolean smoking = rs.getBoolean("smoking");
                bools.add(smoking);
                boolean beachfront = rs.getBoolean("beachfront");
                bools.add(beachfront);
                boolean waterfront = rs.getBoolean("waterfront");
                bools.add(waterfront);
                boolean smokeAlarm = rs.getBoolean("smokeAlarm");
                bools.add(smokeAlarm);
                boolean carbonMonoxideAlarm = rs.getBoolean("carbonMonoxideAlarm");
                bools.add(carbonMonoxideAlarm);

                System.out.println();
                for (int i = 0; i < bools.size(); i++){
                    boolean hasAmen = bools.get(i);
                    if (hasAmen)
                        System.out.println(names.get(i) + ": Included");
                    else{
                        System.out.println(names.get(i) + ": Not Included");
                    }
                }
            }

        } catch (Exception e){
//            System.out.println(e);
            System.out.println("Unable to complete.");
            handleRenter(username, con);
        }
    }

    public static void bookListing(HashSet<Integer> listingSet, String startDate, String endDate){
        int option;
        while (true){
            System.out.println("Choose listing to book:");

            try {
                option = scan.nextInt();
                scan = new Scanner(System.in);

                if (!listingSet.contains(option)){
                    System.out.println("Option not valid.\n");
                } else{
                    break;
                }
            } catch (Exception e){
                System.out.println("Invalid option. Must be integer.");
            }
        }

        try {
            //get information needed from the listing id
            PreparedStatement s = con.prepareStatement("select * from listing join owns using (listid) where listing.listID = ?");
            s.setInt(1, option);

            ResultSet rs = s.executeQuery();
            int price = 0, hostID = 0;
            if (rs.next()){
                price = rs.getInt("price");
                hostID = rs.getInt("hostID");
            }



            //add an entry to reserved
            PreparedStatement s2 = con.prepareStatement("insert into reserved (hostID, renterID, listID, " +
                "startDate, endDate, statusAvailable, hostCancelled, price) values (?,?,?,?,?, false, false, ?)");
            s2.setInt(1, hostID);
            s2.setInt(2, username);
            s2.setInt(3, option);
            s2.setString(4, startDate);
            s2.setString(5, endDate);
            s2.setInt(6, price);



            int status = s2.executeUpdate();
            if (status == 1){
                System.out.println("Successfully booked!");
                handleRenter(username, con);
            } else{
                System.out.println("Unable to complete booking. Please try again.");
                handleRenter(username, con);
            }

        } catch (Exception e){
            System.out.println("Unable to complete booking. Please try again.");
//            bookListing(listingSet, startDate, endDate);
            handleRenter(username, con);

        }

    }

    public static void createReservation(int username){
        //get dates
        System.out.println("Starting date of reservation:");
        String startDate = scan.next();
        System.out.println("Ending date of reservation:");
        String endDate = scan.next();

        try {
            PreparedStatement s_date = con.prepareStatement("select * from users where curdate() <= ?");
            s_date.setString(1, startDate);
            ResultSet rs_date = s_date.executeQuery();
            if (!rs_date.next()){
                System.out.println("Start date cannot be in past. Please try again.");
                createReservation(username);
            }
        } catch (Exception e){
            System.out.println("Error. Please try again.");
        }

        //choose city
        System.out.println("City:");
        String city = scan.next();

        System.out.println("Country:");
        String country = scan.next();

        //3 options
        // not in reserved -> add to set
        // in reserved with no overlapping dates
        // in reserved with overlapping dates, but is cancelled


        try {
            String query = "(select listid from listing join located using (listid) join address using (addressid) join " +
                "owns using (listid) where owns.hostid != " + username + " and city = '" + city + "' " +
                "and country = '" + country + "' and listid not in " +
                "(select listid from reserved)) union (select listid from listing join located using (listid) join " +
                "address using (addressid) join owns using (listid) where owns.hostid != " + username +
                " and city = '" + city + "' and country = '" + country + "' and listid not in " +
                "(select listid from listing join reserved using (listid) where (reserved.startDate <= ? " +
                "and reserved.endDate >= ?) and statusAvailable = false))";
            PreparedStatement s = con.prepareStatement(query);

//            s.setString(1, city);
//            s.setString(2, country);
//            s.setString(3, city);
//            s.setString(4, country);
            s.setString(1, endDate);
            s.setString(2, startDate);
            ResultSet rs = s.executeQuery();
            HashSet<Integer> listingSet = printListingOptions(rs);


            //option to either view amenities or book the listing
            int option;
            while (true){
                System.out.println("\nChoose an option:\n" +
                    "(0) Exit to Menu \n" +
                    "(1) View Amenity List for Listing\n" +
                    "(2) Book Listing");
                try {
                    option = scan.nextInt();
                    scan = new Scanner(System.in);
                    if (option == 0){
                        handleRenter(username, con);
                        break;
                    } else if (option == 1){
                        printAmenityList(listingSet);
                        System.out.println();
//                        printListingOptions(rs);
                    } else if (option == 2){
                        //create reservation
                        bookListing(listingSet, startDate, endDate);
                        break;
                    } else {
                        System.out.println("Invalid option.");
                    }
                } catch (Exception e){
                    System.out.println("Invalid type. Must be an integer.");
                }
            }
        } catch (Exception e){
            System.out.println(e);
        }

        //look through each listing

        //select * from listing join reserved where city = ? and (reserved.startDate > ? or reserved.endDate < ?);
        //UNION
        //select * from listing join reserved where city = ? and (reserved.startDate <= ? and reserved.startDate >= ?) and statusAvailable = true;

        //select * from listing join reserved where city = ? and listing.listID = reserved.listID and NOT IN ( select * from listing join reserved where (reserved.startDate <= ? and reserved.endDate >= ?) and statusAvailable = false);

        //display all the listings that are available
        //prompt choosing an option
    }

    public static void writeReview(int username) {

        HashSet<Integer> resSet = new HashSet<>();
        try {

            PreparedStatement s = con.prepareStatement("select * from reserved join listing using (listid) join " +
                "located using (listid) join address using (addressid) where " +
                "statusAvailable = false and endDate < curdate() and renterid = ?");
            s.setInt(1, username);

            ResultSet rs = s.executeQuery();
            String format = "%1$-8s| %2$-8s | %3$-10s | %4$-10s | %5$-10s | %6$-15s | %7$-15s " +
                "| %8$-10s | %9$-10s | %10$-15s | %11$-10s | %12$-10s";
            System.out.println(String.format(format, "ReservationID", "ListID", "HostID",
                "Price", "Start Date", "End Date",
                "House Number", "Street Name", "City", "Province", "Postal Code", "Unit No."));
            String under = "_";
            for (int i = 0; i < 150; i++) {
                under += "_";
            }
            System.out.println(under);
            while (rs.next()) {
                int reservationID = rs.getInt("reservationID");
                resSet.add(reservationID);
                int listID = rs.getInt("listID");
                int hostID = rs.getInt("hostID");
                int price = rs.getInt("price");
                String startDate = rs.getString("startdate");
                String endDate = rs.getString("enddate");
                int streetNo = rs.getInt("streetno");
                String streetName = rs.getString("streetname");
                String city = rs.getString("city");
                String province = rs.getString("province");
                String postalCode = rs.getString("postalcode");
                int unitNo = rs.getInt("unitno");
                System.out.println(String.format(format, reservationID + "", listID + "", hostID + "", price +
                        "", startDate, endDate,
                    streetNo + "", streetName, city, province, postalCode, unitNo + ""));

            }
        } catch (Exception e) {
            System.out.println(e);
        }

        int option;
        while (true) {
            try {
                System.out.println("Choose a reservation ID to review (-1 to exit):");
                option = scan.nextInt();
                scan = new Scanner(System.in);

                if (option == -1){
                    handleRenter(username, con);
                }
                int score;
                if (resSet.contains(option)) {
                    while (true){
                        System.out.println("Rate your stay from 1-5:");
                        try {
                            score = scan.nextInt();
                            scan = new Scanner(System.in);
                            break;
                        } catch (Exception e){
                            System.out.println("Must be an integer.");
                        }
                    }

                    System.out.println("Leave a review:");
                    String review = scan.nextLine();


                    try{
                        //change status on reservation
                        PreparedStatement s = con.prepareStatement("update Reserved set " +
                            "renterReview = ?, renterScore = ? where reservationID = ?");
                        s.setString(1, review);
                        s.setInt(2, score);
                        s.setInt(3, option);
                        int status = s.executeUpdate();
                        if (status == 1){
                            System.out.println("Successfully updated review.");
                        } else {
                            System.out.println("Unable to complete review.");
                        }
                    } catch (SQLException e){
                        System.out.println(e);
                        System.out.println("Unable to complete review.");
                    }
                    handleRenter(username, con);
                }
            } catch (Exception e) {
                System.out.println("Invalid.\n");
            }
        }
    }
}
