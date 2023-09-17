package latipe.cart.utils;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ContextClassLoaderLocal;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;

import java.lang.reflect.InvocationTargetException;

public class NullAwareBeanUtilsBean extends BeanUtilsBean {

    private static final ContextClassLoaderLocal<BeanUtilsBean>
            BEAN_UTILS_BEAN = new ContextClassLoaderLocal<BeanUtilsBean>() {
        @Override
        protected BeanUtilsBean initialValue() {
            return new NullAwareBeanUtilsBean();
        }
    };

    public static BeanUtilsBean getInstance() {
        return BEAN_UTILS_BEAN.get();
    }

    public NullAwareBeanUtilsBean() {
        super(new ConvertUtilsBean(), new PropertyUtilsBean());
    }

    @Override
    public void copyProperty(Object dest, String name, Object value) throws IllegalAccessException, InvocationTargetException {
        if (value == null || "id".equals(name)) {
            return;
        }
        super.copyProperty(dest, name, value);
    }
}
