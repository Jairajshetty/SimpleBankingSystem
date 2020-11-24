package banking;
import org.sqlite.SQLiteDataSource;

import java.sql.*;
import java.util.*;

public class Main {

    public static String cardNumber="";
    public static int pinNumber=0;
    public static int Balance=0;
    public static String url="";
    public static boolean truth = true;
    public static boolean isLoggedin=true;

    public static void main(String[] args) {
        url = "jdbc:sqlite:"+args[1];
        SQLiteDataSource dataSource=new SQLiteDataSource();
        dataSource.setUrl(url);

        try (Connection con = dataSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                // Statement execution
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS card(" +
                        "id INTEGER PRIMARY KEY," +
                        "number TEXT NOT NULL," +
                        "pin TEXT NOT NULL,"+
                        "balance INTEGER DEFAULT 0)");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        while (truth) {
            PrintLoginOptions();
            Scanner scanner = new Scanner(System.in);
            int LoginOption = scanner.nextInt();
            switch (LoginOption) {
                case 1:CreateAccount();
                    break;
                case 2:LogIntoAccount();
                    break;
                case 0:
                    System.out.println("Bye!");
                    truth = false;
                    break;
                default:break;
            }
        }
    }

    public static void PrintLoginOptions() {
        System.out.println();
        System.out.println("1. Create an account");
        System.out.println("2. Log into account");
        System.out.println("0. Exit");
    }

    public static void CreateAccount() {
        System.out.println();
        System.out.println("Your card has been created");
        Random random = new Random();
        int cardNumberPart1 = random.nextInt(899999999)+100000000;
        int PinNumber = random.nextInt(8999)+1000;
        cardNumber=LhunAlgorithm(cardNumberPart1);
        pinNumber=PinNumber;

        SQLiteDataSource dataSource=new SQLiteDataSource();
        dataSource.setUrl(url);
        try (Connection con = dataSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                // Statement execution
                statement.executeUpdate("INSERT INTO card(number,pin) VALUES("+cardNumber+","+pinNumber+")");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }


        System.out.println("Your card number:");
        System.out.println(cardNumber);
        System.out.println("Your card PIN:");
        System.out.println(pinNumber);
        System.out.println();
    }

    public static void LogIntoAccount(){
        boolean isLoginCredentialsCorrect=false;
        System.out.println();
        System.out.println("Enter your card number:");
        Scanner scanner=new Scanner(System.in);
        String cardNumberInput=scanner.nextLine();
        System.out.println("Enter your PIN:");
        int pinInput=scanner.nextInt();
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);
        try (Connection con = dataSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                try (ResultSet cards = statement.executeQuery("SELECT number,pin,balance FROM card WHERE number = "+cardNumberInput)) {
                    if(cards.getString(1).equals(cardNumberInput)&&cards.getInt(2)==pinInput){
                        isLoginCredentialsCorrect=true;
                        cardNumber=cardNumberInput;
                        pinNumber=pinInput;
                        Balance=cards.getInt(3);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if(isLoginCredentialsCorrect){
            System.out.println();
            System.out.println("You have successfully logged in!");
            System.out.println();

            while(isLoggedin){
                printAccountOptions();
                int AccountOption=scanner.nextInt();
                switch (AccountOption){
                    case 1:checkBalance();
                    break;
                    case 2:addIncome();
                    break;
                    case 3:doTranfer();
                    break;
                    case 4:closeAccount();
                    break;
                    case 5:isLoggedin=logOutFunction();
                    break;
                    case 0:Exit();
                    break;
                    default:break;
                }
            }



        }else{
            System.out.println("Wrong card number or PIN!");
        }
    }

    public static void printAccountOptions(){
        System.out.println("1. Balance");
        System.out.println("2. Add income");
        System.out.println("3. Do transfer");
        System.out.println("4. Close account");
        System.out.println("5. Log out");
        System.out.println("0. Exit");
    }

    public static void checkBalance()
    {
        System.out.println("Balance: "+Balance);
    }
    public static boolean logOutFunction(){
        System.out.println("You have successfully logged out!");
        return false;
    }

    public static String LhunAlgorithm(int cardPart){
        String cardTempNo="400000"+cardPart;
        int[] luhnArray=new int[15];
        String[] cardArr=cardTempNo.split("");
        for(int i=0;i<15;i++){
            luhnArray[i]=Integer.parseInt(cardArr[i]);
        }
        for(int i=0;i<15;i++){
            if(i%2==0){
                luhnArray[i]=luhnArray[i]*2;
            }
        }
        int Total=0;
        for(int i=0;i<15;i++){
            if(luhnArray[i]>9){
                luhnArray[i]-=9;
            }
            Total+=luhnArray[i];
        }
        int checkSum=0;
        if(Total%10!=0){
            checkSum=10-Total%10;
        }


        return cardTempNo+checkSum;

    }

    public static void addIncome(){
        System.out.println();
        Scanner scanner=new Scanner(System.in);
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);

        System.out.println("Enter income:");
        int amount=scanner.nextInt();
        String updateIncome = "UPDATE card SET balance = ? WHERE number = ?";

        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = con.prepareStatement(updateIncome)) {
                preparedStatement.setInt(1, Balance+amount);
                preparedStatement.setString(2, cardNumber);
                preparedStatement.executeUpdate();
                Balance+=amount;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("Income was added!");
        System.out.println();

    }

    public static void closeAccount(){
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);
        String deleteAccount="DELETE FROM card WHERE number = ?";

        try (Connection con = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = con.prepareStatement(deleteAccount)) {
                preparedStatement.setString(1, cardNumber);
                preparedStatement.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        isLoggedin=false;
        System.out.println("The account has been closed!");
    }

    public static void doTranfer(){
        System.out.println();
        Scanner scanner=new Scanner(System.in);
            System.out.println("Transfer");
            System.out.println("Enter card number:");
            String cardNumberToTranfer=scanner.next();

            if(cardNumberToTranfer.equals(cardNumber)){
                System.out.println("You can't transfer money to the same account!");
            }
            else if(!willPassLuhnsAlgorithm(cardNumberToTranfer)){
                System.out.println("Probably you made mistake in the card number. Please try again!");
                System.out.println();
            }
            else if(isCardNotExist(cardNumberToTranfer)){
                System.out.println("Such a card does not exist.");
                System.out.println();
            }
            else{
                System.out.println("Enter how much money you want to transfer:");
                int amount=scanner.nextInt();
                SQLiteDataSource dataSource = new SQLiteDataSource();
                dataSource.setUrl(url);

                try (Connection con = dataSource.getConnection()) {
                    try (Statement statement = con.createStatement()) {
                        try (ResultSet cards = statement.executeQuery("SELECT balance FROM card WHERE number = "+cardNumber)) {
                            if(cards.getInt(1)<amount){
                                System.out.println("Not enough money!");
                                System.out.println();
                            }else{
                                int balanceOfToTranferAccount=0;
                                try (ResultSet cardToTransferDetail = statement.executeQuery("SELECT balance FROM card WHERE number = "+cardNumberToTranfer)) {
                                    balanceOfToTranferAccount=cardToTransferDetail.getInt(1);
                                }


                                String updateIncome = "UPDATE card SET balance = ? WHERE number = ?";

                                try (PreparedStatement preparedStatement = con.prepareStatement(updateIncome)) {
                                    preparedStatement.setInt(1, balanceOfToTranferAccount+amount);
                                    preparedStatement.setString(2, cardNumberToTranfer);
                                    preparedStatement.executeUpdate();
                                    preparedStatement.setInt(1, Balance-amount);
                                    preparedStatement.setString(2, cardNumber);
                                    Balance-=amount;
                                    preparedStatement.executeUpdate();
                                    System.out.println("Success!");
                                    System.out.println();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }


                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
    }

    public static boolean willPassLuhnsAlgorithm(String cardNo){
        String[] cardArray=cardNo.split("");
        int sum=0;
        for(int i=0;i<cardArray.length;i++){
            int e=Integer.parseInt(cardArray[i]);
            if(i%2==0){
                e=e*2;
            }
            if(e>9){
                e-=9;
            }
            sum+=e;
        }
        if(sum%10==0){
            return true;
        }else {
            return false;
        }
    }

    public static boolean isCardNotExist(String cardNo){
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);

        try (Connection con = dataSource.getConnection()) {
            try (Statement statement = con.createStatement()) {
                try (ResultSet cards = statement.executeQuery("SELECT number FROM card WHERE number = "+cardNo)) {
                    if(cards.getString(1).equals(cardNo)){
                        return false;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static void Exit(){
        System.out.println();
        isLoggedin=false;
        truth=false;
        System.out.println("Bye!");
    }

}