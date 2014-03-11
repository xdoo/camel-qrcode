package org.apache.camel.dataformat.qrcode;

import com.google.zxing.BarcodeFormat;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class QRCodeDataFormatTest extends CamelTestSupport {
    
    private final static String MSG = "This is a testmessage!";
    
    @Test
    public void testqrcode() throws Exception {
        
        MockEndpoint out = getMockEndpoint("mock:out");
        out.expectedBodiesReceived(MSG);
        template.sendBody("direct:qrcode", MSG);

        out.assertIsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() {
                
                // QR-Code
                DataFormat qrcode = new QRCodeDataFormat();
                
                from("direct:qrcode")
                        .marshal(qrcode)
                        .to("file:target/out");
                
                from("file:target/out?delete=true")
                        .unmarshal(qrcode)
                        .to("log:OUT")
                        .to("mock:out");
                
                
            }
        };
    }
}
