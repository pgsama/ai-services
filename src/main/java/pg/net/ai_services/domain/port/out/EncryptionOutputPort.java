package pg.net.ai_services.domain.port.out;

public interface EncryptionOutputPort {
    String encode(String plainText);
    String decode(String cipherText);
}
