package br.com.ifce.network.rmi;

import java.rmi.Naming;

public class OfflineMessagingServiceProvider {

    private static OfflineMessagingService INSTANCE;

    public static void lookup() {
        try {
            if (INSTANCE == null) {
                INSTANCE = (OfflineMessagingService) Naming.lookup("offline-messaging-service");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static OfflineMessagingService provide() {
        try {
            return INSTANCE;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
