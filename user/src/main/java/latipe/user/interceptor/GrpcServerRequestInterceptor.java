package latipe.user.interceptor;


import static latipe.user.utils.GenTokenInternal.getPublicKey;
import static latipe.user.utils.GenTokenInternal.verifyHash;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import java.security.interfaces.RSAPublicKey;
import latipe.user.configs.SecureInternalProperties;
import latipe.user.exceptions.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GrpcServerRequestInterceptor implements ServerInterceptor {

    private final SecureInternalProperties secureInternalProperties;

    public GrpcServerRequestInterceptor(SecureInternalProperties secureInternalProperties) {
        this.secureInternalProperties = secureInternalProperties;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
        ServerCall<ReqT, RespT> serverCall, Metadata metadata,
        ServerCallHandler<ReqT, RespT> next) {

        log.info("Validating user token");
        var token = metadata.get(Metadata.Key.of("X-API-KEY", Metadata.ASCII_STRING_MARSHALLER));
        try {
            validateUserToken(token);
        } catch (Exception e) {
            throw Status.UNAUTHENTICATED.withDescription(e.getMessage()).asRuntimeException();
        }
        return next.startCall(serverCall, metadata);
    }

    private void validateUserToken(String token) throws Exception {
        if (token == null) {
            throw new UnauthorizedException("Unauthorized");
        }

        RSAPublicKey publicKey = getPublicKey(secureInternalProperties.getPublicKey());
        if (!verifyHash("user-service", token, publicKey)) {
            throw new UnauthorizedException("Unauthorized");
        }
    }
}