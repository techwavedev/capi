package io.surisoft.capi.cache;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import io.surisoft.capi.schema.ThrottleServiceObject;
import io.surisoft.capi.utils.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConditionalOnProperty(prefix = "capi.throttling", name = "enabled", havingValue = "true")
public class HazelcastConfiguration {

    @Value("${capi.hazelcast.service-dns}")
    private String hazelcastServiceDns;

    @Bean
    public Config hazelcastConfig() {
        Config config = new Config();
        config.setClusterName("capi-cluster");
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getKubernetesConfig().setEnabled(true)
                .setProperty("service-dns", hazelcastServiceDns);

        MapConfig throttleMapConfig = new MapConfig(Constants.THROTTLE_CACHE_NAME);
        // Time to live for each entry will be handled by the ThrottleServiceObject's expiry
        throttleMapConfig.setTimeToLiveSeconds(0);
        config.addMapConfig(throttleMapConfig);

        return config;
    }

    @Bean
    public HazelcastInstance hazelcastInstance(Config hazelcastConfig) {
        return Hazelcast.newHazelcastInstance(hazelcastConfig);
    }

    @Bean(name = "throttleServiceObjectCache")
    public Map<String, ThrottleServiceObject> throttleServiceObjectCache(HazelcastInstance hazelcastInstance) {
        return hazelcastInstance.getMap(Constants.THROTTLE_CACHE_NAME);
    }
}
