import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;

import javax.xml.transform.Result;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;

public class ReportsQueries {

    static Connection con;
    static Scanner scan = new Scanner(System.in);

//    static String sentence = "Who is the author of The Call of the Wild?";

    static int user;

    static Set<Integer> listingID = new HashSet<>();
    public static void mainMenu(Connection connection, int username) {
        con = connection;
        user = username;

        //display all options
        System.out.println("\nChoose an option:\n" +
                "(0) Back Menu\n" +
                "(1) Search by Distance\n" +
                "(2) Search by Postal Code\n" +
                "(3) Search by Address\n" +
                "(4) Search by Date Range\n" +
                "(5) Filtered Search\n" +
                "(6) Number of Bookings by City in Date Range\n" +
                "(7) Number of Bookings by City, Zip in Date Range\n" +
                "(8) Number of Listings by Country\n" +
                "(9) Number of Listings by City, Country\n" +
                "(10) Number of Listings by Zip, City, Country\n" +
                "(11) Rank Hosts by Number of Listings - Country\n" +
                "(12) Rank Hosts by Number of Listings - City\n" +
                "(13) Find Potential Commercial Hosts - Country\n" +
                "(14) Find Potential Commercial Hosts - City\n" +
                "(15) Rank Renter by Number of Bookings \n" +
                "(16) Rank Renter by Number of Bookings - City\n" +
                "(17) Rank Hosts by Cancellations in Past Year\n" +
                "(18) Rank Renters by Cancellations in Past Year\n" +
                "(19) Listing Noun Phrases");
        //take in user input and redirect
        int option;
        try {
            option = scan.nextInt();
            scan = new Scanner(System.in);
            if (option == 0) {
                Driver.mainMenu(con);
            } else if (option == 1) {
                findListingByCoord();
            } else if (option == 2) {
                getAdjacentListingsPC();
            } else if (option == 3) {
                searchByAddress();
            } else if (option == 4) {
                searchByTime();
            } else if (option == 5) {
                filteredSearch();
            } else if (option == 6) {
                numBookingsCity();
            } else if (option == 7) {
                numBookingsCityPostal();
            } else if (option == 8) {
                numListingsCountry();
            } else if (option == 9) {
                numListingsCountryCity();
            } else if (option == 10) {
                numListingsCountryCityPostal();
            } else if (option == 11) {
                rankListingHostCountry();
            } else if (option == 12) {
                rankListingHostCountryCity();
            } else if (option == 13) {
                plus10percentListingsCountry();
            } else if (option == 14) {
                plus10percentListingsCityCountry();
            } else if (option == 15) {
                rankRenterBookings();
            } else if (option == 16) {
                rankRenterBookingsCity();
            } else if (option == 17) {
                rankHostCancel();
            } else if (option == 18) {
                rankRenterCancel();
            } else if (option == 19) {
                findNounPhrases();
            } else {
                System.out.println("Invalid option. Please try again.\n");
            }
            mainMenu(con, user);
        } catch (Exception e) {
            System.out.println("Invalid option. Must be integer. Try again.\n");
            mainMenu(con, user);
        }

    }

    //queries
    //by distance

    //recursively loop through tree, extracting noun phrases

    public static HashMap<String,Integer> findNounPhrasesHelper(String sentence, HashMap<String, Integer> count){
        InputStream modelInParse = null;
        try {
            //load chunking model
            modelInParse = new FileInputStream("en-parser-chunking.bin"); //from http://opennlp.sourceforge.net/models-1.5/
            ParserModel model = new ParserModel(modelInParse);

            //create parse tree
            Parser parser = ParserFactory.create(model);
            Parse topParses[] = ParserTool.parseLine(sentence, parser, 1);

            //call subroutine to extract noun phrases
            for (Parse p : topParses)
                getNounPhrases(p,count);

            //print noun phrases
//            for (String s : nounPhrases)
//                System.out.println(s);

            //The Call
            //the Wild?
            //The Call of the Wild? //punctuation remains on the end of sentence
            //the author of The Call of the Wild?
            //the author
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (modelInParse != null) {
                try {
                    modelInParse.close();
                }
                catch (IOException e) {
                }
            }
        }
        return count;
    }
    public static HashMap<String, Integer> getNounPhrases(Parse p, HashMap<String, Integer> count) {

        if (p.getType().equals("NP")) { //NP=noun phrase
//            nounPhrases.add(p.getCoveredText());
            String phrase = p.getCoveredText();
            if(count.containsKey(phrase)) count.put(phrase,count.get(phrase)+1);
            else count.put(phrase, 1);
        }
        for (Parse child : p.getChildren()) getNounPhrases(child, count);
        return count;
    }
    public static void getAllListings(){
        String query = "SELECT listID FROM Listing";
        try{
            PreparedStatement q = con.prepareStatement(query);
            ResultSet rs = q.executeQuery();
            while(rs.next()){
                listingID.add(rs.getInt(1));
            }
        }catch(Exception E){
            System.out.println(E);
        }
    }
    static Hashtable<Integer, LinkedHashMap<String, Integer>> listRevPairs = new Hashtable<>();
    public static void findNounPhrases(){
        getAllListings();
//        System.out.println(listingID);
        for(Integer i: listingID){
//            System.out.println(i);
            HashMap<String, Integer> nounPhrases = new HashMap<>();
            try{
                String query = "SELECT renterReview FROM Reserved WHERE listID = ? AND hostID != renterID AND statusAvailable = false AND renterReview IS NOT NULL";
                PreparedStatement q = con.prepareStatement(query);
                q.setInt(1,i);
                ResultSet rs = q.executeQuery();
                while(rs.next()){
                    String rev = rs.getString(1);
//                    System.out.println(rev);
//                    String[] sentences = rev.split(".");
//                    System.out.println(sentences);
//                    for(String j: sentences){
//                        System.out.println(rev);
                        nounPhrases = findNounPhrasesHelper(rev, nounPhrases);
//                    }
                }
            }catch(Exception E){
                System.out.println(E);
            }
            LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();
            ArrayList<Integer> list = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : nounPhrases.entrySet()) {
                list.add(entry.getValue());
            }
            Collections.sort(list);
            for (int num : list) {
                for (Map.Entry<String, Integer> entry : nounPhrases.entrySet()) {
                    if (entry.getValue().equals(num)) {
                        sortedMap.put(entry.getKey(), num);
                    }
                }
            }
            listRevPairs.put(i, sortedMap);
        }
        printList();
        listRevPairs = null;
    }
    public static void printList(){
        for(Integer i: listRevPairs.keySet()){
            System.out.println("Listing ID: " + i);
            HashMap<String, Integer> values = listRevPairs.get(i);
            if(values.isEmpty()) System.out.println("No Reviews\n-------------------------------------------------");
            Integer count = 1;
            for(String j: values.keySet()){
                System.out.println("    "+count + ". "+j);
                count +=1;
            }
        }
    }
    public static void findListingByCoord() {
        Double lon, lat, distance = 0.0;

        while (true) {
            System.out.println("Latitude:");
            lon = scan.nextDouble();
            System.out.println("Longitude:");
            lat = scan.nextDouble();
            if (!(lon >= -85 || lon <= 85 || lat >= -180 || lat <= 180)) System.out.println("Values out of range for longitude or latitude");
            System.out.println("Distance (km):");
            distance = scan.nextDouble();
            if (distance > 1) break;
            else System.out.println("Distance can't be negative");
        }
        String query = "SELECT * \n" +
                "FROM Listing join Located using(listID) join Address using(addressID)\n" +
                "WHERE ST_Distance_Sphere(point(?,?), point(longitude,latitude))/1000 <= ?";
        try {
            PreparedStatement query1 = con.prepareStatement(query);
            query1.setDouble(1, lon);
            query1.setDouble(2, lat);
            query1.setDouble(3, distance);
            ResultSet rs = query1.executeQuery();
            Host.printListings(rs);
        } catch (Exception E) {
            System.out.println(E);
        }


    }


    //add search mechanics

    // query by address
    public static void searchByAddress() {
        System.out.println("House Number:");
        int houseNumber;
        while (true) {
            try {
                houseNumber = scan.nextInt();
                scan = new Scanner(System.in);
                break;
            } catch (Exception e) {
                System.out.println("Invalid. Must be integer.");
            }
        }
        System.out.println("Street Name:");
        String streetName = scan.nextLine();
        System.out.println("City:");
        String city = scan.nextLine();
        System.out.println("Province/State:");
        String prov = scan.nextLine();
        System.out.println("Country:");
        String country = scan.nextLine();
        System.out.println("Postal Code:");
        String postalCode = scan.nextLine();


        System.out.println("Unit Number (If no Unit Number, type -1):");
        int unitNumber;
        while (true) {
            try {
                unitNumber = scan.nextInt();
                scan = new Scanner(System.in);
                break;
            } catch (Exception e) {
                System.out.println("Invalid. Must be integer.");
            }
        }

        try {
            String query = "select listing.listID, listingType, price, streetNo, " +
                    "streetName, city, province, country, postalcode, unitNo from listing join located join " +
                    "address where " + "listing.listid = located.listid and address.addressid = located.addressid and " +
                    "streetNo = ? and streetName = ? and city = ? and province = ? and country = ? " +
                    "and postalCode = ? and unitNo = ";

            PreparedStatement s;
            if (unitNumber < 0) {
                query += "null";
                s = con.prepareStatement(query);

            } else {
                query += "?";
                s = con.prepareStatement(query);
                s.setInt(7, unitNumber);
            }
            s.setInt(1, houseNumber);
            s.setString(2, streetName);
            s.setString(3, city);
            s.setString(4, prov);
            s.setString(5, country);
            s.setString(6, postalCode);



            ResultSet rs = s.executeQuery();

            String format = "%1$-8s| %2$-20s | %3$-10s | %4$-15s | %5$-10s | %6$-10s | %7$-15s " +
                    "| %8$-10s | %9$-15s | %10$-15s";
            System.out.println(String.format(format, "ListID", "ListingType", "Price",
                    "House Number", "Street Name", "City", "Province", "Country", "Postal Code", "Unit No."));
            String under = "_";
            for (int i = 0; i < 150; i++) {
                under += "_";
            }
            System.out.println(under);

            int counter = 0;
            while (rs.next()) {
                int listID = rs.getInt("listID");
                String listType = rs.getString("listingType");
                int price = rs.getInt("price");
                System.out.println(String.format(format, listID + "", listType, price + "",
                        houseNumber + "", streetName, city, prov, country, postalCode, unitNumber + ""));
                counter++;
            }
            if (counter == 0) {
                System.out.println("No Entries. \n");
                ReportsQueries.mainMenu(con, user);
            }


        } catch (Exception e) {
            System.out.println("Unable to complete query.");
        }
        mainMenu(con, user);

    }


    //return all listings available in certain date frame

    public static void searchByTime(){
        System.out.println("Starting date:");
        String startDate = scan.nextLine();
        System.out.println("Ending date:");
        String endDate = scan.nextLine();
        String query = "((select listing.listid from listing where listid not in " +
                "(select listid from reserved)) union (select listing.listID from listing " +
                "join reserved where listing.listid = reserved.listid and listing.listid not in " +
                "(select listing.listID from listing join reserved where reserved.listid = listing.listid " +
                "and (reserved.startDate <= ? and reserved.endDate >= ?) and statusAvailable = false)))";

        try {
            PreparedStatement s = con.prepareStatement(query);
            s.setString(1, endDate);
            s.setString(2, startDate);
            ResultSet rs = s.executeQuery();

            //print out all the listings
            printListingOptions(rs);
            //send back to the menu
            mainMenu(con, user);

        } catch (Exception e){
            System.out.println("Unable to complete. Please try again.");
            mainMenu(con, user);
        }
    }


    // filter searches
    private static void printListingOptions(ResultSet rs){
        try {

            String format = "%1$-20s| %2$-15s | %3$-15s | %4$-15s | %5$-15s | %6$-15s ";
            System.out.println(String.format(format, "Listing ID", "House Number", "Street Name",
                    "City", "Unit Number", "Price"));
            String under = "_";
            for (int i = 0; i < 100; i++){
                under += "_";
            }
            System.out.println(under);

            int counter = 0;

            while (rs.next()){
                int listID = rs.getInt("listID");
//                listingSet.add(listID);
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
                counter++;
            }
            if (counter == 0){
                System.out.println("No entries.\n");
            }
        } catch (Exception e){
            System.out.println("Unable to complete. Please try again.");
            mainMenu(con, user);
        }
    }

    public static void filteredSearch() {

//        String query = "select listing.listid from listing join located join address join provides join amenities " +
//                "join reserved where listing.listid = reserved.listid and " +
//                "listing.listid = located.listid and address.addressid = located.addressid and listing.listid = " +
//                "provides.listid and provides.amenityID = amenities.amenityID";
        String query = "select listid from listing join located using (listid) " +
                "join address using (addressid) " +
                "join provides using (listid) join amenities using (amenityID) where true=true";

        //search by country, city, postal code
        System.out.println("Search by country (y/n):");
        String option = scan.nextLine();
        if (option.toLowerCase().equals("y")) {
            System.out.println("Country:");
            String country = scan.nextLine();
            query += " and address.country = '" + country + "'";

            System.out.println("Add city to search (y/n):");
            option = scan.nextLine();
            if (option.toLowerCase().equals("y")) {
                System.out.println("City:");
                String city = scan.nextLine();
                query += " and address.city = '" + city + "'";

                System.out.println("Add postal code to search (y/n):");
                option = scan.nextLine();
                if (option.toLowerCase().equals("y")) {
                    System.out.println("Postal Code:");
                    String postal = scan.nextLine();
                    query += " and address.postalCode = '" + postal + "'";

                    System.out.println("Add street name to search (y/n):");
                    option = scan.nextLine();
                    if (option.toLowerCase().equals("y")){
                        System.out.println("Street Name:");
                        String streetName = scan.nextLine();
                        query += " and address.streetName = '" + streetName + "'";

                        System.out.println("Add house number to search (y/n):");
                        option = scan.nextLine();
                        if (option.toLowerCase().equals("y")){
                            int houseNum;
                            while (true){
                                try {
                                    System.out.println("House Number:");
                                    houseNum = scan.nextInt();
                                    scan = new Scanner(System.in);
                                    break;
                                } catch (Exception e){
                                    System.out.println("Must be an integer. Try again.");
                                }

                            }
                            query += " and address.streetNo = " + houseNum;
                        }

                    }
                }
            }

        }

        //search by price
        System.out.println("Add price range to search (y/n):");
        option = scan.nextLine();
        if (option.toLowerCase().equals("y")) {
            while (true) {
                double lower, upper;
                try {
                    System.out.println("Lower bound of price:");
                    lower = scan.nextDouble();
                    scan = new Scanner(System.in);
                    System.out.println("Upper bound of price:");
                    upper = scan.nextDouble();
                    scan = new Scanner(System.in);
                    query += " and price >= " + lower + " and price <= " + upper;
                    break;

                } catch (Exception e) {
                    System.out.println("Must be integer. ");
                }
            }
        }

        //search by listing type
        System.out.println("Search by listing type (y/n):");
        option = scan.nextLine();
        if (option.toLowerCase().equals("y")) {
            System.out.println("Listing Type:");
            String listType = scan.nextLine();
            query += " and listing.listingType = '" + listType + "'";
        }


        //search by amenities
        System.out.println("Search by amenities (y/n):");
        option = scan.nextLine();
        if (option.toLowerCase().equals("y")){
            ArrayList<Integer> choices = new ArrayList<>();
            ArrayList<String> names = new ArrayList<>(List.of("wifi", "washer", "ac", "heating", "tv", "iron", "kitchen",
                    "dryer", "workspace", "hairDryer", "pool", "parking", "crib", "grill", "indoorFireplace", "hotTub", "evCharger",
                    "gym", "breakfast", "smoking", "beachfront", "waterfront", "smokeAlarm", "carbonMonoxideAlarm"));

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
                System.out.println("Amenities Chosen: " + choices);
                Integer option_amen = scan.nextInt();
                scan = new Scanner(System.in);
                if (option_amen == 0) {
                    bre = 1;
                } else {
                    if (!choices.contains(option_amen)) {
                        choices.add(option_amen);
                    } else {
                        System.out.println("Option already selected.");
                    }
                }
            }

            for (int i = 0; i < 24; i ++){
                if (choices.contains(i))
                    query += " and " + names.get(i) + "= true";
//                else query += " and " + names.get(i) + "= false";
            }
        }



        //search by time frame
        System.out.println("Add time frame availability to search (y/n):");
        option = scan.nextLine();
        if (option.toLowerCase().equals("y")){
            System.out.println("Starting date:");
            String startDate = scan.nextLine();
            System.out.println("Ending date:");
            String endDate = scan.nextLine();
//            query += " intersects ((select listing.listid from listing where listid not in " +
//                    "(select listid from reserved)) union (select listing.listID from listing " +
//                    "join reserved where listing.listid = reserved.listid and listing.listid not in " +
//                    "(select listing.listID from listing join reserved where reserved.listid = listing.listid " +
//                    "and (reserved.startDate <= '" + endDate + "' and reserved.endDate >= '" + startDate + "' " +
//                    ") and statusAvailable = false)))";
            query += " and listid in (Select listid from listing where listid not in (select listid from reserved) " +
                    "union (select listid from listing where listid not in (select listID from listing " +
                    "join reserved using (listid) where startDate <= '" + endDate + "'and endDate >= '" +
                    startDate + "' and statusAvailable = false)))";
        }

        //all filters are completed
        //now have a list of listing id's
        try {
            PreparedStatement s = con.prepareStatement(query);
            ResultSet rs = s.executeQuery();

            //print out all the listings
            printListingOptions(rs);
            //send back to the menu
            mainMenu(con, user);

        } catch (Exception e){
            System.out.println("Unable to complete. Please try again.");
            mainMenu(con, user);
        }
    }


//////////////////////////////////////////////////
    // reports


    // num of bookings in date range by city, country
    public static void numBookingsCity() {
        System.out.println("Starting date:");
        String startDate = scan.nextLine();
        System.out.println("Ending date:");
        String endDate = scan.nextLine();


        try {
            String query = "select city, country, count(reserved.listid) as total from " +
                    "reserved join located using (listid) join address using (addressid) " +
                    "where startDate <= ? and endDate >= ? and statusAvailable = false " +
                    "group by city, country";

            PreparedStatement s = con.prepareStatement(query);

            s.setString(1, endDate);
            s.setString(2, startDate);
            ResultSet rs = s.executeQuery();


            //print out all the listings
            int counter = 0;
            String format = "%1$-8s| %2$-10s | %3$-10s";
            System.out.println(String.format(format, "City", "Country", "Count"));
            String under = "_";
            for (int i = 0; i < 30; i++) {
                under += "_";
            }
            System.out.println(under);
            while (rs.next()) {
                String city = rs.getString("city");
                String country = rs.getString("country");
                int totalCount = rs.getInt("total");
                System.out.println(String.format(format, city, country, totalCount+""));
                counter++;
            }
            if (counter == 0) {
                System.out.println("No Entries.\n");
                ReportsQueries.mainMenu(con, user);
            }


        } catch (Exception e) {
            System.out.println("Unable to complete.");
        }
        ReportsQueries.mainMenu(con, user);
    }

    //num of bookings in date range by city, country, postal code
    public static void numBookingsCityPostal() {
        System.out.println("Starting date:");
        String startDate = scan.nextLine();
        System.out.println("Ending date:");
        String endDate = scan.nextLine();


        try {
            String query = "select city, country, postalCode, count(reserved.listid) as total from " +
                    "reserved join located using (listid) join address using (addressid) " +
                    "where startDate <= ? and endDate >= ? and statusAvailable = false " +
                    "group by city, country, postalCode";

            PreparedStatement s = con.prepareStatement(query);

            s.setString(1, endDate);
            s.setString(2, startDate);
            ResultSet rs = s.executeQuery();

            //print out all the listings
            int counter = 0;
            String format = "%1$-8s| %2$-10s | %3$-15s | %4$-10s";
            System.out.println(String.format(format, "City", "Country", "Postal Code", "Count"));
            String under = "_";
            for (int i = 0; i < 50; i++) {
                under += "_";
            }
            System.out.println(under);
            while (rs.next()) {
                String city = rs.getString("city");
                String country = rs.getString("country");
                String postal = rs.getString("postalCode");
                int totalCount = rs.getInt("total");
                System.out.println(String.format(format, city, country, postal, totalCount+""));
                counter++;
            }
            if (counter == 0) {
                System.out.println("No Entries.\n");
                ReportsQueries.mainMenu(con, user);
            }


        } catch (Exception e) {
            System.out.println("Unable to complete.");
        }
        ReportsQueries.mainMenu(con, user);
    }


    // total num of listings per country, city, postal code
    // country, country and city, country and city and postal code

    public static void numListingsCountry() {
        try {
            PreparedStatement s = con.prepareStatement("select country, count(listing.listid) as total from " +
                    "listing join located join address where listing.listid = located.listid and " +
                    "located.addressid = address.addressid group by country");
            ResultSet rs = s.executeQuery();

            //print out all the listings
            int counter = 0;
            String format = "%1$-15s| %2$-6s";
            System.out.println(String.format(format, "Country", "Count"));
            String under = "_";
            for (int i = 0; i < 25; i++) {
                under += "_";
            }
            System.out.println(under);
            while (rs.next()) {
                String country = rs.getString("country");
                int totalCount = rs.getInt("total");
                System.out.println(String.format(format, country, totalCount+""));
                counter++;
            }
            if (counter == 0) {
                System.out.println("No Entries.\n");
                ReportsQueries.mainMenu(con, user);
            }

        } catch (Exception e){
            System.out.println("Unable to complete.");
            mainMenu(con, user);
        }
    }

    public static void numListingsCountryCity() {
        try {
            PreparedStatement s = con.prepareStatement("select country, city, count(listing.listid) as total from " +
                    "listing join located join address where listing.listid = located.listid and " +
                    "located.addressid = address.addressid group by country, city");
            ResultSet rs = s.executeQuery();

            //print out all the listings

            String format = "%1$-8s| %2$-10s | %3$-10s";
            System.out.println(String.format(format, "Country", "City", "Count"));
            String under = "_";
            for (int i = 0; i < 40; i++) {
                under += "_";
            }
            System.out.println(under);
            int counter = 0;
            while (rs.next()) {
                String country = rs.getString("country");
                String city = rs.getString("city");
                int totalCount = rs.getInt("total");
                System.out.println(String.format(format, country, city, totalCount+""));
                counter++;
            }
            if (counter == 0) {
                System.out.println("No Entries.\n");
                ReportsQueries.mainMenu(con, user);
            }

        } catch (Exception e){
            System.out.println("Unable to complete.");
            mainMenu(con, user);
        }
    }

    public static void numListingsCountryCityPostal() {
        try {
            PreparedStatement s = con.prepareStatement("select country, city, postalCode, count(listing.listid) as total from " +
                    "listing join located join address where listing.listid = located.listid and " +
                    "located.addressid = address.addressid group by country, city, postalCode");
            ResultSet rs = s.executeQuery();

            //print out all the listings
            String format = "%1$-8s| %2$-10s | %3$-15s | %4$-10s";
            System.out.println(String.format(format, "Country", "City", "Postal Code", "Count"));
            String under = "_";
            for (int i = 0; i < 50; i++) {
                under += "_";
            }
            int counter = 0;
            System.out.println(under);
            while (rs.next()) {
                String country = rs.getString("country");
                String city = rs.getString("city");
                String postalCode = rs.getString("postalCode");
                int totalCount = rs.getInt("total");
                System.out.println(String.format(format, country, city, postalCode, totalCount+""));
                counter++;
            }
            if (counter == 0) {
                System.out.println("No Entries.\n");
                ReportsQueries.mainMenu(con, user);
            }

        } catch (Exception e){
            System.out.println("Unable to complete.");
            mainMenu(con, user);
        }
    }


    //rank hosts by number of listings in country and city

    public static void rankListingHostCountry() {
        System.out.println("Country:");
        String country = scan.nextLine();

        try {
//            PreparedStatement s = con.prepareStatement("select listing.hostID, users.firstName, users.lastName, " +
//                    "count(listing.listID) as total_list from Users join listing join located join address" +
//                    "where listing.hostID = users.sin and listing.listID = located.listID and " +
//                    "located.addressID = address.addressID and address.country = ?" +
//                    "group by listing.hostID order by total_list DESC");
            PreparedStatement s = con.prepareStatement("select users.sin, users.firstName, users.lastName, " +
                    "count(listing.listID) as total_list from Users join owns join listing join located join address " +
                    "where owns.listID = listing.listID and owns.hostID = users.sin and " +
                    "listing.listID = located.listID and " +
                    "located.addressID = address.addressID and address.country = ? " +
                    "group by users.sin, firstName, lastName order by total_list DESC");
            s.setString(1, country);
            ResultSet rs = s.executeQuery();

            String format = "%1$-12s| %2$-10s | %3$-10s | %4$-10s";
            System.out.println(String.format(format, "Host ID", "First Name", "Last Name", "Count"));
            String under = "_";
            for (int i = 0; i < 60; i++) {
                under += "_";
            }
            System.out.println(under);
            int counter = 0;
            while (rs.next()) {
                int hostID = rs.getInt("users.sin");
                String firstName = rs.getString("users.firstName");
                String lastName = rs.getString("users.lastName");
                int count = rs.getInt("total_list");
                System.out.println(String.format(format, hostID + "", firstName, lastName, count + ""));
                counter++;
            }
            if (counter == 0) {
                System.out.println("No Entries.\n");
                ReportsQueries.mainMenu(con, user);
            }

        } catch (Exception e) {
            System.out.println("Unable to complete.");
        }
        ReportsQueries.mainMenu(con, user);
    }

    public static void rankListingHostCountryCity() {
        System.out.println("Country:");
        String country = scan.nextLine();

        System.out.println("City:");
        String city = scan.nextLine();

        try {
//            PreparedStatement s = con.prepareStatement("select listing.hostID, users.firstName, users.lastName, count(listing.listID) as total_list from Users join listing join located join address" +
//                    "where listing.hostID = users.sin and listing.listID = located.listID and " +
//                    "located.addressID = address.addressID and address.country = ? and address.city = ?" +
//                    "group by listing.hostID order by total_list DESC");
            PreparedStatement s = con.prepareStatement("select sin, users.firstName, users.lastName, " +
                    "count(listing.listID) as total_list from Users join owns join listing join located join address " +
                    "where owns.listID = listing.listID and owns.hostID = users.sin and " +
                    "listing.listID = located.listID and " +
                    "located.addressID = address.addressID and address.country = ? and address.city = ? " +
                    "group by sin, firstName, lastName order by total_list DESC");
            s.setString(1, country);
            s.setString(2, city);
            ResultSet rs = s.executeQuery();

            int counter = 0;
            String format = "%1$-12s| %2$-10s | %3$-10s | %4$-10s";
            System.out.println(String.format(format, "Host ID", "First Name", "Last Name", "Count"));
            String under = "_";
            for (int i = 0; i < 60; i++) {
                under += "_";
            }
            System.out.println(under);
            while (rs.next()) {
                int hostID = rs.getInt("sin");
                String firstName = rs.getString("users.firstName");
                String lastName = rs.getString("users.lastName");
                int count = rs.getInt("total_list");
                System.out.println(String.format(format, hostID + "", firstName, lastName, count + ""));
                counter++;
            }
            if (counter == 0) {
                System.out.println("No Entries.\n");
                ReportsQueries.mainMenu(con, user);
            }

        } catch (Exception e) {
            System.out.println("Unable to complete.");
        }
        ReportsQueries.mainMenu(con, user);
    }


    //helper for getting total listings in a country
    public static void getAdjacentListingsPC(){
        System.out.println("Enter Postal Code:");
        String postalCode = scan.nextLine();
        postalCode.strip();
        String lastChar = postalCode.substring(postalCode.length() - 1);
        Integer lstChar = Integer.valueOf(lastChar);
        String query = "SELECT * FROM Listings join Located using(listID) join Address using(addressID) where postalCode = ? or postalCode = ? or postalCode = ?";
        try {
            PreparedStatement query1 = con.prepareStatement(query);
            query1.setString(1,postalCode);
            query1.setString(2, postalCode.substring(0,postalCode.length()-1) + (lstChar + 1));
            query1.setString(2, postalCode.substring(0,postalCode.length()-1) + (lstChar - 1));
            ResultSet rs = query1.executeQuery();
            Host.printListings(rs);
        }catch(Exception E){
            System.out.println(E);
        }
    }
    public static int getTotalListingsCountry(String country) {
        try {
            PreparedStatement s = con.prepareStatement("select * from listing join located using (listid)" +
                    " join address using (addressid) where country = ?");
            s.setString(1, country);
            ResultSet rs = s.executeQuery();

            int counter = 0;
            while (rs.next()) {
                counter++;
            }
            return counter;
        } catch (Exception e) {
            System.out.println("Unable to complete.");
        }
        return -1;
    }

    //helper for getting total listings in a city+country
    public static int getTotalListingsCountryCity(String country, String city) {
        try {
            PreparedStatement s = con.prepareStatement("select * from listing join located using (listid)" +
                    " join address using (addressid) where country = ? and city = ?");
            s.setString(1, country);
            s.setString(2, city);
            ResultSet rs = s.executeQuery();

            int counter = 0;
            while (rs.next()) {
                counter++;
            }
            return counter;
        } catch (Exception e) {
            System.out.println("Unable to complete.");
        }
        return -1;
    }


    // hosts with 10% or more of countries/cities total listings
    public static void plus10percentListingsCountry() {

        System.out.println("Country:");
        String country = scan.nextLine();
        int totalListings = getTotalListingsCountry(country);

        int tenPercList = totalListings / 10;

        try {
            String query = "select hostID, firstName, lastName, count(listID) as total from owns join Listing" +
                    " using (listid) join located using (listid) join address using (addressID) join " +
                    "Users where users.sin = owns.hostid " +
                    "and country = ? group by hostID order by total DESC";
            PreparedStatement s = con.prepareStatement(query);
            s.setString(1, country);
            ResultSet rs = s.executeQuery();

            int counter = 0;
            String format = "%1$-12s| %2$-15s | %3$-15s | %4$-15s";
            System.out.println(String.format(format, "Host ID", "First Name", "Last Name", "Count"));
            String under = "_";
            for (int i = 0; i < 60; i++) {
                under += "_";
            }
            System.out.println(under);
            while (rs.next()) {
                int hostID = rs.getInt("hostID");
                String firstName = rs.getString("firstName");
                String lastName = rs.getString("lastName");
                int count = rs.getInt("total");
                if (count >= tenPercList) {
                    counter++;
                    System.out.println(String.format(format, hostID + "", firstName, lastName, count + ""));
                }
            }
            if (counter == 0) {
                System.out.println("No Entries.\n");
            }

        } catch (Exception e) {
            System.out.println("Unable to complete.");
        }
        ReportsQueries.mainMenu(con, user);
    }

    public static void plus10percentListingsCityCountry() {
        System.out.println("City:");
        String city = scan.nextLine();
        System.out.println("Country:");
        String country = scan.nextLine();
        int totalListings = getTotalListingsCountryCity(country, city);

        int tenPercList = totalListings / 10;

        try {
            String query = "select hostID, firstName, lastName, count(listID) as total from owns join Listing" +
                    " using (listid) join located using (listid) join address using (addressID) join " +
                    "Users where users.sin = owns.hostid " +
                    "and country = ? and city = ? group by hostID order by total DESC";
            PreparedStatement s = con.prepareStatement(query);
            s.setString(1, country);
            s.setString(2, city);
            ResultSet rs = s.executeQuery();

            int counter = 0;
            String format = "%1$-12s| %2$-15s | %3$-15s | %4$-15s";
            System.out.println(String.format(format, "Host ID", "First Name", "Last Name", "Count"));
            String under = "_";
            for (int i = 0; i < 60; i++) {
                under += "_";
            }
            System.out.println(under);
            while (rs.next()) {
                int hostID = rs.getInt("hostID");
                String firstName = rs.getString("firstName");
                String lastName = rs.getString("lastName");
                int count = rs.getInt("total");
                if (count >= tenPercList) {
                    counter++;
                    System.out.println(String.format(format, hostID + "", firstName, lastName, count + ""));
                }
            }
            if (counter == 0) {
                System.out.println("No Entries.\n");
            }

        } catch (Exception e) {
            System.out.println(e);
            System.out.println("Unable to complete.");
        }
        ReportsQueries.mainMenu(con, user);
    }


    //rank renters by number of bookings in a specific time period

    public static void rankRenterBookings() {
        System.out.println("Starting date:");
        String startDate = scan.nextLine();
        System.out.println("Ending date:");
        String endDate = scan.nextLine();

        try {
//            PreparedStatement s = con.prepareStatement("select reserved.renterID, users.firstName, users.lastName, " +
//                    "count(reservationID) as total_bookings from users join reserved where users.sin = reserved.renterID" +
//                    "and (reserved.startDate <= ? and reserved.endDate >= ?) and statusAvailable = false");
            String query = "select renterid, firstname, lastname, count(reservationid) as total from users join" +
                    " reserved where sin = renterid and reserved.startDate <= ? and reserved.endDate >= ?" +
                    " and statusAvailable = false group by renterid order by total DESC";
//            System.out.println(query);
            PreparedStatement s = con.prepareStatement(query);
            s.setString(1, endDate);
            s.setString(2, startDate);
            ResultSet rs = s.executeQuery();

            int counter = 0;
            String format = "%1$-15s| %2$-15s | %3$-15s | %4$-10s";
            System.out.println(String.format(format, "Renter ID", "First Name", "Last Name", "Count"));
            String under = "_";
            for (int i = 0; i < 60; i++) {
                under += "_";
            }
            System.out.println(under);
            while (rs.next()) {
                int renterID = rs.getInt("renterID");
                String firstName = rs.getString("firstName");
                String lastName = rs.getString("lastName");
                int count = rs.getInt("total");
                System.out.println(String.format(format, renterID + "", firstName, lastName, count + ""));
                counter++;
            }
            if (counter == 0) {
                System.out.println("No Entries.\n");
                ReportsQueries.mainMenu(con, user);
            }

        } catch (Exception e) {
            System.out.println("Unable to complete.");
        }
        ReportsQueries.mainMenu(con, user);

    }


    //same for city - only interested in 2+ bookings

    public static void rankRenterBookingsCity() {
//        System.out.println("Starting date of reservation:");
//        String startDate = scan.nextLine();
//        System.out.println("Ending date of reservation:");
//        String endDate = scan.nextLine();

        System.out.println("City:");
        String city = scan.nextLine();

        System.out.println("Country:");
        String country = scan.nextLine();

        try {
            String query = "select renterid, firstname, lastname, count(reservationid) as total from users join" +
                    " reserved join listing using (listid) join located using (listid) join " +
                    "address using (addressid) where sin = renterid and reserved.startDate <= ? and " +
                    "reserved.endDate >= ? and statusAvailable = false and city = ? and country = ? " +
                    "group by renterid order by total DESC";
//            System.out.println(query);
            PreparedStatement s = con.prepareStatement(query);
            s.setString(1, "2022-08-08");
            s.setString(2, "2021-08-08");
            s.setString(3, city);
            s.setString(4, country);
            ResultSet rs = s.executeQuery();


            String format = "%1$-15s| %2$-15s | %3$-15s | %4$-10s";
            System.out.println(String.format(format, "Renter ID", "First Name", "Last Name", "Count"));
            String under = "_";
            for (int i = 0; i < 60; i++) {
                under += "_";
            }
            System.out.println(under);
            int counter = 0;
            while (rs.next()) {
                int renterID = rs.getInt("renterID");
                String firstName = rs.getString("firstName");
                String lastName = rs.getString("lastName");
                int count = rs.getInt("total");
                if (count >= 2) {
                    System.out.println(String.format(format, renterID + "", firstName, lastName, count + ""));
                    counter++;
                }
            }
            if (counter == 0) {
                System.out.println("No Entries.\n");
            }

        } catch (Exception e) {
            System.out.println("Unable to complete.");
        }
        ReportsQueries.mainMenu(con, user);
    }


    // rank hosts by largest number of cancellations

    public static void rankHostCancel() {
        try {
            String query = "select hostid, firstname, lastname, count(reservationid) as total from reserved join users " +
                    "where users.sin = reserved.renterid and statusAvailable = true and hostCancelled = true " +
                    "and users.sin = reserved.hostID and startDate >= '2021-08-01' "+
                    "group by reserved.hostID order by total DESC";
            PreparedStatement s = con.prepareStatement(query);
            ResultSet rs = s.executeQuery();


            int counter = 0;
            String format = "%1$-15s| %2$-15s | %3$-15s | %4$-25s";
            System.out.println(String.format(format, "Host ID", "First Name", "Last Name", "Total Cancellations"));
            String under = "_";
            for (int i = 0; i < 80; i++) {
                under += "_";
            }
            System.out.println(under);
            while (rs.next()) {
                int hostID = rs.getInt("hostID");
                String firstName = rs.getString("firstName");
                String lastName = rs.getString("lastName");
                int count = rs.getInt("total");
                System.out.println(String.format(format, hostID + "", firstName, lastName, count + ""));
                counter++;
            }
            if (counter == 0) {
                System.out.println("No Entries.\n");
                ReportsQueries.mainMenu(con, user);
            }


        } catch (Exception e) {
            System.out.println("Unable to complete query.");
            mainMenu(con, user);
        }
    }


    // rank renters by largest number of cancellations

    public static void rankRenterCancel() {
        try {
            String query = "select renterid, firstname, lastname, count(reservationid) as total from reserved join users " +
                    "where users.sin = reserved.renterid and statusAvailable = true and hostCancelled = false " +
                    "and users.sin = reserved.hostID and startDate >= '2021-08-01' "+
                    "group by reserved.renterid order by total DESC";
            PreparedStatement s = con.prepareStatement(query);
            ResultSet rs = s.executeQuery();


            int counter = 0;
            String format = "%1$-15s| %2$-15s | %3$-15s | %4$-20s";
            System.out.println(String.format(format, "Renter ID", "First Name", "Last Name", "Total Cancellations"));
            String under = "_";
            for (int i = 0; i < 70; i++) {
                under += "_";
            }
            System.out.println(under);
            while (rs.next()) {
                int renterID = rs.getInt("renterID");
                String firstName = rs.getString("firstName");
                String lastName = rs.getString("lastName");
                int count = rs.getInt("total");
                System.out.println(String.format(format, renterID + "", firstName, lastName, count + ""));
                counter++;
            }
            if (counter == 0) {
                System.out.println("No Entries.\n");
                ReportsQueries.mainMenu(con, user);
            }


        } catch (Exception e) {
            System.out.println("Unable to complete query.");
            mainMenu(con, user);
        }
    }


}
