package khoroshkin.coursework;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class APIScraper {
    private static final Logger logger = LogManager.getLogger(APIScraper.class);
    private final int maxThreads;
    private final int timeoutSeconds;
    private final Runnable[] apiServices;
    private final DataWriter dataWriter;

    public APIScraper(int maxThreads, int timeoutSeconds, String[] serviceNames, String outputFormat) {
        this.maxThreads = maxThreads;
        this.timeoutSeconds = timeoutSeconds;
        this.dataWriter = new DataWriter(outputFormat);
        this.apiServices = createApiServices(serviceNames, dataWriter);
        logger.info("API services created");
    }

    public void start()  {
        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        try {
            while (!Thread.currentThread().isInterrupted()) {
                for (Runnable service : apiServices) {
                    executor.submit(() -> {
                        service.run();
                    });
                }
                TimeUnit.SECONDS.sleep(timeoutSeconds);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted while waiting for tasks to complete", e);
        } finally {
            executor.shutdown();
        }
    }

    Runnable[] createApiServices(String[] serviceNames, DataWriter dataWriter) {
        Map<String, Runnable> serviceMap = new HashMap<>();
        serviceMap.put("spotify", new SpotifyAPI(dataWriter));
        serviceMap.put("assembly", new AssemblyAPI(dataWriter));
        serviceMap.put("virustotal", new VirusTotalAPI(dataWriter));

        Runnable[] services = new Runnable[serviceNames.length];
        for (int i = 0; i < serviceNames.length; i++) {
            services[i] = serviceMap.getOrDefault(serviceNames[i], new UnknownAPIService(serviceNames[i], dataWriter));
        }
        return services;
    }

    static class UnknownAPIService implements Runnable {
        private final String serviceName;
        private final DataWriter dataWriter;

        UnknownAPIService(String serviceName, DataWriter dataWriter) {
            this.serviceName = serviceName;
            this.dataWriter = dataWriter;
        }

        @Override
        public void run() {
            String data = "{\"error\": \"Unknown service\"}";
            logger.warn("Unknown service requested: " + serviceName);
            dataWriter.saveData(data, serviceName);
        }
    }
}