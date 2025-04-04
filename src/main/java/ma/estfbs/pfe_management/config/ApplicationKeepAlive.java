package ma.estfbs.pfe_management.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ApplicationKeepAlive implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        // Print a message that the application is running
        System.out.println("APPLICATION IS NOW RUNNING - DO NOT CLOSE THIS WINDOW");
        
        // Keep the application running
        Thread keepAliveThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60000); // Sleep for 1 minute
                    System.out.println("Application still running... " + 
                        new java.util.Date());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        keepAliveThread.setDaemon(false); // Non-daemon thread will keep app alive
        keepAliveThread.start();
    }
}
