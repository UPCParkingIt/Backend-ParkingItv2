package com.parkingit.cloud.iam.domain.model.entities;

import com.parkingit.cloud.iam.domain.model.aggregates.User;
import com.parkingit.cloud.iam.domain.model.valueobjects.PersonName;
import com.parkingit.shared.domain.valueobjects.FacialEmbedding;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_companions")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserCompanion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "firstName", column = @Column(name = "first_name")),
            @AttributeOverride(name = "lastName", column = @Column(name = "last_name"))
    })
    private PersonName personName;

    @Column(name = "facial_embedding", columnDefinition = "TEXT")
    private String facialEmbedding;

    @Column(name = "face_image_s3_url")
    private String faceImageS3Url;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    public static UserCompanion create(
            User user,
            PersonName personName,
            String facialEmbedding
    ) {
        UserCompanion companion = new UserCompanion();
        companion.user = user;
        companion.personName = personName;
        companion.facialEmbedding = facialEmbedding;
        companion.createdAt = LocalDateTime.now();
        companion.isVerified = false;
        return companion;
    }

    public void verify() {
        this.isVerified = true;
        this.verifiedAt = LocalDateTime.now();
    }

    public void setFaceImage(String s3Url) {
        this.faceImageS3Url = s3Url;
    }

    public FacialEmbedding getFacialEmbeddingValue() {
        return new FacialEmbedding(facialEmbedding);
    }
}
