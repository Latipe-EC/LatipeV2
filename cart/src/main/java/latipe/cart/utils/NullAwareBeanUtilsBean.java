package latipe.cart.utils;

import java.lang.reflect.InvocationTargetException;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ContextClassLoaderLocal;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;

public class NullAwareBeanUtilsBean extends BeanUtilsBean {

  private static final ContextClassLoaderLocal<BeanUtilsBean>
      BEAN_UTILS_BEAN = new ContextClassLoaderLocal<BeanUtilsBean>() {
    @Override
    protected BeanUtilsBean initialValue() {
      return new NullAwareBeanUtilsBean();
    }
  };

  public NullAwareBeanUtilsBean() {
    super(new ConvertUtilsBean(), new PropertyUtilsBean());
  }

  public static BeanUtilsBean getInstance() {
    return BEAN_UTILS_BEAN.get();
  }

  @Override
  public void copyProperty(Object dest, String name, Object value)
      throws IllegalAccessException, InvocationTargetException {
    if (value == null || "id".equals(name)) {
      return;
    }
    super.copyProperty(dest, name, value);
  }
}
