
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.Date;

class Result{
    
    ResultSet result;
    int error;
    Result(){
        result = null;
        error = 0;
    }
}

class err{
    static final int TABLE_NOT_EXIST = 1146;
}

public class DB {
    
    Connection con;
    Statement st;
    ResultSet result;
    err err;
    
    Date stamp;
    
    long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;
    
    String add_item = "INSERT into Items VALUES(?,?,?,?,?,?,?,?,?,?)";
    PreparedStatement ps_add_item;
    
    String add_price = "INSERT into Prices VALUES(?,?,?,?,?)";
    PreparedStatement ps_add_price;
    
    boolean newDB;
    
    public int initdbconn(){
        String url = "jdbc:mysql://localhost:3306?rewriteBatchedStatements=true";
        String user = "GW2TP";
        String password = "XXXX";
        
        String query = "";
        
        try {
            con = DriverManager.getConnection(url, user, password);
            st = con.createStatement();
        }catch(SQLException e){
            System.err.println("SQLException : "+e.getMessage());
            return -1;
        };
        executeCommand("CREATE DATABASE GW2;");
        executeCommand("USE GW2;");
        return 0;
    }
    
    public void startFresh(){
        executeCommand("DROP DATABASE GW2;");
        executeCommand("CREATE DATABASE GW2;");
        executeCommand("USE GW2;");
        setupTables();
        putStamp();
    }
    
    public void setupTables(){
        executeCommand("CREATE TABLE Items ("
                + "id INT(64) NOT NULL UNIQUE, "
                + "name CHAR(200) NOT NULL, "
                + "description TEXT, "                         //optional
                + "chat_link CHAR(50) NOT NULL, "
                + "icon CHAR(200) ,"
                + "type CHAR(200) NOT NULL, "
                + "rarity CHAR(200) NOT NULL, "
                + "level INT(64) NOT NULL, " 
                + "vendor_value integer NOT NULL, "
                + "default_skin integer);"
        );
        executeCommand("CREATE TABLE Prices ("
                + "id INT(64) NOT NULL UNIQUE, "
                + "buy_price INT, "
                + "buy_quantity INT, "
                + "sell_price INT, "
                + "sell_quantity INT );");
    }
        
    public String getDate(Date d){
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        return sdf.format(d);
    }
    
    public void putStamp(){
        System.out.println("Putting new stamp...");
        stamp = new Date();
        executeCommand("CREATE TABLE Stamp ("
                + "value BIGINT NOT NULL UNIQUE);");
        executeCommand("INSERT INTO Stamp VALUES("
                + stamp.getTime()+");");
    }
    
    public void getStamp(){
        Result r = executeCommand("SELECT * FROM Stamp;");
        try{
            r.result.next();
            System.err.println("Last DB update at : "+new Date(r.result.getLong(1)).toString());
            stamp = new Date(r.result.getLong(1));
        }catch(SQLException e){
            System.err.println("SQL Exception : "+e.toString());
        }
    }
    
    public boolean check_exists(){
        Result r = executeCommand("SELECT * FROM Stamp;");
        return (r.error != err.TABLE_NOT_EXIST);
    }
    
    public boolean check_old(){
        
        Date now = new Date();
        newDB = false;
        getStamp();
        boolean moreThanDay = Math.abs(now.getTime() - stamp.getTime()) > MILLIS_PER_DAY;
        
        return moreThanDay;
    }
    
    public void saveOldDB(){
        executeCommand("SELECT * INTO OUTFILE\"" + getDate(stamp)+".mysqldb" + "\" FROM "+"Items;");
    }
        
    public Result executeCommand(String s) {
        Result res = new Result();
        try{
            st = con.createStatement();
            st.execute(s);
            result = st.getResultSet();
            res.result = result;
        }catch (SQLException e){
            System.err.println("SQLException : "+e.getMessage() +" "+e.getErrorCode());
            res.error = e.getErrorCode();
        }
        return res;
    }
    
    public int[] pushItemsBatch(){
        
        int[] ret = null;
        if(ps_add_item==null){
            System.err.println("PreparedStatement ps is null");
            return ret;
        }
        
        try{
            ret = ps_add_item.executeBatch();
            ps_add_item.clearBatch();
        }catch(SQLException e){
            System.err.println("Here  SQLException : "+e.toString()+" error code : "+e.getErrorCode());
            return ret;
        }
        return ret;
    }
    
    public boolean pushPricesBatch(){
        
        if(ps_add_price==null){
            System.err.println("PreparedStatement ps is null");
            return false;
        }
        
        try{
            ps_add_price.executeBatch();
            ps_add_price.clearBatch();
        }catch(SQLException e){
            System.err.println("SQLException : "+e.toString()+" error code : "+e.getErrorCode());
            return false;
        }
        return true;
    }
    
    public boolean addItemBatch(Item item){
        try{
            if(ps_add_item == null)ps_add_item = con.prepareStatement(add_item);
    
            ps_add_item.setLong(1, item.id);
            ps_add_item.setString(2, item.name);
            ps_add_item.setString(3, item.description);
            ps_add_item.setString(4, item.chat_link);
            ps_add_item.setString(5, item.icon);
            ps_add_item.setString(6, item.type);
            ps_add_item.setString(7, item.rarity);
            ps_add_item.setInt(8, item.level);
            ps_add_item.setInt(9, item.vendor_value);

            if(item.default_skin != null)
                ps_add_item.setInt(10, item.default_skin);
            else    
                ps_add_item.setNull(10, Types.INTEGER);
            

            //ps_add_item.execute();
            ps_add_item.addBatch();
        }catch(SQLException e){
            System.err.println("Exception in addItem : "+e.toString()+" errorcode : "+e.getErrorCode()+" "+item);
            return false;
        }
        return true;
        //System.out.println("Inserting item : "+item.name+" id : "+item.id);
    }
    
    public boolean addItemPriceBatch(Item item){
        
        try{
            if(ps_add_price == null)ps_add_price = con.prepareStatement(add_price);
    
            ps_add_price.setLong(1, item.id);
            
            if(item.b_upr != null)
                ps_add_price.setInt(2, item.b_upr);
            else
                ps_add_price.setNull(2, Types.INTEGER);
            
            if(item.b_num != null)
                ps_add_price.setInt(3, item.b_num);
            else
                ps_add_price.setNull(3, Types.INTEGER);
            
            if(item.s_upr != null)
                ps_add_price.setInt(4, item.s_upr);
            else
                ps_add_price.setNull(4, Types.INTEGER);
            
            if(item.s_num != null)
                ps_add_price.setInt(5, item.s_num);
            else
                ps_add_price.setNull(5, Types.INTEGER);
            
            ps_add_price.addBatch();
        }catch(SQLException e){
            System.err.println("Exception in addItemPrice : "+e.toString()+" errorcode : "+e.getErrorCode());
            return false;
        }
        return true;
    }
}