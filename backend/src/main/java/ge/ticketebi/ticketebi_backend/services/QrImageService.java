package ge.ticketebi.ticketebi_backend.services;

public interface QrImageService {
    byte[] generatePng(String content, int size);
}
