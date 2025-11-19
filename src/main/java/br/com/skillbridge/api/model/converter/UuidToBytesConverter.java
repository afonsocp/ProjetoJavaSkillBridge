package br.com.skillbridge.api.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.nio.ByteBuffer;
import java.util.UUID;

@Converter(autoApply = false)
public class UuidToBytesConverter implements AttributeConverter<UUID, byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(UUID attribute) {
        if (attribute == null) {
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(attribute.getMostSignificantBits());
        byteBuffer.putLong(attribute.getLeastSignificantBits());
        return byteBuffer.array();
    }

    @Override
    public UUID convertToEntityAttribute(byte[] dbData) {
        if (dbData == null || dbData.length != 16) {
            return null;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(dbData);
        long mostSignificantBits = byteBuffer.getLong();
        long leastSignificantBits = byteBuffer.getLong();
        return new UUID(mostSignificantBits, leastSignificantBits);
    }
}


