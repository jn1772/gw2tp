import java.util.Arrays;
import java.util.Collections;

public class Genie {
    
    void calcProfits(Item[] items){
        for(int i=0;i<items.length;++i)if(items[i].name != null)items[i].calcProfit();
    }
    
    Item[] sortItemsByMaxProfit(Item[] items) throws Exception{
        Item[] curItems = new Item[items.length];
        System.arraycopy(items, 0, curItems, 0, items.length);
        for(int i=0;i<300;++i)System.out.println("Orig name : "+items[i].name);
        Arrays.sort(curItems);
        for(int i=0;i<300;++i)System.out.println("Sorted name : "+curItems[i].name);
        return curItems;
    }
    
    void displayTopItems(Item[] item, int top, InfoGet infoget) throws Exception{
        for(int i=0;i<top;++i){
            Item it = item[i];
            System.out.println("Getting profit for item : "+it.name +" desc : "+it.description+" type : "+it.type);
            it.calcProfit();
            if(it.profit == 0)continue;
            Price bp=null, sp=null;
            if(!it.b_unit_price.isEmpty())bp = Price.getPrice(it.b_unit_price.get(0));
            if(!it.s_unit_price.isEmpty())sp = Price.getPrice(it.s_unit_price.get(0));
            
            System.out.println("|||"+it.name+"|||\n"+
                    "|Price(b): "+it.b_unit_price.get(0)+"|\n"+
                    "|Price(s): "+it.s_unit_price.get(0)+"|\n"+
                    "|Profit(bronze): "+it.profit+"|\n"+
                    "|Buy at: "+bp+"|\n"+
                    "|Sell at: "+sp+"|\n"+
                    "|Profit: "+(Price.getPrice(it.profit))+"|\n\n\n");
        }
    }
}
