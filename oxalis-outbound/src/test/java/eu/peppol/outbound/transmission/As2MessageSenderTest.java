package eu.peppol.outbound.transmission;

import com.google.inject.Injector;
import com.google.inject.Stage;
import eu.peppol.identifier.ParticipantId;
import eu.peppol.util.GlobalState;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.net.URL;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Attempts to send a message to the UNIT4 access point using the AS2 message sender.
 *
 * Purpose of this test is to verify that the implementation of the AS2 protocol
 * works as expected after refactoring.
 *
 * @author steinar
 * @author thore
 */
@Test(groups = {"manual"})
public class As2MessageSenderTest {

    MessageSender messageSender;
    TransmissionRequestBuilder transmissionRequestBuilder;

    @BeforeMethod
    public void setUp() {
        GlobalState.getInstance().setTransmissionBuilderOverride(true);
        Injector injector = com.google.inject.Guice.createInjector(Stage.DEVELOPMENT, new TransmissionTestModule());
        messageSender = injector.getInstance(As2MessageSender.class);
        transmissionRequestBuilder = injector.getInstance(TransmissionRequestBuilder.class);
    }

    @Test
    public void sendSampleEhfToUnit4() throws Exception {
        InputStream inputStream = As2MessageSenderTest.class.getClassLoader().getResourceAsStream("ehf-t10-alle-elementer.xml");
        assertNotNull(messageSender);
        assertNotNull(inputStream);
        String url = "https://ap-test.unit4.com/oxalis/as2";
        TransmissionRequest transmissionRequest = transmissionRequestBuilder
                .payLoad(inputStream)
                .receiver(new ParticipantId("9908:810017902"))
                .overrideAs2Endpoint(new URL(url), "APP_1000000006")
                .build();
        assertEquals(transmissionRequest.getEndpointAddress().getUrl(), new URL(url));
        TransmissionResponse response = messageSender.send(transmissionRequest);
        assertNotNull(response);
    }

}

