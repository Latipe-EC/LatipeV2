package latipe.rating.utils;

import latipe.rating.exceptions.NotFoundException;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;

public class GetInstanceServer {

    public static String get(LoadBalancerClient loadBalancer, String serviceId) {
        ServiceInstance instance = loadBalancer.choose(serviceId);
        if (instance == null) {
            throw new NotFoundException("No instances found for service: " + serviceId);
        }
        return instance.getUri().toString();
    }

}
