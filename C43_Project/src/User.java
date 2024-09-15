import java.sql.*;
import java.util.Scanner;

public class User {
    static Connection con;
    static int user;
    static Scanner scan = new Scanner(System.in);

    public static void handleLogin(Connection connection){
        con = connection;
        int username;
        while (true){
            try {
                System.out.println("Enter username:");
                username = scan.nextInt();
                break;
            } catch (Exception e){
                System.out.println("Must be an integer. Please try again. \n");
            }
        }

        System.out.println("Enter password:");
        String password = scan.next();
        //check to see if this matches in database
        try {
            PreparedStatement s = con.prepareStatement("select * from users where " +
                    "sin = ? and password = ?");
            s.setInt(1, username);
            s.setString(2, password);
            ResultSet rs = s.executeQuery();
            if (rs.next()){
                //entry exists
                user = username;
                handleUserMainMenu(username, con);

            } else {
                System.out.println("Username and password do not match.\n");
                handleLogin(con);
            }
        } catch (SQLException e){
            System.out.println(e);
        }
    }

    public static void handleUserMainMenu(int username, Connection con){
        user = username;
        int option;
        while (true) {
            System.out.println("Choose an option:");
            System.out.println("(0) Back Menu");
            System.out.println("(1) Renter");
            System.out.println("(2) Host");
            System.out.println("(3) Delete Account");
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
            Driver.mainMenu(con);
        } else if (option == 1) {
            Renter.handleRenter(user, con);
        } else if (option == 2) {
            Host.handleHost(user, User.con);
        } else if (option == 3) {
            deleteAccount();
        } else {
            System.out.println("Invalid option.\n");
            handleUserMainMenu(username, con);
        }
    }


    public static void createAccount(Connection connection) {
        con = connection;
        int username = 0, ccMonth = 0, ccYear = 0, cvc = -1;
        String password = "", firstname="", lastname="", occupation = "", dob = "", ccNumber = "";
        try {
            System.out.println("Username (Enter SIN Number):");
            username = scan.nextInt();
//            scan = new Scanner(System.in);
            System.out.println("Password:");
            password = scan.next();
            scan.nextLine();
            System.out.println("First name:");
            firstname = scan.nextLine();
            System.out.println("Last name:");
            lastname = scan.nextLine();
            System.out.println("Occupation:");
            occupation = scan.nextLine();
            System.out.println("Date of birth (YYYY-MM-DD):");
            dob = scan.next();
            System.out.println("Credit Card Number:");
            ccNumber = scan.next();
            System.out.println("Credit Card Expiry Month:");
            ccMonth = scan.nextInt();
            scan = new Scanner(System.in);
            System.out.println("Credit Card Expiry Year:");
            ccYear = scan.nextInt();
            scan = new Scanner(System.in);
            System.out.println("Credit Card CVC:");
            cvc = scan.nextInt();
            scan = new Scanner(System.in);
        } catch (Exception e){
            System.out.println(e);
            createAccount(con);
        }
        try {
            PreparedStatement s = con.prepareStatement("insert into Users values (?,?,?,?,?,?)");
            s.setInt(1, username);
            s.setString(2, password);
            s.setString(3, occupation);
            s.setString(4, firstname);
            s.setString(5, lastname);

            s.setString(6, dob);
            PreparedStatement s2 = con.prepareStatement("insert into Renter values (?,?,?,?,?)");
            s2.setInt(1, username);
            s2.setString(2, ccNumber);
            s2.setInt(3, ccMonth);
            s2.setInt(4, ccYear);
            s2.setInt(5, cvc);

            PreparedStatement s3 = con.prepareStatement("INSERT INTO Hosts values (?)");
            s3.setInt(1, username);

            int status = s.executeUpdate();
            int status2 = s2.executeUpdate();
            int status3 = s3.executeUpdate();

            if ((status == 1) && (status2 == 1) && (status3 == 1)){
                System.out.println("Successfully created.");
            } else{
                System.out.println("Not able to create account.");
            }
        } catch (Exception e){
            System.out.println(e);
            System.out.println("Unsuccessful.");
            createAccount(con);
        }

        // send this back to the main menu
        Driver.mainMenu(con);
    }

    public static void deleteAccount(){
        try {
            PreparedStatement s = con.prepareStatement("delete from Users where sin = " + user);
            PreparedStatement s2 = con.prepareStatement("delete from Renter where renterID = " + user);
            PreparedStatement s3 = con.prepareStatement("delete from Hosts where hostID = " + user);
            int status1 = s.executeUpdate();
            int status2 = s2.executeUpdate();
            int status3 = s3.executeUpdate();
            if ((status1 == 1) && (status2 == 1) && (status3 == 1)) {
                System.out.println("Successfully deleted.\n");
                Driver.mainMenu(con);
            } else{
                System.out.println("Account was not deleted.\n");
                //send back to handling user work
                handleUserMainMenu(user, con);
            }
        } catch (SQLException e){
            System.out.println(e);
            //send back to handler
            handleUserMainMenu(user, con);
        }
        Driver.mainMenu(con);
    }

}
