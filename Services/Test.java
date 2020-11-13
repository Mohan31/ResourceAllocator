package Services;
import Model.*;
import java.util.*;
import Exceptions.*;


public class Test {
    
    public static TreeMap<Double,List<HashMap<String, ResultPair>>> response;
    

    Test(){
        
        System.out.println("*******************Test Run***********************");
        response = new TreeMap<>();
        
    }
    public static void main(String[] args) {

        Test test = new Test();
        Server[] server = new Server[15];;
        
        ResourceAllocator resource_allocator = new ResourceAllocator();
       //@arg0 String type, @arg1 float cost, @arg2 int cpus, @arg3 int hours, @arg4 String region_name
            
        server[0] = new Server("large", 120, 1, 24, "us-east");
        server[1] = new Server("xlarge", 230, 2, 24, "us-east");
        server[2] = new Server("2xlarge", 450, 4, 24, "us-east");
        server[3] = new Server("4xlarge", 774, 8, 24, "us-east");
        server[4] = new Server("8xlarge", 1400, 16, 24, "us-east");
        server[5] = new Server("10xlarge", 2820, 32, 24, "us-east");

        server[6] = new Server("large", 140, 1, 24, "us-west");
        server[7] = new Server("2xlarge", 413, 4, 24, "us-west");
        server[8] = new Server("4xlarge", 890, 8, 24, "us-west");
        server[9] = new Server("8xlarge", 1300, 16, 24, "us-west");
        server[10] = new Server("10xlarge", 2970, 32, 24, "us-west");

        server[11] = new Server("large", 110, 1, 24, "asia");
        server[12] = new Server("xlarge", 200, 2, 24, "asia");
        server[13] = new Server("4xlarge", 670, 8, 24, "asia");
        server[14] = new Server("8xlarge", 1180, 16, 24, "asia");

        for(int i = 0; i < 15;i++)
            resource_allocator.add_resource(server[i].get_region(), server[i]);
        
        try {
            response = resource_allocator.GetCPU(12, 42, Integer.MAX_VALUE , false, true);            
        } catch (Exception e) {
            System.out.println(e.toString());
        }
            
        for(Map.Entry<Double, List<HashMap<String, ResultPair>>> response_entry : response.entrySet()){

            for(HashMap<String, ResultPair> result_list : response_entry.getValue())
            for(Map.Entry<String, ResultPair> result_pair_entry : result_list.entrySet()){
                
                System.out.println("Region : " + result_pair_entry.getKey());
                System.out.println("Total Cost : " + result_pair_entry.getValue().total_cost);
                for(Map.Entry<String, Integer> server_entry : result_pair_entry.getValue().server_map.entrySet()){

                    System.out.println(server_entry.getKey() + " : " + server_entry.getValue());
                } 
            }

            System.out.println("--------------------------------------------------------");
        }
        
    }
}
