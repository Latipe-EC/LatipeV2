package latipe.store.utils;

import latipe.store.exceptions.NotFoundException;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;

public class GetInstanceServer {

    public static String get(LoadBalancerClient loadBalancer, String serviceId) {

        var instance = loadBalancer.choose(serviceId);
        if (instance == null) {
            return serviceId;
        }
        return instance.getUri().toString();
    }

}
