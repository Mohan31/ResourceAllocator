package Services;
import java.util.*;
import Model.*;
import Exceptions.*;

public class ResourceAllocator {
     
    /*
            creating a min_heap which sorts the servers across regions by cost per cpu/hour in
            ascending order, so we will get the most cheapest cpu first which will give
            
            1. the customer more cpu if the constraint is on the "price" and "hours", as we are picking
               the cheapest first. 
            2. the customer cheapest price if the constraint is on the "cpu" and "hours"  
    */
    class ServerComparator implements Comparator<Server>{

        @Override
        public int compare(Server server_a, Server server_b){

            double cost_per_cpu_a = server_a.get_server_cost()/server_a.get_number_of_cpu();
            double cost_per_cpu_b = server_b.get_server_cost()/server_b.get_number_of_cpu();
            if(cost_per_cpu_a < cost_per_cpu_b) return -1;
            if(cost_per_cpu_a > cost_per_cpu_b) return 1;
            return 0;

        }    
    }
    /*
        region_map - contains the existing regions.
        min_cost_queue - contains the servers sorted by cost per cpu/hour.
    */
    private HashMap<String, Region> region_map; 
    private PriorityQueue<Server> min_cost_queue;
    
    
    ResourceAllocator(){

        region_map = new HashMap<>();
        min_cost_queue = new PriorityQueue<>(new ServerComparator());
    }
    /*
       **********************************************************************************************************
        Adding resources/servers in the given region.
        Here, we are using the hash map "region_map" to find if
        the region is already existing, if yes we just add the servers to the region
        else we create a new region for the given region name and add that to the list of regions
       **********************************************************************************************************       
    */
    void add_resource(String region_name, Server server){

        if(!region_map.containsKey(region_name)){

            Region region = new Region(region_name);
            region_map.put(region_name, region);
        }

        region_map.get(region_name).add_server(server);
        min_cost_queue.add(server);
        
    }
    void compute_result(Server server, HashMap<String, ResultPair> result, double cost){
        
        String region_key = server.get_region();
        ResultPair new_result;
        if(result.containsKey(region_key)){

            new_result = result.get(region_key);
            
        }
        else{

            new_result = new ResultPair();
            result.put(region_key, new_result);
        }

        new_result.total_cost += cost;

        String server_type = server.get_server_type();
        if(new_result.server_map.containsKey(server_type)){

            new_result.server_map.put(server_type, 
                        new_result.server_map.get(server_type) + 1);

        }
        else{

            new_result.server_map.put(server_type, 1);
        }

    }
    /*
       **********************************************************************************************************
        using TreeMap to sort the result by cost, treemap by default sorts the
        input by key in ascending order
        TreeMap<Double, List<HashMap<String,      ResultPair>>>  arguments is used as given below
        TreeMap<Cost,   List<HashMap<region_name, ResultPair>>>

        Maintaining the List<HashMap<region_name, ResultPair>> inside TreeMap because TreeMap does not allow
        duplicate key, but we may get the same cost for multiple regions, so we are storing the
        values as List, we check if the cost is already existing in the Map, if yes we just append the new result
        else we create a new List and add that to the Map.
       **********************************************************************************************************
    */
    TreeMap<Double, List<HashMap<String, ResultPair>>> sort_result_by_cost(HashMap<String, ResultPair> result){
        
        TreeMap<Double, List<HashMap<String, ResultPair>>> sorted_result = new TreeMap<>();
            
        for(Map.Entry<String, ResultPair> result_entry : result.entrySet()){

            HashMap<String, ResultPair> current_result = new HashMap<>();
            current_result.put(result_entry.getKey(), result_entry.getValue());

            // Condition to check for duplicate values.
            if(sorted_result.containsKey(result_entry.getValue().total_cost))
                sorted_result.get(result_entry.getValue().total_cost).add(current_result);      
            else{

                List<HashMap<String, ResultPair>> result_list = new LinkedList<>();
                result_list.add(current_result);
                sorted_result.put(result_entry.getValue().total_cost, result_list);
            }
        }

        return sorted_result;
    }
    TreeMap<Double,List<HashMap<String, ResultPair>>> GetCPU( int hours_requested, int cpu_requested, double price, Boolean is_price_constraint, Boolean is_cpu_constraint){
        
        int cpu_added = 0;
        HashMap<String, ResultPair> result =  new HashMap<>();
        PriorityQueue<Server> current_queue_of_servers = new PriorityQueue<>(new ServerComparator());
        /*
        ******************************************************************************************************
        case 1 : The user request minimum of N cpu's for H hours
                 
                 Since the price is not a constraint, we can make the price as some max value 
                 and the resouce allocator should give the requested number of cpu's for
                 requested hours in minimum price.

                 So maintaining the @condition - "cpu_added < cpu_requested ", will add the servers
                 until the requested number of cpu's added.  
                
                Note: if resource allocator not able to provide the required cpu's for required
                       hours, then the customer request fails, will throw the RequestNotPossible exception.

        ******************************************************************************************************
        case 2: The user request max number of cpu's for H hours for the given price

                Since the price is a constraint, while adding each server we need to check  
                if we can provide with the customer requested price.

                So maintaining the @condition cost_for_req_hours <= price

                Note : Here we will maintain the cpu_requested as some max_value. So we need to add
                as many possible cpu's for the given price
        */   
        int number_of_servers = this.min_cost_queue.size();
        while(number_of_servers-- > 0 && cpu_added < cpu_requested){

            //getting the cheapest cpu on the queue.
            Server server = this.min_cost_queue.poll();
            if(server.get_hours_remaining() >= hours_requested
                && cpu_added < cpu_requested){
                
                double cost_for_req_hours = server.get_server_cost() * hours_requested;
                if(cost_for_req_hours <= price){
                    
                    cpu_added += server.get_number_of_cpu();
                    server.update_server(hours_requested);
                    price -= cost_for_req_hours; 
                    /*
                        creating the result dictionary of the format,
                        result - {region_name, {total_cost, 
                                                [server_type : number of servers]
                                                }
                                 }
                    */     
                    compute_result(server, result, cost_for_req_hours);
                }
            }
            //adding the server back to queue with reduced hours, so it will be reused.
            if(server.get_hours_remaining() > 0)
            current_queue_of_servers.add(server);
            
            else if(server.get_hours_remaining() == 0)
            server.update_status(ServerStatus.Occupied);
        
        }
        //updating the priority queue after assigning some servers to the customer.
        this.min_cost_queue = current_queue_of_servers;
        // when both the price and number of cpu is a constraint
        if(is_cpu_constraint && is_price_constraint &&
            (cpu_added >= cpu_requested))
             return sort_result_by_cost(result);

        // when the number of cpu is a contraint
        if(is_cpu_constraint && cpu_added >= cpu_requested)
        return sort_result_by_cost(result);

        // when the price is a constraint
        if(is_price_constraint && !result.isEmpty())
        return sort_result_by_cost(result);
        
        //if the customer request cannot be satisfied for the given constraint
        throw new RequestNotPossible("Request cannot be satisfied");
               
    }
    
}
