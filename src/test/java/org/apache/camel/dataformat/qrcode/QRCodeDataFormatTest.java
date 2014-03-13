/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.dataformat.qrcode;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class QRCodeDataFormatTest extends CamelTestSupport {
    
    private final static String MSG = "This is a testmessage!";
    
    @EndpointInject(uri = "mock:out")
    MockEndpoint out;
    
    @EndpointInject(uri = "mock:image")
    MockEndpoint image;
    
    @Test
    public void testDefaultQRCode() throws Exception {
        out.expectedBodiesReceived(MSG);
        image.expectedMessageCount(1);
        
        template.sendBody("direct:qrcode1", MSG);

        assertMockEndpointsSatisfied(5, TimeUnit.SECONDS);
        this.checkImage(image, 100, 100, ImageType.PNG.toString());
    }
    
    @Test
    public void testQRCodeWithModifiedSize() throws Exception {
        out.expectedBodiesReceived(MSG);
        image.expectedMessageCount(1);
        
        template.sendBody("direct:qrcode2", MSG);
        
        assertMockEndpointsSatisfied(5, TimeUnit.SECONDS);
        this.checkImage(image, 200, 200, ImageType.PNG.toString());
    }
    
    @Test
    public void testQRCodeWithJPEGType() throws Exception {
       out.expectedBodiesReceived(MSG);
       image.expectedMessageCount(1); 
       
       template.sendBody("direct:qrcode3", MSG);
       
       assertMockEndpointsSatisfied(5, TimeUnit.SECONDS);
       this.checkImage(image, 100, 100, "JPEG");
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() {
                
                // QR-Code default
                DataFormat qrcode1 = new QRCodeDataFormat();
                
                from("direct:qrcode1")
                        .marshal(qrcode1)
                        .to("file:target/out");
                
                // QR-Code with modified size
                DataFormat  qrcode2 = new QRCodeDataFormat(200, 200);
                
                from("direct:qrcode2")
                        .marshal(qrcode2)
                        .to("file:target/out");
                
                // QR-Code with modified type
                DataFormat qrcode3 = new QRCodeDataFormat(ImageType.JPG);
                
                from("direct:qrcode3")
                        .marshal(qrcode3)
                        .to("file:target/out");
                
                // read file and route it
                from("file:target/out?noop=true")
                        .multicast().to("direct:marshall", "mock:image");
                
                // get the message from qrcode
                from("direct:marshall")
                        .unmarshal(qrcode1) // for unmarshalling, the instance doesn't matter
                        .to("log:OUT")
                        .to("mock:out");
                        
            }
        };
    }
    
    private void checkImage(MockEndpoint mock, int height, int width, String type) throws IOException {
        Exchange ex = mock.getReceivedExchanges().get(0);
        File in = ex.getIn().getBody(File.class);
        
        // check image
        BufferedImage i = ImageIO.read(in);
        assertEquals(height, i.getHeight());
        assertEquals(width, i.getWidth());
        ImageInputStream iis = ImageIO.createImageInputStream(in);
        ImageReader reader = ImageIO.getImageReaders(iis).next();
        String format = reader.getFormatName();
        assertEquals(type, format.toUpperCase());
        in.delete();
    }
}
