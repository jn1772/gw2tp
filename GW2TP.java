import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/*
TODO: 
    1. Add more info to Items.
    2. Multiple connections in InfoGet for faster fetching
*/

public class GW2TP {
    
    static InfoGet info;
    static ArrayList<Long> itemIds;
    static Item[] items;
    static Genie genie;
    static DB db;
    static int max;
    static boolean was_old, exists;
    
    public static void launch_user_interface(){
        System.out.println("*** GW2 TP Helper ***");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
        while(true){
            System.out.println("What would you like to do: ");
            System.out.println("1. Get item purchase recommendations.");
            System.out.println("2. Update Item database.");
            System.out.print("3. Exit.\n\n:");
            
            try{
                int response = Integer.parseInt(br.readLine());
                int user_gold = 0;

                switch(response){    
                    case 1:
                        //check and update db if old/doesn't exist
                        if(!(exists = db.check_exists())){
                            System.out.println("DB doesn't exist. Creating a new one...");
                            db.startFresh();
                        }else if(was_old = db.check_old()){
                                System.out.println("Local database is older than 24 hours...");
                                db.saveOldDB();
                                db.startFresh();
                                System.out.println("Fetching new data...");
                        }

                        if(exists && !was_old){
                            info.getInfoFromDB(items, db);
                        }else{
                            //Get Info about items such as name description type etc
                            info.getItemsInfo(items, itemIds, max, db);

                            //Get all item's prices at TP
                            info.getItemsPrices(items, itemIds, max, db);

                            //Get all item's prices at TP listing. Not using right now.
                            //info.getItemsListings(items, itemIds, max);
                        }

                        System.out.println("How much gold do you have?");
                        user_gold = Integer.parseInt(br.readLine());

                        genie.calcProfits(items);
                        Item[] sorted = genie.sortItemsByMaxProfit(items);
                        genie.displayTopItems(sorted, max, user_gold * 100 * 100);
                        break;
                    case 2:
                        db.startFresh();

                        itemIds = info.getItemsIds();
                        items = info.initItems(itemIds);
                        info.getItemsInfo(items, itemIds, max, db);
                        info.getItemsPrices(items, itemIds, max, db);
                        break;
                    case 3:
                        System.out.println("Bye!");
                        System.exit(0);
                        break;
                }
            }catch(IOException e){
                System.out.println("IOException caught! "+e.toString());
            }
            catch(Exception e){
                System.out.println("Exception caught! "+e.toString());
            }
        }
    }
    
    public static void main(String[] args) throws Exception{
        //Connect to DB
        db = new DB();
        db.initdbconn();
        
        //Connects to GW2 servers and gets required Information
        info = new InfoGet();

        //List of all item IDS returned by server
        itemIds = info.getItemsIds();

        //Max items from TP (only for testing)
        max = itemIds.size();

        //Struct Items
        items = info.initItems(itemIds);
                    
        //Calculator
        genie = new Genie();
        
        launch_user_interface();
    }
}
