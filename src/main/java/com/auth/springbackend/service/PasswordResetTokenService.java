package com.auth.springbackend.service;

import com.auth.springbackend.exception.InvalidTokenRequestException;
import com.auth.springbackend.model.token.PasswordResetToken;
import com.auth.springbackend.payload.request.ChangePasswordRequest;
import com.auth.springbackend.repository.PasswordResetTokenRepository;
import com.auth.springbackend.util.Util;
import com.auth.springbackend.config.AppProperties;
import com.auth.springbackend.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class PasswordResetTokenService {
    private final PasswordResetTokenRepository repository;
    private AppProperties appProperties;

    private final UserService userService;

    private final Long expiration;

    @Autowired
    public PasswordResetTokenService(PasswordResetTokenRepository repository, AppProperties appProperties, UserService userService) {
        this.repository = repository;
        this.appProperties = appProperties;
        expiration = appProperties.getAuth().getTokenExpirationMsec();
        this.userService = userService;
    }



    /**
     * Finds a token in the database given its naturalId or throw an exception.
     * The reset token must match the email for the user and cannot be used again
     */
    public PasswordResetToken getValidToken(ChangePasswordRequest request) {
        String tokenID = request.getPasswordResetToken();
        PasswordResetToken token = repository.findPasswordResetTokenByToken(tokenID);
                if(token == null)
                {
                    throw new InvalidTokenRequestException("Password Reset Token", tokenID, "Invalid token");
                }

        verifyExpiration(token);
        return token;
    }

    /**
     * Creates and returns a new password token to which a user must be
     * associated and persists in the token repository.
     */
    public Optional<PasswordResetToken> createToken(User user) {
        repository.deleteAllByUser(user);
        PasswordResetToken token = createTokenWithUser(user);
        return Optional.of(repository.save(token));
    }

    /**
     * Mark this password reset token as claimed (used by user to update password)
     * Since a user could have requested password multiple times, multiple tokens
     * would be generated. Hence, we need to invalidate all the existing password
     * reset tokens prior to changing the user password.
     */
    /*public PasswordResetToken claimToken(PasswordResetToken token) {
        User user = token.getUser();
        token.setClaimed(true);

        CollectionUtils.emptyIfNull(repository.findActiveTokensForUser(user))
                .forEach(t -> t.setActive(false));

        return token;
    }*/

    /**
     * Verify whether the token provided has expired or not on the basis of the current
     * server time and/or throw error otherwise
     */
    public void verifyExpiration(PasswordResetToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            deleteById(token.getId()); //repository.deletePasswordResetTokenByToken(token.getToken());
            throw new InvalidTokenRequestException("Password Reset Token", token.getToken(), "Password reset token was expired. Please make a new request");
        }

    }



    private PasswordResetToken createTokenWithUser(User user) {
        PasswordResetToken passwordResetToken = new PasswordResetToken();

        passwordResetToken.setUser(user);
        passwordResetToken.setExpiryDate(Instant.now().plusMillis(appProperties.getAuth().getRefreshTokenExpirationMsec()));
        passwordResetToken.setToken(Util.generateRandomUuid()); //Util.generateRandomUuid() UUID.randomUUID().toString()

        return passwordResetToken;
    }
    public Optional<PasswordResetToken> findByToken(String token) {
        return Optional.ofNullable(repository.findPasswordResetTokenByToken(token));
    }


    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
