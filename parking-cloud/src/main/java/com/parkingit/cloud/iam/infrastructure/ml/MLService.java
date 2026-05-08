package com.parkingit.cloud.iam.infrastructure.ml;

import java.io.InputStream;

public interface MLService {
    String extractFacialEmbedding(InputStream imageInputStream);
    double compareFacialEmbeddings(String embedding1, String embedding2);
    boolean isValidFace(InputStream imageInputStream);
}
