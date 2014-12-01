package eu.peppol;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author steinar
 * @author thore
 *         Date: 29.10.13
 *         Time: 18:06
 */
public class BusDoxProtocolTest {

    @Test(expectedExceptions = IllegalStateException.class,
            expectedExceptionsMessageRegExp = "Unknown protocol name : busdox-transport-start")
    public void testInstanceFromForSTART() throws Exception {
        BusDoxProtocol busDoxProtocol = BusDoxProtocol.instanceFrom("busdox-transport-start");
    }

    @Test
    public void testInstanceFromForAS2() throws Exception {
        BusDoxProtocol busDoxProtocol = BusDoxProtocol.instanceFrom("busdox-transport-as2-ver1p0");
        assertEquals(busDoxProtocol.name(), "AS2");
    }

}
