package latipe.user.controllers;


import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import latipe.auth.Entity.User;
import latipe.auth.config.ApiPrefixController;
import latipe.auth.config.JwtTokenService;
import latipe.auth.exceptions.BadRequestException;
import latipe.auth.exceptions.NotFoundException;
import latipe.auth.exceptions.SignInRequiredException;
import latipe.auth.repositories.IUserRepository;
import latipe.auth.utils.GenTokenUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController()
@ApiPrefixController("/auth")
@Tag(name = "User authentication")
public class AuthController {
    private final JwtTokenService jwtUtil;
//    private final IUserRepository userRepository;
    private final ModelMapper toDto;
    private final IUserRepository userRepository;

    @Value("${URL_FE}")
    private String URL;

    public AuthController( JwtTokenService jwtUtil, IUserRepository userRepository, ModelMapper toDto) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.toDto = toDto;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public CompletableFuture<LoginDto>  createAuthenticationToken(@RequestBody @Valid LoginInputDto loginRequest)  {
        return CompletableFuture.supplyAsync(() -> {
            final List<User> users = userRepository.findByPhoneAndEmail(loginRequest.getUsername());
            if (users.size() == 0) {
                throw new NotFoundException("Cannot find user with email");
            }
            if (!JwtTokenService.comparePassword(loginRequest.getPassword(), users.get(0).getPassword())) {
                throw new BadRequestException("Password not correct");
            }
            final String accessToken = jwtUtil.generateAccessToken(users.get(0));
            final String refreshToken = jwtUtil.generateRefreshToken(users.get(0));
            toDto.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
            return new LoginDto(accessToken, refreshToken, toDto.map(users.get(0), UserProfileDto.class));
        });
    }

    @PostMapping("/refresh-token")
    @ResponseStatus(HttpStatus.OK)
    public CompletableFuture<RefreshTokenDto> refreshAuthenticationToken(@RequestBody @Valid RefreshTokenInput refreshTokenRequest) {
        return CompletableFuture.supplyAsync(() -> {
            final String refreshToken = refreshTokenRequest.getRefreshToken();
            // Check if the refresh token is valid and not expired
            String username = null;
            try {
                username = jwtUtil.checkRefreshToken(refreshToken);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new RuntimeException(e);
            }
            if (username == null)
                throw new BadRequestException("Not Type Refresh Token");
            List<User> users = null;
            try {
                users = userRepository.findByPhoneAndEmail(jwtUtil.getUsernameFromToken(refreshToken));
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new NotFoundException("Cannot find user with email or phone");
            }
            if (users.size() == 0)
                throw new NotFoundException("Cannot find user with email");
            try {
                if (jwtUtil.validateToken(refreshToken, users.get(0))) {
                    final String accessToken = jwtUtil.generateAccessToken(users.get(0));
                    return new RefreshTokenDto(accessToken, refreshToken);
                }
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new RuntimeException(e);
            }
            throw new BadRequestException("Invalid refresh token");
        });
    }

    @PostMapping("/validate-token")
    @ResponseStatus(HttpStatus.OK)
    public CompletableFuture<UserCredentialDto> validateToken(@RequestBody @Valid String accessToken) {
        return CompletableFuture.supplyAsync(() -> {
            String username = null;
            try {
                username = JwtTokenService.getUsernameFromToken(accessToken);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new RuntimeException(e);
            }
            if (username == null) {
                throw new SignInRequiredException("Token is invalid!");
            }
            List<User> users = userRepository.findByPhoneAndEmail(username);
            if (users.size() == 0) {
                throw new NotFoundException("Cannot find user with email");
            }
            else if (users.get(0).getPoint() < -100) {
                throw new BadRequestException("Your account has been locked due to too many cancellations");
            } else if (users.get(0).getIsDeleted())
                throw new BadRequestException("Your account has been deleted");
            try {
                if (JwtTokenService.validateToken(accessToken, users.get(0))) {
                    return UserCredentialDto.builder().email(users.get(0).getEmail()).phone(users.get(0).getPhoneNumber()).id(users.get(0).getId()).build();
                }
                throw new SignInRequiredException("Token is expired!");
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new RuntimeException(e);
            }
        });
    }
    @PostMapping("/request-reset-password-by-email")
    public ResponseEntity<?> requestResetPasswordByEmail(@RequestParam("email") String userEmail) throws UnsupportedEncodingException {
        List<User> users = userRepository.findByPhoneAndEmail(userEmail);
        if (users.size() == 0) {
            throw new NotFoundException("Cannot find user with email");
        }
        User user = users.get(0);
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(5);
        TokenResetPasswordDto tokenResetPassword = new TokenResetPasswordDto(token, userEmail, expiryDate, 0);
        user = GenTokenUtils.setToken(user, tokenResetPassword);
        userRepository.save(users.get(0));
        String resetPasswordUrl = URL + URLEncoder.encode(users.get(0).getTokenResetPassword(), StandardCharsets.UTF_8);
        // notification service will cover
//        emailService.sendResetPasswordEmail(userEmail, resetPasswordUrl);
        return ResponseEntity.ok(true);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPasswordByToken( @RequestBody PayLoadResetPasswordByPhone input) {
        User user = userRepository.findByTokenResetPassword(input.getToken()).orElseThrow(() -> new NotFoundException("User not found"));
        TokenResetPasswordDto tokenResetPassword = GenTokenUtils.decodeToken(user.getTokenResetPassword());
        if (tokenResetPassword == null || tokenResetPassword.isExpired()) {
            return ResponseEntity.badRequest().body("Token is expired");
        }
        if (!tokenResetPassword.getEmail().equals(user.getEmail())) {
            return ResponseEntity.badRequest().body("Email not match!");
        }
        user.setRequestCount(0);
        user.setLastRequest(null);
        user.setHashedPassword(JwtTokenService.hashPassword(input.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(true);
    }

    @PostMapping("/request-pin/{phone}")
    public void requestPin(@PathVariable("phone") String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber).orElseThrow(() -> new NotFoundException("User not found"));
        TokenResetPasswordDto token = new TokenResetPasswordDto();
        token.setToken(GenTokenUtils.generateRandomDigits());
        token.setEmail(user.getEmail());
        token.setExpired(LocalDateTime.now().plusMinutes(1).plusSeconds(2));
        token.setType(1);
        user = GenTokenUtils.setToken(user, token);
        user = userRepository.save(user);
        String messageText = "Mã xác thực của bạn là: " + token.getToken();

        // notification will cover
//        smsService.sendSMS(phoneNumber.replaceFirst("^0", "+84"), messageText);
    }

    @PostMapping("/validate-pin/{phone}")
    public ResponseEntity<?> validatePin(@PathVariable String phone, @RequestBody TokenDto input) throws UnsupportedEncodingException {
        User user = userRepository.findByPhoneNumber(phone).orElseThrow(() -> new NotFoundException("User not found"));
        TokenResetPasswordDto tokenResetPassword = GenTokenUtils.decodeToken(user.getTokenResetPassword());
        if (tokenResetPassword == null || tokenResetPassword.isExpired()) {
            return ResponseEntity.badRequest().body("Token is expired");
        }
        if (!tokenResetPassword.getToken().equals(input.getToken())) {
            return ResponseEntity.badRequest().body("Pin code is not correct!");
        }
        tokenResetPassword.setExpired(LocalDateTime.now().plusMinutes(1).plusSeconds(2).plusSeconds(2));
        user.setTokenResetPassword(GenTokenUtils.encodeToken(tokenResetPassword));
        userRepository.save(user);
        return ResponseEntity.ok(URL + URLEncoder.encode(user.getTokenResetPassword(), StandardCharsets.UTF_8));
    }

}