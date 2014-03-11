package org.apache.camel.dataformat.qrcode;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class QRCodeDataFormatTest extends CamelTestSupport {
    
    @Test
    public void testqrcode() throws Exception {
        
        MockEndpoint out = getMockEndpoint("mock:out");
        out.expectedBodiesReceived("Das ist ein Test.");
        template.sendBody("direct:start", "Das ist ein Test.");

        out.assertIsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() {
                
                DataFormat format = new QRCodeDataFormat();
                
                from("direct:start")
                        .marshal(format)
                        .to("file:out");
                
                from("file:out?delete=true")
                        .unmarshal(format)
                        .to("log:OUT")
                        .to("mock:out");
            }
        };
    }
}
