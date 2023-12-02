package latipe.product.listener;

import java.util.Date;
import latipe.product.entity.AbstractAuditEntity;
import latipe.product.response.UserCredentialResponse;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class CustomAuditingEntityListener extends AbstractMongoEventListener<Object> {
  @Override
  public void onBeforeConvert(BeforeConvertEvent<Object> event) {
    Object entity = event.getSource();
    if (entity instanceof AbstractAuditEntity abstractAuditEntity) {
      String currentUser = "Anonymous";
      ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      if (requestAttributes != null
          && requestAttributes.getRequest().getAttribute("user") != null) {
        currentUser = ((UserCredentialResponse) requestAttributes.getRequest()
            .getAttribute("user")).email();
      }
      Date currentDate = new Date();
      if (abstractAuditEntity.getCreatedDate() == null) {
        abstractAuditEntity.setCreatedDate(currentDate);
        abstractAuditEntity.setCreatedBy(currentUser);
      }
      abstractAuditEntity.setLastModifiedDate(currentDate);
      abstractAuditEntity.setLastModifiedBy(currentUser);
    }
  }
}