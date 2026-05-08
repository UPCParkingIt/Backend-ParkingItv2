package com.parkingit.shared.domain.valueobjects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FacialEmbedding {
    private String vectorData;  // Base64 encoded float array o JSON

    public static FacialEmbedding fromBase64(String base64Data) {
        if (base64Data == null || base64Data.isBlank()) {
            throw new IllegalArgumentException("Embedding data cannot be null");
        }
        return new FacialEmbedding(base64Data);
    }

    public static FacialEmbedding empty() {
        return new FacialEmbedding("");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FacialEmbedding that = (FacialEmbedding) o;
        return Objects.equals(vectorData, that.vectorData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vectorData);
    }
}