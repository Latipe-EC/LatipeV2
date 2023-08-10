package latipe.user.listener;

import latipe.user.Entity.AbstractAuditEntity;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class CustomAuditingEntityListener extends AbstractMongoEventListener<Object> {
    @Override
    public void onBeforeConvert(BeforeConvertEvent<Object> event) {
        Object entity = event.getSource();
        if (entity instanceof AbstractAuditEntity abstractAuditEntity) {
            Date currentDate = new Date();
            if (abstractAuditEntity.getCreatedDate() == null) {
                abstractAuditEntity.setCreatedDate(currentDate);
            }
            abstractAuditEntity.setLastModifiedDate(currentDate);
        }
    }

}