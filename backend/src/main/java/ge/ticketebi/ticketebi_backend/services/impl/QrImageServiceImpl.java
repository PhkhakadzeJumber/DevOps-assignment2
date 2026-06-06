package ge.ticketebi.ticketebi_backend.services.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import ge.ticketebi.ticketebi_backend.services.QrImageService;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class QrImageServiceImpl implements QrImageService {

    @Override
    public byte[] generatePng(String content, int size) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    content,
                    BarcodeFormat.QR_CODE,
                    size,
                    size,
                    hints
            );
            return toPng(bitMatrix);
        } catch (WriterException | IOException ex) {
            throw new IllegalStateException("Failed to generate QR code PNG", ex);
        }
    }

    private byte[] toPng(BitMatrix matrix) throws IOException {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, matrix.get(x, y) ? 0x000000 : 0xFFFFFF);
            }
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", outputStream);
        return outputStream.toByteArray();
    }
}
