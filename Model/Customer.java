package Model;
import java.util.*;
/*
  --> Further enhancements, if required.
  Customer class helps us to keep track of the servers assigned to the customer and
  also to store the customer specific metadata which can be utilised for analytics and trends.
*/
public class Customer {
    
    private final String customer_id;
    private List<Server> servers;

    public Customer(String customer_id){
        
        this.customer_id = customer_id;
        servers = new ArrayList<>();
    }

    public String get_customer_id(){

        return this.customer_id;
    }

    void add_servers(Server server){

        servers.add(server);
    }

}
