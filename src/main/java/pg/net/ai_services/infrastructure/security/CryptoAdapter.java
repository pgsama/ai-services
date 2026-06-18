package pg.net.ai_services.infrastructure.security;

import org.springframework.stereotype.Component;

import pg.net.ai_services.domain.port.out.EncryptionOutputPort;
import pg.net.ai_services.utils.CryptoUtils;

@Component
public class CryptoAdapter implements EncryptionOutputPort {

    @Override
    public String encode(String plainText) {
        return CryptoUtils.encode(plainText);
    }

    @Override
    public String decode(String cipherText) {
        return CryptoUtils.decode(cipherText);
    }
}
