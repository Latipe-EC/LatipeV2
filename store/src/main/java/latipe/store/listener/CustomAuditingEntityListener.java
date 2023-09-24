package latipe.store.listener;

import java.util.Date;
import latipe.store.Entity.AbstractAuditEntity;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

@Component
public class CustomAuditingEntityListener extends AbstractMongoEventListener<Object> {
    @Override
    public void onBeforeConvert(BeforeConvertEvent<Object> event) {
        Object entity = event.getSource();

        if (entity instanceof AbstractAuditEntity abstractAuditEntity) {
            String currentUser = "Anonymous";
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            if (request.getAttribute("user") != null) {
                currentUser = ((UserCredentialResponse) request.getAttribute("user")).email();
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