package com.auth.springbackend.controller;

import com.auth.springbackend.email.EmailDetails;
import com.auth.springbackend.email.EmailServiceImpl;
import com.auth.springbackend.exception.BadRequestException;
import com.auth.springbackend.model.AuthProvider;
import com.auth.springbackend.model.User;
import com.auth.springbackend.model.token.ConfirmationToken;
import com.auth.springbackend.model.token.PasswordResetToken;
import com.auth.springbackend.model.token.RefreshToken;
import com.auth.springbackend.payload.request.*;
import com.auth.springbackend.payload.request.user.AddNewUserRequest;
import com.auth.springbackend.payload.response.ApiResponse;
import com.auth.springbackend.payload.response.AuthResponse;
import com.auth.springbackend.repository.ConfirmationTokenRepository;
import com.auth.springbackend.security.CurrentUser;
import com.auth.springbackend.security.JwtTokenProvider;
import com.auth.springbackend.security.UserPrincipal;
import com.auth.springbackend.service.PasswordResetTokenService;
import com.auth.springbackend.service.UserService;
import com.auth.springbackend.config.AppProperties;
import com.auth.springbackend.exception.TokenRefreshException;
import com.auth.springbackend.service.RefreshTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AppProperties appProperties;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenManager;
    private final UserService userService;
    private final EmailServiceImpl emailService;
    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final PasswordResetTokenService passwordResetTokenService;

    // Constructor
    public AuthController(AppProperties appProperties, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder, JwtTokenProvider tokenManager, RefreshTokenService refreshTokenService, UserService userService, EmailServiceImpl emailService, ConfirmationTokenRepository confirmationTokenRepository, PasswordResetTokenService passwordResetTokenService) {
        this.appProperties = appProperties;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.tokenManager = tokenManager;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
        this.emailService = emailService;
        this.confirmationTokenRepository = confirmationTokenRepository;
        this.passwordResetTokenService = passwordResetTokenService;
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshtoken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = tokenManager.createToken(user.getId());
                    return ResponseEntity.ok(new AuthResponse(token, requestRefreshToken, appProperties.getAuth().getTokenType()));
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));
    }

    // baeldung.com/spring-request-response-body
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        if(loginRequest.getEmail() != null || loginRequest.getLogin() != null)
        {
            if(loginRequest.getLogin()!=null)
            {
                Optional<User> user = userService.findByLogin(loginRequest.getLogin());
                if(user.isPresent())
                {
                    loginRequest.setEmail(user.get().getEmail());
                }
                else {
                    return ResponseEntity.badRequest().body(new ApiResponse(false,"Bad credentials"));
                }

            }
        }
        else {
           return ResponseEntity.badRequest().body(new ApiResponse(false, "ooops! It seams like we dont have this user in our system"));
        }
        Optional<User> us1 = userService.findByEmail(loginRequest.getEmail());
        if(!us1.get().getActive())
        {
            return ResponseEntity.badRequest().body(new ApiResponse(false,"You need to confirm your account"));
        }
        // auth
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );
        //  Set auth
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // Create token
        // UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String token = tokenManager.createToken(userPrincipal.getId());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userPrincipal.getId()); //authentication.getPrincipal();
        // If all good we return token
        return ResponseEntity.ok(new AuthResponse(token, refreshToken.getToken(), appProperties.getAuth().getTokenType()));
    }

    @RequestMapping(value="/confirm-account", method= {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> confirmUserAccount(@RequestParam("token")String confirmationToken) {
        return userService.confirmEmail(confirmationToken);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logoutUser(@CurrentUser UserPrincipal userPrincipal) {
        refreshTokenService.deleteByUserId(userPrincipal.getId());
        return ResponseEntity.ok(new ApiResponse(true, "Log out successful"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) throws MessagingException {
        // Checks if email or login already exist or they not null
        if(signUpRequest.getEmail() == null && signUpRequest.getLogin() == null)
        {
            return ResponseEntity.badRequest().body(new ApiResponse(false,"Email/login field is not filled"));
        }

        if(userService.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new ApiResponse(false,"This email address already in use"));
        }

        if(userService.existsByLogin(signUpRequest.getLogin())) {
            return ResponseEntity.badRequest().body(new ApiResponse(false,"This username already taken"));
        }

        /*User user = userService.createUser(signUpRequest);
        userService.save(user);*/
        // Creating user's account
        User user = new User();
        user.setLogin(signUpRequest.getLogin());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(signUpRequest.getPassword());
        user.setProvider(AuthProvider.local);
        user.setActive(false);
        user.setFullname(signUpRequest.getFullname());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Save user
        User result = userService.save(user);
        ConfirmationToken confirmationToken = new ConfirmationToken(user);

        confirmationTokenRepository.save(confirmationToken);

        String link = "https://gribble-ua.netlify.app/en/confirm-account?token=" + confirmationToken.getConfirmationToken();
        EmailDetails email = new EmailDetails();
        email.setRecipient(user.getEmail());
        //email.setFrom("Gribble");
        email.setMsgBody("Please Confirm your account to access our resource");
        email.setSubject("Confirm your Email");

        email.setTemplate("confirm-email-template.html");

        Map<String, Object> properties = new HashMap<>();
        properties.put("link", link);
        properties.put("msgBody", email.getMsgBody());

        email.setProperties(properties);

        emailService.sendHtmlMessage(email);




        // Set URI, so we would know from where we get response
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/user/me")
                .buildAndExpand(result.getId()).toUri();

        // ResponseEntity extend HttpEntity and add Status code (also there body and headers)
        // here we specify response status so in the future on front we can work with it (that's why we don't use @requestbody)
        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "User registered successfully, please confirm your email"));
    }

    @PostMapping("/add-new-user")
    public ResponseEntity<?> addNewUser(@Valid @RequestBody AddNewUserRequest addNewUserRequest) {
        User user = new User();
        user.setLogin(addNewUserRequest.getLogin());
        user.setEmail(addNewUserRequest.getEmail());
        user.setPassword(addNewUserRequest.getPassword());
        user.setProvider(AuthProvider.local);
        user.setActive(true);
        user.setFullname(addNewUserRequest.getFullname());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Save user
        userService.save(user);
        //email

        return ResponseEntity.ok(new ApiResponse(true, "New User registered"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetRequest passwordResetRequest) throws MessagingException {
       Optional<User> user = userService.findByEmail(passwordResetRequest.getEmail());
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "ooops! It seams like we dont have this user in our system"));
        }
        String token = UUID.randomUUID().toString();
        passwordResetTokenService.createToken(user.get());

        String link = "https://gribble-ua.netlify.app/en/change-password?token=" + token;
        EmailDetails email = new EmailDetails();
        email.setRecipient(user.get().getEmail());
        email.setFrom("Gribble");
        email.setMsgBody("Reset your password by the link below");
        email.setSubject("Reset password");
        email.setTemplate("confirm-email-template.html");

        Map<String, Object> properties = new HashMap<>();
        properties.put("link", link);
        properties.put("msgBody", email.getMsgBody());
        email.setProperties(properties);

        emailService.sendHtmlMessage(email);
        //email

        return ResponseEntity.ok(new ApiResponse(true, "We send you link where you can reset password"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?>changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest)
    {
        PasswordResetToken token = passwordResetTokenService.getValidToken(changePasswordRequest);
        passwordResetTokenService.deleteById(token.getId());
        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(changePasswordRequest.getPassword()));
        userService.save(user);

        return ResponseEntity.ok(new ApiResponse(true, "Password reset successfully"));
    }

}

