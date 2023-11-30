package be.kuleuven.distributedsystems.cloud.auth;

import java.math.BigInteger;
import java.security.interfaces.RSAPublicKey;

public class CustomRSAPublicKey implements RSAPublicKey {
    @Override
    public BigInteger getPublicExponent() {
        return null;
    }

    @Override
    public String getAlgorithm() {
        return null;
    }

    @Override
    public String getFormat() {
        return null;
    }

    @Override
    public byte[] getEncoded() {
        return new byte[0];
    }

    @Override
    public BigInteger getModulus() {
        return null;
    }
}
