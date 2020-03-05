package guru.sfg.brewery.beer_service.config;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Created by jt on 3/4/20.
 */
@EnableFeignClients
@EnableDiscoveryClient
@Profile("local-discovery")
@Configuration
public class LocalDiscovery {
}
