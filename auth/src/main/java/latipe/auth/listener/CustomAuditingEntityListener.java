package latipe.auth.listener;

import jakarta.servlet.http.HttpServletRequest;
import latipe.auth.Entity.AbstractAuditEntity;
import latipe.auth.dtos.UserCredentialDto;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.ZonedDateTime;

@Component
public class CustomAuditingEntityListener extends AbstractMongoEventListener<Object> {
    @Override
    public void onBeforeConvert(BeforeConvertEvent<Object> event) {
        Object entity = event.getSource();

//        if (entity instanceof AbstractAuditEntity abstractAuditEntity) {
//            String currentUser = null;
//            HttpServletRequest request  =  ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
//            if ( request.getAttribute("user") != null) {
//                currentUser = ((UserCredentialDto) request.getAttribute("user")).getId();
//            }
//            ZonedDateTime currentDate = ZonedDateTime.now();
//            if (abstractAuditEntity.getCreatedDate() == null) {
//                abstractAuditEntity.setCreatedDate(currentDate);
//                abstractAuditEntity.setCreatedBy(currentUser);
//            }
//            abstractAuditEntity.setLastModifiedDate(currentDate);
//            abstractAuditEntity.setLastModifiedBy(currentUser);
//        }
    }

}