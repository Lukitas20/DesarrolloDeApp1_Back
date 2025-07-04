package com.example.TpDA1.service;

import com.example.TpDA1.model.Package;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class QRCodeService {
    
    public String generateQRCodeForPackage(Package packageEntity) {
        String qrContent = "PACKAGE_" + packageEntity.getId() + "_" + packageEntity.getDescription();
        return generateQRCodeImage(qrContent);
    }
    
    public String generateQRCodeImage(String content) {
        try {
            // Configuración del QR
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            // Crear el QR
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 200, 200, hints);

            // Convertir a imagen
            BufferedImage qrImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = qrImage.createGraphics();
            
            // Fondo blanco
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, 200, 200);
            
            // QR negro
            graphics.setColor(Color.BLACK);

            for (int x = 0; x < 200; x++) {
                for (int y = 0; y < 200; y++) {
                    if (bitMatrix.get(x, y)) {
                        graphics.fillRect(x, y, 1, 1);
                    }
                }
            }
            graphics.dispose();

            // Convertir a Base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            String result = "data:image/png;base64," + base64Image;
            return result;
            
        } catch (WriterException | IOException e) {
            e.printStackTrace();
            // Fallback: retornar solo los datos como string
            return "QR_DATA:" + content;
        }
    }
} 