package latipe.user.services.user;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import latipe.user.grpc.GetDetailAddressRequest;
import latipe.user.grpc.GetDetailAddressResponse;
import latipe.user.grpc.UserServiceGrpc;
import latipe.user.repositories.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserGrpcService.class);
  private final IUserRepository userRepository;

  @Override
  public void getAddressDetail(GetDetailAddressRequest request,
      StreamObserver<GetDetailAddressResponse> responseObserver) {

    LOGGER.info("Received request get address detail");
    var user = userRepository.findById(request.getUserId()).orElseThrow(
        () -> Status.NOT_FOUND.withDescription("User not found").asRuntimeException()
    );

    var address = user.getAddresses().stream().filter(
        addr -> addr.getId().equals(request.getAddressId())
    ).findFirst().orElseThrow(
        () -> Status.NOT_FOUND.withDescription("Address not found").asRuntimeException()
    );

    var response = GetDetailAddressResponse.newBuilder()
        .setId(request.getAddressId())
        .setContactName(address.getContactName())
        .setPhone(address.getPhone())
        .setDetailAddress(address.getDetailAddress())
        .setCityOrProvinceId(address.getCityOrProvinceId())
        .setCityOrProvinceName(address.getCityOrProvinceName())
        .setDistrictId(address.getDistrictId())
        .setDistrictName(address.getDistrictName())
        .setWardId(address.getWardId())
        .setWardName(address.getWardName())
        .setCountryId(address.getCountryId().intValue())
        .setCountryName(address.getCountryName())
        .setZipCode(address.getZipCode() == null ? "" : address.getZipCode())
        .build();

    LOGGER.info("Sending response get address detail");
    responseObserver.onNext(response);
    responseObserver.onCompleted();
  }

}
