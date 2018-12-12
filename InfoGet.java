import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
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
        Item[] items = new Item[ids.size()];
        for(int i=0;i<ids.size();++i){
            items[i] = new Item();
            items[i].id = ids.get(i);
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
        
        if(item.name.matches("null"))System.out.println("Name is null for desc : "+item.description+" type : "+item.type);
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

    /*
    Get item info for 'max' number of Items in items[]. 200 at a time.
    */
    ArrayList<Long> getItemsInfo(Item items[], int max) throws Exception {
        int processed = 0;
        
        while(processed < max){
            StringBuilder sbb = new StringBuilder();
            int j;
            for (j = processed; j < Math.min(200 + processed, max); ++j) { 
                sbb.append(items[j].id).append(",");
            }
            sbb.deleteCharAt(sbb.length()-1);
            processed = j;
            
            if(debug)System.out.println("getItemsInfo processed : "+processed+"/"+items.length);

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
                String name = (String)object.get("name");
                String type = (String)object.get("type");
                int id = (int)(long)object.get("id");
                String description = (String)object.get("description");
                
                items[id].name = name;
                items[id].type = type;
                items[id].description = description;
                if(debug);
                    System.out.println("Curr : "+id+" Name: "+name);
            }
        }
        return null;
    }
    
    /*
    Get Item Prices at TP for 'max' number of items in items[]. 200 at a time.
    */
    ArrayList<Long> getItemsPrices(Item []items, int max) throws Exception {

        int processed = 0;
        while(processed < max){
            StringBuilder sbb = new StringBuilder();
            int j;
            for (j = processed; j < Math.min(200 + processed, max); ++j) {
                sbb.append(items[j].id).append(",");
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

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    response.getEntity().getContent()));

            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                //if(respDebug)System.out.println(line);
                sb.append(line);
            }
            System.out.println("itemsPrices processed : "+processed+"/"+items.length);
            JSONParser parser = new JSONParser();
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
        }
        return null;
    }
}