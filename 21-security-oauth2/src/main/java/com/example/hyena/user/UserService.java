package com.example.hyena.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final UserRepository userRepository;

    private final GroupRepository groupRepository;

    public UserService(UserRepository userRepository, GroupRepository groupRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        checkArgument(!username.isEmpty(), "username must be provided");
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByProviderAndProviderId(String provider, String providerId) {
        checkArgument(!provider.isEmpty(), "provider must be provided");
        checkArgument(!providerId.isEmpty(), "providerId must be provided");
        return userRepository.findByProviderAndProviderId(provider, providerId);
    }

    @Transactional
    public User join(OAuth2User oauth2User, String provider) {
        checkArgument(oauth2User != null, "oAuth2User must be provided");
        checkArgument(!provider.isEmpty(), "provider must be provided");

        String providerId = oauth2User.getName();
        return findByProviderAndProviderId(provider, providerId)
                .map(user -> {
                    log.warn("Already exists: {} for provider: {} providerId: {}", user, provider, providerId);
                    return user;
                })
                .orElseGet(() -> {
                    Map<String, Object> attributes = oauth2User.getAttributes();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
                    checkArgument(properties != null, "OAuth2User properties is empty");

                    String nickname = (String) properties.get("nickname");
                    String profileImage = (String) properties.get("profile_image");
                    Group group = groupRepository.findByName("USER_GROUP")
                            .orElseThrow(() -> new IllegalStateException("Could not found group for USER_GROUP"));
                    return userRepository.save(
                            new User(nickname, provider, providerId, profileImage, group)
                    );
                });
    }

    private void checkArgument(boolean isOk, String message) {
        if (!isOk) {
            throw new IllegalArgumentException(message);
        }
    }
}
