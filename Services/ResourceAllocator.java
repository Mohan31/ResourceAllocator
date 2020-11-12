package Services;
import java.util.*;
import Model.*;
import Exceptions.*;

public class ResourceAllocator {

    /*
        region_map - contains the existing regions.
        min_cost_queue - contains the servers sorted by cost per cpu/hour.
    */
    private HashMap<String, Region> region_map; 
    private PriorityQueue<Server> min_cost_queue;
    
    
    ResourceAllocator(){

        region_map = new HashMap<>();
        /*
            creating a min_heap which sorts the servers across regions by cost per cpu/hour in
            ascending order, so we will get the most cheapest cpu first which will give
            
            1. the customer more cpu if the constraint is on the "price" and "hours", as we are picking
               the cheapest first. 
            2. the customer cheapest price if the constraint is on the "cpu" and "hours"  
        */
        min_cost_queue = new PriorityQueue<>(new Comparator<Server>(){

            public int compare(Server server_a, Server server_b){

                float cost_per_cpu_a = server_a.get_server_cost()/server_a.get_number_of_cpu();
                float cost_per_cpu_b = server_b.get_server_cost()/server_b.get_number_of_cpu();
                if(cost_per_cpu_a < cost_per_cpu_b) return -1;
                if(cost_per_cpu_a > cost_per_cpu_b) return 1;
                return 0;

            }
            
        });
    }

    /*
        Adding resources/servers in the given region.
        Here, we are using the hash map "region_map" to find if
        the region is already existing, if yes we just add the servers to the region
        else we create a new region for the given region name and add that to the list of regions
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

    HashMap<String, ResultPair> GetCPU( int hours_requested, int cpu_requested, float price, Boolean is_price_constraint, Boolean is_cpu_constraint){
        
        int cpu_added = 0;
        HashMap<String, ResultPair> result =  new HashMap<>();
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
            //  adding the server back to queue with reduced hours, so it will be reused.
            if(server.get_hours_remaining() > 0)
            this.min_cost_queue.add(server);
            
            else if(server.get_hours_remaining() == 0)
            server.update_status(ServerStatus.Occupied);
        
        }
        
        
        // when both the price and number of cpu is a constraint
        if(is_cpu_constraint && is_price_constraint &&
            (cpu_added >= cpu_requested))
             return result;

        //when the number of cpu is a contraint
        if(is_cpu_constraint && cpu_added >= cpu_requested)
        return result;

        // when the price is a constraint
        if(is_price_constraint && !result.isEmpty())
        return result;
        
        //if the customer request cannot be satisfied for the given constraint
        throw new RequestNotPossible("Request cannot be satisfied");
               
    }
    
}