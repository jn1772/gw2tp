import java.util.ArrayList;

/*
TODO: 
    1. Add DB Support to save and update at set frequency.
    2. Add more info to Items.
    3. Remember last recommendations.
    4. Tell profit based on purchasing power of user. Only those items that cost
       less than the coins the user has
    5. Exception Handling (hoping to see basic functinality first)
    6. Simple Interactive CMDLine interface
    7. Multiple connections for InfoGet for faster fetching
*/

public class GW2TP {
    
    
    public static void main(String[] args) throws Exception{
        //Connect to DB
        DB db = new DB();
        db.initdbconn();
        boolean was_old = db.process_old();

        //Connects to GW2 servers and gets required Information
        InfoGet info = new InfoGet();
        
        //List of all item IDS returned by server
        ArrayList<Long> itemIds = info.getItemsIds();
        
        //Max items from TP (only for testing)
        int max = 200;//itemIds.size();
        
        //Struct Items
        Item[] items = info.initItems(itemIds);
        
        if(was_old || db.newDB){
            //Get Info about items such as name description type etc
            info.getItemsInfo(items, itemIds, max, db);

            //Get all item's prices at TP listing
            info.getItemsPrices(items, itemIds, max);
        }else{
            info.getInfoFromDB(items, db);
        }
        
        //Calculator
        Genie genie = new Genie();
        
        //Calculate profits
        genie.calcProfits(items);

        //Sort items by max profit on resell
        Item[] sorted = genie.sortItemsByMaxProfit(items);
        
        //Displya the top #arg2 items for profit
        genie.displayTopItems(sorted, max, info);

    }
}
