package Model;

import java.util.*;
/*
    --> Further enhancement, if required
    Region model will give us the list of all regions available and can store region
    specific metadata.
*/
public class Region{

    private final String region_name;
    private List<Server> servers;
    
    public Region(String name){

        this.region_name = name;
        servers = new ArrayList<>();
    }

    public void add_server(Server server){
        
        this.servers.add(server);
    }

    public List<Server> get_servers(){

        return this.servers;
    }

    public String get_region_name(){

        return this.region_name;
    }

    
}
