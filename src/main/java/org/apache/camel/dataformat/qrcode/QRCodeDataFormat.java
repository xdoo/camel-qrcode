/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.apache.camel.dataformat.qrcode;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.util.ExchangeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author claus.straube
 */
public class QRCodeDataFormat implements DataFormat {
    
    private static final Logger LOG = LoggerFactory.getLogger(QRCodeDataFormat.class);
    
    ImageType imageType = ImageType.PNG;
    int width = 100;
    int height = 100;

    @Override
    public void marshal(Exchange exchange, Object graph, OutputStream stream) throws Exception {
        String payload = ExchangeHelper.convertToMandatoryType(exchange, String.class, graph);
        LOG.debug("Payload --> " + payload);
        QRCode.from(payload)
                .to(this.imageType)
                .withSize(this.width, this.height)
                .writeTo(stream);
    }

    @Override
    public Object unmarshal(Exchange exchange, InputStream stream) throws Exception {
        Map<DecodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<DecodeHintType, ErrorCorrectionLevel>();

        
        BufferedInputStream in = exchange.getContext().getTypeConverter().mandatoryConvertTo(BufferedInputStream.class, stream);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(ImageIO.read(in))));
        Result result = new MultiFormatReader().decode(bitmap,hintMap);
        return result.getText();
    }

    public ImageType getImageType() {
        return imageType;
    }

    public void setImageType(ImageType imageType) {
        this.imageType = imageType;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
    
}
