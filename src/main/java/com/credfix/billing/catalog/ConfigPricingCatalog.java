package com.credfix.billing.catalog;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Loads service pricing configuration from a properties file in resources.
 */
public class ConfigPricingCatalog implements PricingCatalog {
    private static final String SERVICES_KEY = "services";
    private final Map<String, ServicePlan> plans;

    public ConfigPricingCatalog(String resourcePath) {
        this.plans = loadPlans(resourcePath);
    }

    @Override
    public Optional<ServicePlan> findPlan(String serviceType) {
        return Optional.ofNullable(plans.get(serviceType));
    }

    private Map<String, ServicePlan> loadPlans(String resourcePath) {
        Properties properties = new Properties();
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalArgumentException("Could not find pricing config: " + resourcePath);
            }
            properties.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load pricing config: " + resourcePath, e);
        }

        String services = properties.getProperty(SERVICES_KEY, "");
        Map<String, ServicePlan> loaded = new HashMap<>();
        Arrays.stream(services.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(service -> loaded.put(service, buildPlan(service, properties)));
        return Map.copyOf(loaded);
    }

    private ServicePlan buildPlan(String serviceType, Properties properties) {
        String prefix = "service." + serviceType + ".";
        String billingType = required(properties, prefix + "billingType");
        String unit = required(properties, prefix + "unit");

        Map<String, String> params = new HashMap<>();
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith(prefix)) {
                String suffix = key.substring(prefix.length());
                if (!"billingType".equals(suffix) && !"unit".equals(suffix)) {
                    params.put(suffix, properties.getProperty(key));
                }
            }
        }
        return new ServicePlan(serviceType, billingType, unit, params);
    }

    private String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required config key: " + key);
        }
        return value;
    }
}

