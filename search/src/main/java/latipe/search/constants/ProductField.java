package latipe.search.constants;

public class ProductField {

    public static final String NAME = "name";
    public static final String BAN = "isBanned";
    public static final String PRICE = "price";
    public static final String CATEGORIES = "categories";
    public static final String CLASSIFICATIONS = "classifications";
    public static final String CREATE_ON = "createdDate";
    public static final String COUNT_SALE = "countSale";
    public static final String RATINGS = "ratings";


    private ProductField() {
        throw new UnsupportedOperationException(
            "This is a utility class and cannot be instantiated");
    }
}
