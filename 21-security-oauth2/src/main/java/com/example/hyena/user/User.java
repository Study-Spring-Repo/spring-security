package com.example.hyena.user;

import javax.persistence.*;
import java.util.Optional;

@Entity
@Table(name = "users")
public class User {


    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "provider")
    private String provider;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "profile_image")
    private String profileImage;

    @ManyToOne(optional = false)
    @JoinColumn(name = "group_id")
    private Group group;

    protected User() {/*no-op*/}


    public User(String username, String provider, String providerId, String profileImage, Group group) {
        checkArgument(!username.isEmpty(), "username must be provided.");
        checkArgument(!provider.isEmpty(), "provider must be provided.");
        checkArgument(!providerId.isEmpty(), "providerId must be provided.");
        checkArgument(group != null, "group must be provided.");

        this.username = username;
        this.provider = provider;
        this.providerId = providerId;
        this.profileImage = profileImage;
        this.group = group;
    }

    private void checkArgument(boolean isOk, String message) {
        if (!isOk) {
            throw new IllegalArgumentException(message);
        }
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderId() {
        return providerId;
    }

    public Optional<String> getProfileImage() {
        return Optional.ofNullable(profileImage);
    }

    public Group getGroup() {
        return group;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", provider='" + provider + '\'' +
                ", providerId='" + providerId + '\'' +
                ", profileImage='" + profileImage + '\'' +
                ", group=" + group +
                '}';
    }
}
