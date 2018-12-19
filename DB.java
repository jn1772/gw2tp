
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DB {
    
    Connection con;
    Statement st;
    ResultSet result;
    
    Date stamp;
    
    long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;
    
    boolean newDB;
    
    int errorcode = 0;
    
    public String getDate(Date d){
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        return sdf.format(d);
    }
    
    public void putStamp(){
        System.out.println("put new stamp");
        stamp = new Date();
        executeCommand("CREATE TABLE Stamp ("
                + "value BIGINT NOT NULL UNIQUE);");
        executeCommand("INSERT INTO Stamp VALUES("
                + stamp.getTime()+");");
    }
    
    public void updateStamp(){
        executeCommand("UPDATE Stamp SET value = "+stamp.getTime());
    }
    
    public void getStamp(){
        ResultSet r = executeCommand("SELECT * FROM Stamp;");
        if(r==null){
            System.out.println("ResultSet is null");
            if(errorcode == 1146){
                newDB = true;
                putStamp();
            }
            return;
        }
        try{
            r.next();
            System.out.println("Got time : "+r.getLong(1));
            stamp = new Date(r.getLong(1));
        }catch(SQLException e){
            System.out.println("SQL Exception : "+e.toString());
        }
    }
    
    public boolean process_old(){
        Date now = new Date();
        getStamp();
        boolean moreThanDay = Math.abs(now.getTime() - stamp.getTime()) > MILLIS_PER_DAY;
        
        if(moreThanDay){
            System.out.println("Old database\n");
            //Old database
            executeCommand("SELECT * INTO OUTFILE\"" + getDate(stamp)+".mysqldb" + "\" FROM "+"Items;");
            drop_table();
            setupTable();
            return true;
        }
        setupTable();
        return false;
    }
    
    public void setupTable(){
        executeCommand("CREATE DATABASE GW2;");
        executeCommand("CREATE TABLE Items ("
                + "id INT(64) NOT NULL UNIQUE, "
                + "name CHAR(200) NOT NULL, "
                + "description CHAR(200), "                         //optional
                + "chat_link CHAR(50) NOT NULL, "
                + "icon CHAR(200) NOT NULL, "
                + "type CHAR(200) NOT NULL, "
                + "rarity CHAR(200) NOT NULL, "
                + "level INT(64) NOT NULL, " 
                + "vendor_value integer NOT NULL, "
                + "default_skin integer);"
        );
        if(!newDB)updateStamp();
    }
    
    public int initdbconn(){
        String url = "jdbc:mysql://localhost:3306";
        String user = "GW2TP";
        String password = "XXXX";
        
        String query = "";
        
        try {
            con = DriverManager.getConnection(url, user, password);
            st = con.createStatement();
        }catch(SQLException e){
            System.out.println("SQLException : "+e.getMessage());
            return -1;
        };
        executeCommand("USE GW2;");
        return 0;
    }
    
    public boolean drop_table(){
        if(executeCommand("DROP TABLE Items;") == null)return false;
        return true;
    }
    
    public ResultSet executeCommand(String s) {
        try{
            boolean out = false;
            st = con.createStatement();
            out = st.execute(s);
            result = st.getResultSet();
        }catch (SQLException e){
            System.out.println("SQLException : "+e.getMessage() +" "+e.getErrorCode());
            errorcode = e.getErrorCode();
        }
        return result;
    }
    
    public boolean addItem(Item item){
            System.out.println("Inserting item : "+item.name+" id : "+item.id);
            if(executeCommand("INSERT into Items VALUES("
                + "\""+item.id+"\", "
                + "\""+item.name+"\", "
                + "\""+item.description+"\", "
                + "\""+item.chat_link+"\", "
                + "\""+item.icon+"\", "
                + "\""+item.type+"\", "
                + "\""+item.rarity+"\", "
                + "\""+item.level+"\", "
                + "\""+item.vendor_value+"\", "
                + "\""+item.default_skin+"\");")==null)return false;
            else return true;
    }
}
