package com.example.multi_tanent.pos.service;

import com.example.multi_tanent.pos.entity.ProductVariant;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class BarCodeService {

    private final FileStorageService fileStorageService;

    public BarCodeService(@Qualifier("posFileStorageService") FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    private static final int QR_CODE_WIDTH = 250;
    private static final int QR_CODE_HEIGHT = 250;

    /**
     * Generates a QR code image for the given text.
     *
     * @param text   The text to encode in the QR code.
     * @param width  The width of the QR code image.
     * @param height The height of the QR code image.
     * @return A byte array representing the QR code image in PNG format.
     * @throws WriterException If an error occurs during QR code generation.
     * @throws IOException     If an error occurs while writing the image to the byte stream.
     */
    public byte[] generateQRCodeImage(String text, int width, int height) throws WriterException, IOException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }

    /**
     * Generates a QR code for a specific product variant.
     * The QR code will contain a URL pointing to a public page for that variant.
     *
     * @param variant The ProductVariant entity.
     * @param width   The desired width of the QR code image.
     * @param height  The desired height of the QR code image.
     * @return A byte array for the generated QR code image.
     * @throws WriterException if the content cannot be encoded.
     * @throws IOException if an I/O error occurs.
     */
    public byte[] generateProductVariantQRCode(ProductVariant variant, int width, int height) throws WriterException, IOException {
        String url = buildPublicProductUrl(variant.getSku());
        return generateQRCodeImage(url, width, height);
    }

    /**
     * Generates a QR code for a product variant, saves it as an image file,
     * and returns the relative path to the saved image.
     *
     * @param variant The ProductVariant entity.
     * @return The relative path of the saved QR code image.
     * @throws WriterException if the content cannot be encoded.
     * @throws IOException if an I/O error occurs.
     */
    public String generateAndSaveProductVariantQRCode(ProductVariant variant) throws WriterException, IOException {
        String url = buildPublicProductUrl(variant.getSku());
        byte[] qrCodeImage = generateQRCodeImage(url, QR_CODE_WIDTH, QR_CODE_HEIGHT);

        String fileName = "qr-" + variant.getSku() + ".png";
        // The file path returned by storeFile is relative (e.g., "tenant_id/barcodes/qr-code.png")
        return fileStorageService.storeFile(qrCodeImage, fileName, "barcodes"); // Corrected method call
    }

    /**
     * Constructs a public-facing URL for a product variant using its SKU.
     */
    private String buildPublicProductUrl(String sku) {
        // This will build a URL like: http://localhost:8080/public/products/by-sku/SKU123
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/public/products/by-sku/{sku}")
                .buildAndExpand(sku)
                .toUriString();
    }
}
