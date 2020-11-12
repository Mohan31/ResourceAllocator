package Model;
import java.util.*;
/*
    Creating a own data type for structuring the result
    in the format <total_cost, <server_type, total_servers>>
    @server_map->@key is server_type
    @server_map->@value is total_servers
*/
public class ResultPair{

    public int total_cost;
    public HashMap<String,Integer> server_map;
    public ResultPair(){

        server_map = new HashMap<>();
    }

}