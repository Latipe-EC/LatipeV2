package latipe.schedule.configs;

import latipe.schedule.property.QuartzProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
@RequiredArgsConstructor
public class QuartzConfig {

  private final ApplicationContext applicationContext;
  private final QuartzProperties quartzProperties;

  @Bean
  public SchedulerFactoryBean schedulerFactoryBean() {
    SchedulerFactoryBean factory = new SchedulerFactoryBean();
    factory.setQuartzProperties(quartzProperties.getProperties());
    AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
    jobFactory.setApplicationContext(applicationContext);
    factory.setJobFactory(jobFactory);
    return factory;
  }


}
