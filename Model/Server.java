package Model;

public class Server {
    
    /*
        server_type  - specifies the type such as "large", "2xlarge".
        server_cost - cost per server per hour.
        number_of_cpu - count of total cpu's per server type
        hours_remaining - total hours the servers can be utilised, initially I kept it as 24hrs
                            for testing.
        region_name - Gives the region where the server is located.
        server_status - Gives the server status {Available, Occupied, Reserved, Error}. If the server
                        has '0' hours_remaining then the status will become "Occupied".
    */
    private final String server_type;
    private final double server_cost;
    private final int number_of_cpu;
    private int hours_remaining;
    private final String region_name;
    private ServerStatus server_status;


    public Server(String type, double cost, int cpus, int hours, String region_name){

        this.server_type = type;
        this.server_cost = cost;
        this.number_of_cpu = cpus;
        this.hours_remaining = hours;
        this.region_name = region_name;
        this.server_status = ServerStatus.Available;
    }

    public ServerStatus get_status(){

        return this.server_status;

    }

    public void update_status(ServerStatus server_status){

        this.server_status = server_status;
    }

    public int get_number_of_cpu(){

        return this.number_of_cpu;
    }

    public double get_server_cost(){

        return this.server_cost;
    }

    public int get_hours_remaining(){

        return this.hours_remaining;
    }

    public void update_server(int hours_reserved){

        this.hours_remaining -= hours_reserved;
    }

    public String get_region(){

        return this.region_name;
    }

    public String get_server_type(){

        return this.server_type;
    }
}
