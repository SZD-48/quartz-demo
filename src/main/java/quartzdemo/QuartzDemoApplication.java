package quartzdemo;

import org.quartz.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import quartzdemo.jobs.SimpleJob;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Properties;

@SpringBootApplication
@EnableScheduling
public class QuartzDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuartzDemoApplication.class, args);
    }

    @Bean("customSchedulerFactoryBean1")
    public SchedulerFactoryBean customSchedulerFactoryBean1(DataSource dataSource) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        Properties properties = new Properties();
        properties.setProperty("org.quartz.threadPool.threadNamePrefix", "my-custom-scheduler1_Worker");
        factory.setQuartzProperties(properties);
        factory.setDataSource(dataSource);
        return factory;
    }

    @Bean("customSchedulerFactoryBean2")
    public SchedulerFactoryBean customSchedulerFactoryBean2(DataSource dataSource) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        Properties properties = new Properties();
        properties.setProperty("org.quartz.threadPool.threadNamePrefix", "my-custom-scheduler2_Worker");
        factory.setQuartzProperties(properties);
        factory.setDataSource(dataSource);
        return factory;
    }

    @Bean("customScheduler1")
    public Scheduler customScheduler1(@Qualifier("customSchedulerFactoryBean1") SchedulerFactoryBean factory) throws SchedulerException {
        Scheduler scheduler = factory.getScheduler();
        scheduler.start();
        return scheduler;
    }

    @Bean("customScheduler2")
    public Scheduler customScheduler2(@Qualifier("customSchedulerFactoryBean2") SchedulerFactoryBean factory) throws SchedulerException {
        Scheduler scheduler = factory.getScheduler();
        scheduler.start();
        return scheduler;
    }

    @Bean
    public CommandLineRunner run(@Qualifier("customScheduler1") Scheduler customScheduler1,
                                 @Qualifier("customScheduler2") Scheduler customScheduler2) {
        return (String[] args) -> {
            Date afterFiveSeconds = Date.from(LocalDateTime.now().plusSeconds(5).atZone(ZoneId.systemDefault()).toInstant());

            JobDetail jobDetail1 = JobBuilder.newJob(SimpleJob.class).usingJobData("param", "value1").build();
            Trigger trigger1 = TriggerBuilder.newTrigger().startAt(afterFiveSeconds).build();
            customScheduler1.scheduleJob(jobDetail1, trigger1);

            JobDetail jobDetail2 = JobBuilder.newJob(SimpleJob.class).usingJobData("param", "value2").build();
            Trigger trigger2 = TriggerBuilder.newTrigger().startAt(afterFiveSeconds).build();
            customScheduler2.scheduleJob(jobDetail2, trigger2);
        };
    }
}
