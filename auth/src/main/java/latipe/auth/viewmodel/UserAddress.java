package latipe.auth.viewmodel;

public record UserAddress(String id,
                          String contactName,
                          String phone,
                          String detailAddress,
                          String zipCode,
                          String cityOrProvinceId,
                          String cityOrProvinceName,
                          String districtId,
                          String districtName,
                          String wardId,
                          String wardName,
                          Long countryId,
                          String countryName) {


}
