import java.util.ArrayList;

class Item implements Comparable<Item>{

        //Item ID
        long id, level, vendor_value;
        Long default_skin;
        
        String chat_link, name, icon, description, type, rarity;
        
        ArrayList<String> flags, game_types, restrictions;
        
        //Listings at Trading Post
        ArrayList<Long> b_listings, b_unit_price, b_quantity;
        ArrayList<Long> s_listings, s_unit_price, s_quantity;

        //Profit on resell
        double profit;

        Item(){
            b_listings = new ArrayList<>();
            b_unit_price = new ArrayList<>();
            b_quantity = new ArrayList<>();

            s_listings = new ArrayList<>();
            s_unit_price = new ArrayList<>();
            s_quantity = new ArrayList<>();
            
            flags = new ArrayList<>();
            game_types = new ArrayList<>();
            restrictions = new ArrayList<>();
            
            profit = 0;
            default_skin = null;
        }

        //Calculate Profit
        /*
            Check when there is no listing under selling or buying you might get null there
        */
        double calcProfit(){
            if(name == null || s_unit_price.isEmpty() || b_unit_price.isEmpty())return 0;
            double diff = s_unit_price.get(0) - b_unit_price.get(0);
            double tax = s_unit_price.get(0)*0.15;
            return profit = diff-tax;
        }
        
        //Comapare Items based to their price
        @Override
        public int compareTo(Item o) {
            //if(o == null)return 1; 
            if(this.profit < o.profit)return 1;
            else if(this.profit > o.profit)return -1;
            return 0;
        }
        
        public String toString(){
            StringBuilder sb = new StringBuilder();
            sb.append("ID: ").append(id).append(" Name: ").append(name).append(" Level: ").append(level).append(" Rarity: ").append(rarity);
            return sb.toString();
        }
}

class Price{
        //Gold Silver Bronze Coins
        int g, s, b;
        
        Price(double p){
            //System.out.println("p = "+p+" g = p/10000 = "+(p/10000)+" p = p % 10000 = "+(p = p%10000)+""
            //        + "s = p/100 = "+(p/100)+" p = p % 100 = "+(p%100)+" b = p = "+b);
            g = (int)p/10000;
            p = p%10000;
            s = (int)p/100;
            p = p%100; 
            b = (int)p;
        }
        
        public String toString(){
            return String.format("%dg %ds %db",g,s,b);
        }
        
        public static Price getPrice(double p){
            return new Price(p);
        }
}