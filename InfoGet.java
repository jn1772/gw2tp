import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/*
This class basically requests info from the GW2 servers and populates the local
structures.
*/
class InfoGet{
    static boolean debug = false;
    static boolean respDebug = true;
    
    InfoGet(boolean d){
        debug = d;
    }
    
    InfoGet(){
        debug = false;
    }
    
    ArrayList<Long> getItemsIds() throws Exception {

        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost("api.guildwars2.com")
                .setPort(443)
                .setPath("/v2/items")
                //.addParameter("ids", "1,2") 
                .build();
        HttpGet httpGet = new HttpGet(uri);
        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = client.execute(httpGet);

        BufferedReader rd = new BufferedReader(new InputStreamReader(
                response.getEntity().getContent()));
       
        String line;
        StringBuilder sb = new StringBuilder();

        while ((line = rd.readLine()) != null) {
            //if(respDebug)System.out.println(line); 
            sb.append(line);
        }

        JSONParser parser = new JSONParser();
        Object obj = parser.parse(sb.toString());
        JSONArray itemIds = (JSONArray) obj; 
        ArrayList<Long> ret = new ArrayList<>();
        Iterator it = itemIds.iterator();
        while(it.hasNext())ret.add((long)it.next());
        return ret;
    }
    
    Item[] initItems(ArrayList<Long> ids){
        int max = 0;
        for(int i=0;i<ids.size();++i){
            max = Math.max((int)(long)ids.get(i), max);
        }
        Item[] items = new Item[max+1];
        for(int i=0;i<ids.size();++i){
            items[(int)(long)ids.get(i)] = new Item();
            items[(int)(long)ids.get(i)].id = ids.get(i);
        }
        return items;
    }
    
    /*
    Get item info for a single item
    */
    void getItemInfo(Item item) throws Exception{
        URI uri = new URIBuilder()
                    .setScheme("https")
                    .setHost("api.guildwars2.com")
                    .setPort(443)
                    .setPath("/v2/items/"+item.id)
                    .build();
        HttpGet httpGet = new HttpGet(uri);
        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = client.execute(httpGet);

        // Get the response
        BufferedReader rd = new BufferedReader(new InputStreamReader(
                response.getEntity().getContent()));

        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = rd.readLine()) != null) {
            //if(respDebug)System.out.println(line);
            sb.append(line);
        }

        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject)parser.parse(sb.toString());
        item.name = (String)obj.get("name");
        item.type = (String)obj.get("type");
        item.description = (String)obj.get("description");
        
        if(debug){
            System.out.println("\nName: "+item.name+
                           "\nType: "+item.type+
                           "\nDesc: "+item.description);
            System.out.println("Buy Info: ");
            for(int i=0;i<item.b_listings.size();++i){
                System.out.println("#"+item.b_listings.get(i)+" Price: "+
                        item.b_unit_price.get(i)+" Available: "+
                        item.b_quantity.get(i));
            }        

            System.out.println("Sell Info: ");
            for(int i=0;i<item.b_listings.size();++i){
                System.out.println("#"+item.s_listings.get(i)+" Price: "+
                        item.s_unit_price.get(i)+" Available: "+
                        item.s_quantity.get(i));
            }
        }
    }

    static int tp=0;
    /*
    Get item info for 'max' number of Items in items[]. 200 at a time.
    */
    void getItemsInfo(Item items[], ArrayList<Long> ids, int maxx, DB db) throws Exception {
        int processed = 0;
        int max = maxx;//ids.size();
        
        while(processed < max){
            StringBuilder sbb = new StringBuilder();
            int j;
            for (j = processed; j < Math.min(200 + processed, max); ++j) { 
                sbb.append(items[(int)(long)ids.get(j)].id).append(",");
            }
            sbb.deleteCharAt(sbb.length()-1);
            processed = j;
            
            if(debug);
            System.out.println("getItemsInfo processed : "+processed+"/"+max);

            URI uri = new URIBuilder()
                    .setScheme("https")
                    .setHost("api.guildwars2.com")
                    .setPort(443)
                    .setPath("/v2/items")
                    .addParameter("ids", sbb.toString())
                    .build();
            HttpGet httpGet = new HttpGet(uri);
            HttpClient client = HttpClientBuilder.create().build();
            HttpResponse response = client.execute(httpGet);

            // Get the response 
            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()));

            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                //if(respDebug)System.out.println(line);
                sb.append(line);
            }
            
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(sb.toString());
            JSONArray itemIds = (JSONArray) obj;
            Iterator<Object> iterator = itemIds.iterator();

            while (iterator.hasNext()) {
                
                JSONObject object = (JSONObject) iterator.next();
                int id = (int)(long)object.get("id");
                String chat_link = (String)object.get("chat_link");
                String name = (String)object.get("name");
                String icon = (String)object.get("icon");
                String description = (String)object.get("description");
                String type = (String)object.get("type");
                String rarity = (String)object.get("rarity");
                int level = (int)(long)object.get("level");
                int vendor_value = (int)(long)object.get("vendor_value");
                Long default_skin = (Long)object.get("default_skin");
                
                items[id].chat_link = chat_link;
                items[id].name = name;
                items[id].icon = icon;
                items[id].description = description;
                items[id].type = type;
                items[id].rarity = rarity;
                items[id].level = level;
                items[id].vendor_value = vendor_value;
                items[id].default_skin = default_skin;
                
                JSONArray flags = (JSONArray)object.get("flags");
                Iterator<Object> flagsIterator = flags.iterator();
                while(flagsIterator.hasNext()){
                    String ss = (String) flagsIterator.next();
                    items[id].flags.add(ss);
                    System.out.println("Flag = "+ss);
                }
                
                JSONArray gameTypes = (JSONArray)object.get("game_types");
                Iterator<Object> gameTypesIterator = gameTypes.iterator();
                while(gameTypesIterator.hasNext()){
                    String ss = (String) gameTypesIterator.next();
                    items[id].game_types.add(ss);
                    System.out.println("Game types : "+ss);
                }
                
                JSONArray restrictions = (JSONArray)object.get("restrictions");
                Iterator<Object> restrictionsIterator = restrictions.iterator();
                while(restrictionsIterator.hasNext()){
                    String ss = (String) restrictionsIterator.next();
                    items[id].restrictions.add(ss);
                    System.out.println("Restrictions : "+ss);
                }
                
                JSONObject details = (JSONObject)object.get("details");
                String d_type, d_weight_class, d_damage_type;
                Long d_defense, d_min_power, d_max_power;
                
                switch(type){
                    case "Armor":
                        d_type = (String)details.get("type");
                        d_weight_class = (String)details.get("weight_class");
                        d_defense = (Long)details.get("defense");
                        //leave stat_choices, infusion slots, infix_upgrade, suffix_item_id, secondary_suffix_item_id for now
                        System.out.println("Armor type : "+d_type+" weight_class : "+d_weight_class+" defense : "+d_defense);
                        break;
                    case "Weapon":
                        d_type = (String)details.get("type");
                        d_damage_type = (String)details.get("damage_type");
                        d_min_power = (Long)details.get("min_power");
                        d_max_power = (Long)details.get("max_power");
                        d_defense = (Long)details.get("defense");
                        System.out.println("Weapon type : "+d_type+" damage_type : "+d_damage_type+" min_power : "+d_min_power+" max_power : "+d_max_power+" defense : "+d_defense);
                        break;
                    default:
                        break;
                }
                db.addItem(items[id]);
            }
        }
    }
    
    /*
    Get Item Prices at TP for 'max' number of items in items[]. 200 at a time.
    */
    ArrayList<Long> getItemsPrices(Item []items, ArrayList<Long> ids, int maxx) throws Exception {
        int max = maxx;//ids.size();
        int processed = 0;
        while(processed < max){
            StringBuilder sbb = new StringBuilder();
            int j;
            for (j = processed; j < Math.min(200 + processed, max); ++j) {
                sbb.append(items[(int)(long)ids.get(j)].id).append(",");
            }
            processed = j;
            sbb.deleteCharAt(sbb.length()-1);
            URI uri = new URIBuilder()
                    .setScheme("https")
                    .setHost("api.guildwars2.com")
                    .setPort(443)
                    .setPath("/v2/commerce/listings")
                    .addParameter("ids", sbb.toString())
                    .build();
            HttpGet httpGet = new HttpGet(uri);
            HttpClient client = HttpClientBuilder.create().build();
            HttpResponse response = client.execute(httpGet);
            //if((tp+=200) > 3200)System.out.println("tp = "+tp+" "+sbb.toString());
            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()));

            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                //if(respDebug)System.out.println(line);
                sb.append(line);
            }
            System.out.println("itemsPrices processed : "+processed+"/"+max);
            JSONParser parser = new JSONParser();
            try{
                Object obj = parser.parse(sb.toString());
                JSONArray itemIds = (JSONArray) obj;
                Iterator<Object> iterator = itemIds.iterator();

                int curr=0;
                while (iterator.hasNext()) {

                    JSONObject object = (JSONObject) iterator.next();
                    JSONArray buys = (JSONArray) object.get("buys");
                    JSONArray sells = (JSONArray) object.get("sells");
                    int id = (int)((long)object.get("id"));

                    Iterator<Object> itt = buys.iterator();
                    while(itt.hasNext()){
                        JSONObject listing = (JSONObject)itt.next();
                        long listings = (long)listing.get("listings");
                        long price = (long)listing.get("unit_price");
                        long quantity = (long)listing.get("quantity");
                        if(debug)System.out.println("curr: "+curr+
                                " Buy : [listings, price, quantity]: "+
                                listings+", "+price+", "+quantity);

                        items[id].b_listings.add(listings);
                        items[id].b_unit_price.add(price);
                        items[id].b_quantity.add(quantity);
                    }

                    itt = sells.iterator();
                    while(itt.hasNext()){
                        JSONObject listing = (JSONObject)itt.next();
                        long listings = (long)listing.get("listings");
                        long price = (long)listing.get("unit_price");
                        long quantity = (long)listing.get("quantity");

                        if(debug)System.out.println("curr: "+curr+
                                " Sell : [listings, price, quantity]: "+
                                listings+", "+price+", "+quantity);
                        items[id].s_listings.add(listings);
                        items[id].s_unit_price.add(price);
                        items[id].s_quantity.add(quantity);
                    }
                    curr++;
                }
            }catch (ClassCastException e){
                System.out.println("!Exception (Class Cast). Continuing...");
                System.out.println("Item ids were : "+sbb);
            }
        }
        return null;
    }
    
    void getInfoFromDB(Item[] items, DB db){
        ResultSet results = db.executeCommand("select * from Items");
        
        if(results == null){
            System.out.println("SQL Command error");
            return;
        }
        try{
            while(results.next()){
                int id = results.getInt(1);
                items[id].name = results.getString(2);
                items[id].description = results.getString(3);
                items[id].chat_link = results.getString(4);
                items[id].icon = results.getString(5);
                items[id].type = results.getString(6);
                items[id].rarity = results.getString(7);
                items[id].level = results.getLong(8);
                items[id].vendor_value = results.getLong(9);
                items[id].default_skin = results.getLong(10);
                System.out.println(items[id]);
            }
        }catch (SQLException e){
            System.out.println("SQL Exception! : "+e.toString());
        }
    }
}