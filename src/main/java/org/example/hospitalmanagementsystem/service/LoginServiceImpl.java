package org.example.hospitalmanagementsystem.service;

import lombok.RequiredArgsConstructor;
import org.example.hospitalmanagementsystem.dto.request.LoginRequestDto;
import org.example.hospitalmanagementsystem.dto.request.RefreshTokenRequestDto;
import org.example.hospitalmanagementsystem.dto.response.LoginResponseDto;
import org.example.hospitalmanagementsystem.dto.response.RefreshTokenResponseDto;
import org.example.hospitalmanagementsystem.entity.RefreshToken;
import org.example.hospitalmanagementsystem.entity.Register;
import org.example.hospitalmanagementsystem.exception.InvalidCredentials;
import org.example.hospitalmanagementsystem.exception.InvalidRefreshTokenException;
import org.example.hospitalmanagementsystem.exception.ResourceNotFoundException;
import org.example.hospitalmanagementsystem.repository.RefreshTokenRepository;
import org.example.hospitalmanagementsystem.repository.RegisterRepository;
import org.example.hospitalmanagementsystem.util.JwtUtil;
import org.example.hospitalmanagementsystem.util.RefreshTokenCache;
import org.example.hospitalmanagementsystem.util.TokenHasher;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final RegisterRepository registerRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenCache refreshTokenCache;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final TokenHasher tokenHasher;

    @Override
    @Transactional

    public LoginResponseDto logIn(LoginRequestDto loginRequestDto) {

try{
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.userName(),
                        loginRequestDto.password())
        );}catch (Exception e){
            throw new InvalidCredentials("Invalid Username or Password");
        }

        Register register = registerRepository.findByUsername(loginRequestDto.userName()).orElseThrow(() ->  new InvalidCredentials("Invalid Credentials"));
        String role = register.getRole();

        String accessToken = jwtUtil.generateToken(register.getId(), role);
        String refreshToken = jwtUtil.generateRefreshToken(register.getId(), role);

        persistRefreshToken(register.getId(), refreshToken);

        return new LoginResponseDto(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public RefreshTokenResponseDto createNewAccessToken(RefreshTokenRequestDto refreshToken) {
        String presentedToken = refreshToken.refreshToken();

        validatePresentedToken(presentedToken);

        RefreshToken storedToken = getValidStoredRefreshToken(presentedToken);

        Register register = getUser(storedToken.getUserId());

       rotateRefreshToken(storedToken);

       RefreshTokenResponseDto refreshTokenResponseDto = generateTokenPair(register);

        persistRefreshToken(register.getId(), refreshTokenResponseDto.newRefreshToken());

        return refreshTokenResponseDto;
    }



    private void revokeAllSessions(Long userId) {
        List<RefreshToken> active = refreshTokenRepository.findAllByUserIdAndRevokedFalse(userId);
        List<String> hashes = active.stream().map(RefreshToken::getTokenHash).toList();

        refreshTokenRepository.revokeAllByUserId(userId);
        refreshTokenCache.invalidateAll(hashes);
    }

    private void persistRefreshToken(Long userId, String rawToken) {

            String hash = tokenHasher.hash(rawToken);
            Duration ttl = Duration.ofSeconds(jwtUtil.getRefreshExpiration());
            Instant expiresAt = Instant.now().plus(ttl);

            RefreshToken entity = RefreshToken.builder()
                    .userId(userId)
                    .tokenHash(hash)
                    .expiresAt(expiresAt)
                    .revoked(false)
                    .createdAt(Instant.now())
                    .build();

            refreshTokenRepository.save(entity);
            refreshTokenCache.store(hash, userId, ttl);


    }

    private RefreshToken getValidStoredRefreshToken(String presentedToken) {

        String hash = tokenHasher.hash(presentedToken);

        RefreshToken token = refreshTokenRepository
                .findByTokenHash(hash)
                .orElseThrow(() -> new InvalidRefreshTokenException("Refresh token not recognized"));

        validateStoredToken(token);

        return token;
    }
    private void validatePresentedToken(String token) {

        if (!jwtUtil.isValidToken(token)) {
            throw new InvalidRefreshTokenException("Invalid refresh token");
        }

        if (!jwtUtil.isRefreshToken(token)) {
            throw new InvalidRefreshTokenException("Refresh token required");
        }
    }
    private void validateStoredToken(RefreshToken token) {

        if (token.isRevoked()) {

            revokeAllSessions(token.getUserId());

            throw new InvalidRefreshTokenException(
                    "Refresh token already used");
        }

        if (token.getExpiresAt().isBefore(Instant.now())) {

            revokeAllSessions(token.getUserId());

            throw new InvalidRefreshTokenException(
                    "Refresh token expired");
        }
    }
    private Register getUser(Long userId) {

        return registerRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    private void rotateRefreshToken(RefreshToken token) {

        token.setRevoked(true);

        try {

            refreshTokenRepository.saveAndFlush(token);

        } catch (OptimisticLockingFailureException ex) {

            throw new InvalidRefreshTokenException("Refresh token already used");
        }

        refreshTokenCache.invalidate(token.getTokenHash());
    }
    private RefreshTokenResponseDto generateTokenPair(Register user) {

        String access =
                jwtUtil.generateToken(
                        user.getId(),
                        user.getRole());

        String refresh =
                jwtUtil.generateRefreshToken(
                        user.getId(),
                        user.getRole());

        return new RefreshTokenResponseDto(access, refresh);
    }
}