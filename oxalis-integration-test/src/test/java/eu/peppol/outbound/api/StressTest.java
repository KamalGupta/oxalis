package eu.peppol.outbound.api;

import eu.peppol.identifier.ParticipantId;
import eu.peppol.identifier.PeppolDocumentTypeIdAcronym;
import eu.peppol.identifier.PeppolProcessTypeIdAcronym;
import eu.peppol.outbound.OxalisOutboundModule;
import eu.peppol.outbound.transmission.TransmissionRequest;
import eu.peppol.outbound.transmission.TransmissionRequestBuilder;
import eu.peppol.outbound.transmission.TransmissionResponse;
import eu.peppol.outbound.transmission.Transmitter;
import eu.peppol.outbound.util.Log;
import eu.peppol.util.GlobalState;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.testng.Assert.assertNotNull;

/**
 * Performs a stress test of the access point sending the number of messages specified in
 * MESSAGES to the access point.
 *
 * User: nigel
 * Date: Dec 7, 2011
 * Time: 7:47:11 PM
 */
public class StressTest  {

    private static final long MESSAGES = 100;
    private static final int THREADS = 30;
    private static final long MEMORY_THRESHOLD = 10;
    protected static final String OXALIS_SERVICE_END_POINT = "https://localhost:8443/oxalis/as2";

    private static long lastUsage = 0;

    private OxalisOutboundModule oxalisOutboundModule;

    @BeforeMethod
    public void init() {
        GlobalState.getInstance().setTransmissionBuilderOverride(true);
        oxalisOutboundModule = new OxalisOutboundModule();
    }

    @Test(groups = {"manual"})
    public void test01() throws Exception {

        assertNotNull(oxalisOutboundModule);

        final List<Callable<Integer>> partitions = new ArrayList<Callable<Integer>>();

        long start = System.currentTimeMillis();

        for (int i = 1; i <= MESSAGES; i++) {

            partitions.add(new Callable<Integer>() {
                public Integer call() throws Exception {
                    sendDocument();
                    getMemoryUsage();
                    return 1;
                }
            });
        }

        final ExecutorService executorPool = Executors.newFixedThreadPool(THREADS);
        final List<Future<Integer>> values = executorPool.invokeAll(partitions, 1000, TimeUnit.SECONDS);
        int sum = 0;

        for (Future<Integer> result : values) {
            sum += result.get();
        }

        executorPool.shutdown();
        long millis = System.currentTimeMillis() - start;
        long seconds = millis / 1000;
        long rate = sum / seconds;
        System.out.println("");
        System.out.println("");
        System.out.println("%%% " + sum + " messages in " + seconds + " seconds, " + rate + " messages per second");
        System.out.println("");
    }

    @SuppressWarnings("unused")
    private void sendDocument() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("BII04_T10_EHF-v1.5_invoice.xml");
        assertNotNull(inputStream, "Unable to locate test file in class path");
        TransmissionRequestBuilder builder = oxalisOutboundModule.getTransmissionRequestBuilder();
        TransmissionRequest request = builder
            .sender(new ParticipantId("9908:976098897"))
            .receiver(new ParticipantId("9908:810017902"))
            .documentType(PeppolDocumentTypeIdAcronym.INVOICE.getDocumentTypeIdentifier())
            .processType(PeppolProcessTypeIdAcronym.INVOICE_ONLY.getPeppolProcessTypeId())
            .overrideAs2Endpoint(new URL(OXALIS_SERVICE_END_POINT), "bla-bla")
            .payLoad(inputStream)
            .build();
        Transmitter transmitter = oxalisOutboundModule.getTransmitter();
        TransmissionResponse response = transmitter.transmit(request);
        inputStream.close();
    }

    /**
     * returns a String describing current memory utilization. In addition unusually large
     * changes in memory usage will be logged.
     */
    public static String getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        long usedMemory = totalMemory - freeMemory;
        final long mega = 1048576;
        long usedInMegabytes = usedMemory / mega;
        long totalInMegabytes = totalMemory / mega;
        String memoryStatus = usedInMegabytes + "M / " + totalInMegabytes + "M / " + (runtime.maxMemory() / mega) + "M";
        if (usedInMegabytes <= lastUsage - MEMORY_THRESHOLD || usedInMegabytes >= lastUsage + MEMORY_THRESHOLD) {
            Log.info("Memory usage: " + memoryStatus);
            lastUsage = usedInMegabytes;
        }
        return memoryStatus;
    }

}
