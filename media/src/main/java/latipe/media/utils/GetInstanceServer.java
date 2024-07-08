package latipe.media.utils;

import latipe.media.exceptions.NotFoundException;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;

public class GetInstanceServer {

    public static String get(LoadBalancerClient loadBalancer, String serviceId) {
        ServiceInstance instance = loadBalancer.choose(serviceId);
        if (instance == null) {
            return serviceId;
        }
        return instance.getUri().toString();
    }

}
