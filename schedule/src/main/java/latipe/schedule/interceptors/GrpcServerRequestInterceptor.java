package latipe.schedule.interceptors;


import static latipe.schedule.utils.GenTokenInternal.getPublicKey;
import static latipe.schedule.utils.GenTokenInternal.verifyHash;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import java.security.interfaces.RSAPublicKey;
import latipe.schedule.configs.SecureInternalProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class GrpcServerRequestInterceptor implements ServerInterceptor {

    private final SecureInternalProperties secureInternalProperties;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
        ServerCall<ReqT, RespT> serverCall, Metadata metadata,
        ServerCallHandler<ReqT, RespT> next) {

        log.info("Validating schedule token");
        var token = metadata.get(Metadata.Key.of("x-api-key", Metadata.ASCII_STRING_MARSHALLER));
        try {
            validateUserToken(token);
        } catch (Exception e) {
            throw Status.UNAUTHENTICATED.withDescription(e.getMessage()).asRuntimeException();
        }
        return next.startCall(serverCall, metadata);
    }

    private void validateUserToken(String token) throws Exception {
        if (token == null) {
            throw new RuntimeException("Unauthorized");
        }

        RSAPublicKey publicKey = getPublicKey(secureInternalProperties.getPublicKey());
        if (!verifyHash("schedule-service", token, publicKey)) {
            throw new RuntimeException("Unauthorized");
        }
    }
}