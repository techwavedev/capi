package io.surisoft.capi.configuration;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.grid.hazelcast.HazelcastProxyManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "capi.hazelcast.enabled", havingValue = "true")
public class HazelcastConfiguration {

    @Value("${capi.hazelcast.uris}")
    private String hazelcastUris;

    @Bean
    public HazelcastInstance hazelcastInstance() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.getNetworkConfig().addAddress(hazelcastUris.split(","));
        return HazelcastClient.newHazelcastClient(clientConfig);
    }

    @Bean
    ProxyManager<String> proxyManager(HazelcastInstance hazelcastInstance) {
        return new HazelcastProxyManager<>(hazelcastInstance.getMap("capi-bucket-map"));
    }
}
