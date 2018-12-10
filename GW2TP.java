import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


class GW2TP {

    static class Item implements Comparable<Item>{
        String name; //name in game
        
        long id; //gw2 id
        int level; //rare, ascended
        String type;
        String description;
   
        ArrayList<Long> b_listings, b_unit_price, b_quantity;
        ArrayList<Long> s_listings, s_unit_price, s_quantity;
        
        double profit; //on resell
        
        Item(){
            b_listings = new ArrayList<>();
            b_unit_price = new ArrayList<>();
            b_quantity = new ArrayList<>();
            
            s_listings = new ArrayList<>();
            s_unit_price = new ArrayList<>();
            s_quantity = new ArrayList<>();
        }

        double calcProfit(){
            if(s_unit_price.isEmpty() || b_unit_price.isEmpty())return 0;
            double diff = s_unit_price.get(0) - b_unit_price.get(0);
            double tax = s_unit_price.get(0)*0.15;
            return profit = diff-tax;
        }
        @Override
        public int compareTo(Item o) {
            this.calcProfit(); 
            o.calcProfit();
            if(this.profit < o.profit)return -1;
            else if(this.profit > o.profit)return 1;
            return 0;
        }
    }
    
    static Item items[], curItems[];
    static int max = 0;
    /**      *
     * @param args the command line arguments      *
     */
    public static void main(String[] args) throws Exception {
        
        // TODO code application logic here 
        getItemsIdList();
        //max = items.length;
        max = 5000;
        getItemsPrices();
        getItemsInfo();
        sortItemsByMaxProfit();
        displayTopItems(curItems, 100);
    }

    static Item[] sortItemsByMaxProfit() throws Exception{
        if(curItems == null)curItems = new Item[max];
        System.arraycopy(items, 0, curItems, 0, max);
        Arrays.sort(curItems);
        return curItems;
    }
    
    static class Price{
        double g, s, b;
        Price(double p){
            
            g = p/10000; p=p%10000; 
            s = p/100; p = p%100; 
            b = p; 
        }
        public String toString(){
            return String.format("%.0fg %.0fs %.0fb",g,s,b);
        }
    }
    
    static Price getPrice(double p){
        return new Price(p);
    }
    
    static void displayTopItems(Item[] item, int top) throws Exception{
        for(int i=0;i<top;++i){
            Item it = item[i];
            it.calcProfit();
            Price bp, sp;
            if(it.b_unit_price.isEmpty())bp = null;
            else bp = getPrice(it.b_unit_price.get(0));
            if(it.s_unit_price.isEmpty())sp = null;
            else sp = getPrice(it.s_unit_price.get(0));
            
            System.out.println("|||"+it.name+"|||\n"+
                    "|Buy at: "+bp+"|\n"+
                    "|Sell at: "+sp+"|\n"+
                    "|Profit: "+getPrice(it.profit)+"|\n\n\n");
        }
    }
    //fills items with ids
    static ArrayList<Long> getItemsIdList() throws Exception {

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

        // Get the response 
        BufferedReader rd = new BufferedReader(new InputStreamReader(
                response.getEntity().getContent()));
       
        String line = "";
        StringBuilder sb = new StringBuilder();

        while ((line = rd.readLine()) != null) {
            //System.out.println(line); 
            sb.append(line);
        }

        JSONParser parser = new JSONParser();
        Object obj = parser.parse(sb.toString());
        JSONArray itemIds = (JSONArray) obj;
        Iterator<Long> iterator = itemIds.iterator();
        ArrayList<Long> ret = new ArrayList<>();
        ret.addAll(itemIds);
        
        items = new Item[itemIds.size()];
        //System.out.println("size : "+itemIds.size());
        for(int i=0;i<itemIds.size();++i){
            items[i] = new Item();
            items[i].id = (Long)itemIds.get(i);
       
        }
        return ret;
    }

    
    //Only for a single item
    static void getItemInfo(Item item) throws Exception{
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

        String line = "";
        StringBuilder sb = new StringBuilder();
        while ((line = rd.readLine()) != null) {
            //System.out.println(line);
            sb.append(line);
        }

        JSONParser parser = new JSONParser();
        JSONObject obj = (JSONObject)parser.parse(sb.toString());
        item.name = (String)obj.get("name");
        item.type = (String)obj.get("type");
        item.description = (String)obj.get("description");
        
        //System.out.println("\nName: "+item.name+
        //                   "\nType: "+item.type+
        //                   "\nDesc: "+item.description);
        
        
        ///System.out.println("Buy Info: ");
        for(int i=0;i<item.b_listings.size();++i){
            System.out.println("#"+item.b_listings.get(i)+" Price: "+item.b_unit_price.get(i)+" Available: "+item.b_quantity.get(i));
        }        
        
        //System.out.println("Sell Info: ");
        for(int i=0;i<item.b_listings.size();++i){
            System.out.println("#"+item.s_listings.get(i)+" Price: "+item.s_unit_price.get(i)+" Available: "+item.s_quantity.get(i));
        }
    }
    
    //Get item prices for all available items
    static ArrayList<Long> getItemsInfo() throws Exception {

        int processed = 0;
        //while (processed < ids.size()) {
          while(processed < max){
            StringBuilder sbb = new StringBuilder();
            int j;
            for (j = processed; j < Math.min(200 + processed, max); ++j) {
                //System.out.println("id : "+ids.get(j)); 
                sbb.append(items[j].id).append(",");
            }
            sbb.deleteCharAt(sbb.length()-1);
            processed = j; 
            System.out.println("itemInfo processed : "+processed+"/"+items.length);
            
            
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
            
            String line = "";
            StringBuilder sb = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                //System.out.println(line);
                sb.append(line);
            }
            
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(sb.toString());
            JSONArray itemIds = (JSONArray) obj;
            Iterator<Object> iterator = itemIds.iterator();
            ArrayList<Long> ret = new ArrayList<>();
            ret.addAll(itemIds);
            
            int curr=0;
            while (iterator.hasNext()) {
                JSONObject object = (JSONObject) iterator.next();
                String name = (String)object.get("name");
                String type = (String)object.get("type");
                
                String description = (String)object.get("description");
                
                items[curr].name = name;
                items[curr].type = type;
                items[curr].description = description;
                //System.out.println("Curr : "+curr+" Name: "+name);
                curr++;
            }
        }
        return null;
    }
    
    //Get item prices for all available items
    static ArrayList<Long> getItemsPrices() throws Exception {

        int processed = 0;
        //while (processed < ids.size()) {
          while(processed < max){
            StringBuilder sbb = new StringBuilder();
            int j;
            for (j = processed; j < Math.min(200 + processed, max); ++j) {
                //System.out.println("id : "+ids.get(j)); 
                sbb.append(items[j].id).append(",");
            }
            processed = j;
            System.out.println(sbb.toString());
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
            
            String line = "";
            StringBuilder sb = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                //System.out.println(line);
                sb.append(line);
            }
            System.out.println("itemsPrices processed : "+processed+"/"+items.length);
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(sb.toString());
            JSONArray itemIds = (JSONArray) obj;
            Iterator<Object> iterator = itemIds.iterator();
            ArrayList<Long> ret = new ArrayList<>();
            ret.addAll(itemIds);
            
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
                   // System.out.println("curr: "+curr+" Buy : [listings, price, quantity]: "+listings+", "+price+", "+quantity);
                   
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
                
                   // System.out.println("curr: "+curr+" Sell : [listings, price, quantity]: "+listings+", "+price+", "+quantity);
                    items[id].s_listings.add(listings);
                    items[id].s_unit_price.add(price);
                    items[id].s_quantity.add(quantity);
                
                }
            }
        }
        return null;
    }
}
