package khoroshkin.coursework;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class APIScraper {
    private final int maxThreads;
    private final int timeoutSeconds;
    private final Runnable[] apiServices;
    private final DataWriter dataWriter;

    public APIScraper(int maxThreads, int timeoutSeconds, String[] serviceNames, String outputFormat) {
        this.maxThreads = maxThreads;
        this.timeoutSeconds = timeoutSeconds;
        this.dataWriter = new DataWriter(outputFormat);
        this.apiServices = createApiServices(serviceNames, dataWriter);
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
            // Logger
        } finally {
            executor.shutdownNow();
        }
    }

    private Runnable[] createApiServices(String[] serviceNames, DataWriter dataWriter) {
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

    private static class UnknownAPIService implements Runnable {
        private final String serviceName;
        private final DataWriter dataWriter;

        UnknownAPIService(String serviceName, DataWriter dataWriter) {
            this.serviceName = serviceName;
            this.dataWriter = dataWriter;
        }

        @Override
        public void run() {
            String data = "{\"error\": \"Unknown service\"}";
            dataWriter.saveData(data, serviceName);
        }
    }
}