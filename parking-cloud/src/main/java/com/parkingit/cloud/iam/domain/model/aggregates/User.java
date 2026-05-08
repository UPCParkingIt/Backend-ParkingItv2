//package com.parkingit.cloud.iam.domain.model.aggregates;
//
//import com.parkingit.cloud.iam.domain.model.entities.Role;
//import com.parkingit.cloud.parking.domain.model.aggregates.Parking;
//import com.parkingit.cloud.shared.domain.aggregates.AuditableAbstractAggregateRoot;
//import jakarta.persistence.*;
//import jakarta.validation.constraints.Email;
//import jakarta.validation.constraints.Size;
//import lombok.Getter;
//import lombok.Setter;
//
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//@Entity
//@Table(name = "parking_users")
//@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
//@Getter
//@Setter
//public class User extends AuditableAbstractAggregateRoot<User> {
//    @Email
//    @Column(unique = true)
//    protected String email;
//
//    @Size(max = 120)
//    protected String password;
//
//    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//    @JoinTable(	name = "user_roles",
//            joinColumns = @JoinColumn(name = "user_id"),
//            inverseJoinColumns = @JoinColumn(name = "role_id"))
//    protected Set<Role> roles = new HashSet<>();
//
//    @Size(min = 1, max = 30)
//    private String firstName;
//
//    @Size(min = 1, max = 30)
//    private String lastName;
//
//    private String phoneNumber;
//
//    @Size(min = 8, max = 8)
//    private String dniNumber;
//
//    @ManyToOne
//    @JoinColumn(name = "parking_id")
//    private Parking managedParking;
//
//    public User() { }
//
//    public User(
//            String email,
//            String password,
//            String firstName,
//            String lastName,
//            String phoneNumber,
//            String dniNumber
//    ) {
//        this.email = email;
//        this.password = password;
//        this.firstName = firstName;
//        this.lastName = lastName;
//        this.phoneNumber = phoneNumber;
//        this.dniNumber = dniNumber;
//        this.roles = new HashSet<>();
//    }
//
//    public User(
//            String email,
//            String password,
//            String firstName,
//            String lastName,
//            String phoneNumber,
//            String dniNumber,
//            List<Role> roles
//    ) {
//        this(email, password, firstName, lastName, phoneNumber, dniNumber);
//        addRoles(roles);
//    }
//
//    public void addRoles(List<Role> roles) {
//        var validatedRoleSet = Role.validateRoleSet(roles);
//        this.roles.addAll(validatedRoleSet);
//    }
//
//    public String getUsername() {
//        return email;
//    }
//}

package com.parkingit.cloud.iam.domain.model.aggregates;

import com.parkingit.cloud.iam.domain.model.entities.Role;
import com.parkingit.cloud.iam.domain.model.entities.UserCompanion;
import com.parkingit.cloud.iam.domain.model.valueobjects.*;
import com.parkingit.cloud.parking.domain.model.aggregates.Parking;
import com.parkingit.cloud.shared.domain.aggregates.AuditableAbstractAggregateRoot;
import com.parkingit.shared.domain.valueobjects.FacialEmbedding;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Entity
@Table(name = "parking_users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NoArgsConstructor
@Getter
public class User extends AuditableAbstractAggregateRoot<User> {
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "email_value"))
    })
    private Email email;

    @Setter
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "hash", column = @Column(name = "password_hash"))
    })
    private HashedPassword password;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "firstName", column = @Column(name = "first_name")),
            @AttributeOverride(name = "lastName", column = @Column(name = "last_name"))
    })
    private PersonName personName;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "dni_number"))
    })
    private DNI dni;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "phone_number"))
    })
    private PhoneNumber phoneNumber;

    @Column(name = "driver_facial_embedding")
    private String driverFacialEmbedding;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "managed_parking_id")
    private Parking managedParking;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<UserCompanion> companions = new HashSet<>();

    public static User createRegisteredUser(
            Email email,
            HashedPassword password,
            PersonName personName,
            DNI dni,
            PhoneNumber phoneNumber,
            List<Role> roles
    ) {
        User user = new User();
        user.email = Objects.requireNonNull(email, "Email cannot be null");
        user.password = Objects.requireNonNull(password, "Password cannot be null");
        user.personName = Objects.requireNonNull(personName, "PersonName cannot be null");
        user.dni = Objects.requireNonNull(dni, "DNI cannot be null");
        user.phoneNumber = phoneNumber;
        user.isActive = true;
        user.roles = new HashSet<>(Role.validateRoleSet(roles));
        return user;
    }

    public static User createGuestUser(
            PersonName personName,
            String facialEmbedding
    ) {
        User user = new User();
        user.personName = Objects.requireNonNull(personName, "PersonName cannot be null");
        user.driverFacialEmbedding = facialEmbedding;
        user.email = null;
        user.password = null;
        user.dni = null;
        user.phoneNumber = null;
        user.isActive = true;
        user.roles = new HashSet<>(List.of(Role.getDefaultRole()));
        return user;
    }

    public void addRoles(List<Role> newRoles) {
        List<Role> validatedRoles = Role.validateRoleSet(newRoles);
        this.roles.addAll(validatedRoles);
    }

    public void assignParking(Parking parking) {
        if (parking == null) {
            throw new IllegalArgumentException("Parking cannot be null");
        }
        if (!this.hasRole(com.parkingit.cloud.iam.domain.model.valueobjects.Roles.ADMIN_ROLE)) {
            throw new IllegalStateException("Only ADMIN_ROLE users can manage parkings");
        }
        this.managedParking = parking;
    }

    public boolean hasRole(Roles role) {
        return roles.stream()
                .anyMatch(r -> r.getName() == role);
    }

    public void addCompanion(UserCompanion companion) {
        if (companion == null) {
            throw new IllegalArgumentException("Companion cannot be null");
        }
        if (companions.size() >= 5) {
            throw new IllegalStateException("Maximum 5 companions allowed");
        }
        companion.setUser(this);
        companions.add(companion);
    }

    public void removeCompanion(UserCompanion companion) {
        companion.setUser(null);
        companions.remove(companion);
    }

    public String getUsername() {
        return email != null ? email.getValue() : getId().toString();
    }

    public String getPasswordHash() {
        return password != null ? password.getHash() : null;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public Optional<FacialEmbedding> getDriverFacialEmbedding() {
        return driverFacialEmbedding != null
                ? Optional.of(new FacialEmbedding(driverFacialEmbedding))
                : Optional.empty();
    }

    public void validateForOperation() {
        if (!isActive) {
            throw new IllegalStateException("User is not active");
        }
        if (email == null && dni == null) {
            throw new IllegalStateException("User must have email or DNI");
        }
    }
}
