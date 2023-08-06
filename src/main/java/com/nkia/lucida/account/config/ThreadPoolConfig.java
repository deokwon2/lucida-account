package com.nkia.lucida.account.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import com.nkia.lucida.common.mongodb.TenantAsyncConfigurerSupport;

@EnableAsync
@EnableScheduling
@Configuration
public class ThreadPoolConfig extends TenantAsyncConfigurerSupport implements SchedulingConfigurer {

  private static final org.slf4j.Logger LOG =
      org.slf4j.LoggerFactory.getLogger(ThreadPoolConfig.class);

  @Autowired
  private ApplicationContext applicationContext;

  public static final String GLOBAL_TASK_SCHEDULER_NAME = "GlobalTaskScheduler";

  public static int SCHEDULER_POOL_SIZE = 3;

  @Value("${global-thread-pool-task-scheduler.pool-size:3}")
  private void setSchedulerPoolSize(int schedulerPoolSize) {
    ThreadPoolConfig.SCHEDULER_POOL_SIZE = schedulerPoolSize;
  }



  public static int EXECUTOR_CORE_POOL_SIZE = 3;

  @Value("${global-thread-pool-task-executor.core-pool-size:3}")
  private void setExecutorCorePoolSize(int executorCorePoolSize) {
    ThreadPoolConfig.EXECUTOR_CORE_POOL_SIZE = executorCorePoolSize;
  }

  public static int EXECUTOR_MAX_POOL_SIZE = 5;

  @Value("${global-thread-pool-task-executor.max-pool-size:5}")
  private void setExecutorMaxPoolSize(int executorMaxPoolSize) {
    ThreadPoolConfig.EXECUTOR_MAX_POOL_SIZE = executorMaxPoolSize;
  }

  public static int EXECUTOR_QUEUE_CAPACITY = 10;

  @Value("${global-thread-pool-task-executor.queue-capacity:10}")
  private void setExecutorQueueCapacity(int executorQueueCapacity) {
    ThreadPoolConfig.EXECUTOR_QUEUE_CAPACITY = executorQueueCapacity;
  }


  @Override
  public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
    scheduledTaskRegistrar.setTaskScheduler(taskScheduler());
  }


  @Bean(GLOBAL_TASK_SCHEDULER_NAME)
  public TaskScheduler taskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(SCHEDULER_POOL_SIZE);
    scheduler.setThreadNamePrefix(GLOBAL_TASK_SCHEDULER_NAME + "-");
    scheduler.initialize();
    return scheduler;
  }



  @Override
  public ThreadPoolTaskExecutor enableTenantAwareTaskDecorator() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(EXECUTOR_CORE_POOL_SIZE);
    executor.setMaxPoolSize(EXECUTOR_MAX_POOL_SIZE);
    executor.setQueueCapacity(EXECUTOR_QUEUE_CAPACITY);
    executor.setThreadNamePrefix(TENANT_ASYNC_EXECUTOR_BEAN_NAME + "-");
    executor.initialize();
    return executor;
  }



  @Override
  public void afterPropertiesSet() throws Exception {
    // Nothing to do ...
  }


  @Scheduled(cron = "0/60 * * * * ?")
  public void golbalThreadPoolMonitor() {

    if (LOG.isDebugEnabled()) {
      ThreadPoolTaskExecutor executor =
          applicationContext.getBean(TENANT_ASYNC_EXECUTOR_BEAN_NAME, ThreadPoolTaskExecutor.class);

      ThreadPoolTaskScheduler scheduler =
          applicationContext.getBean(GLOBAL_TASK_SCHEDULER_NAME, ThreadPoolTaskScheduler.class);

      LOG.debug(
          "Runner: {}\n\t{} activeCount: {}, queueSize: {}, coreSize: {}, maxPoolSize: {}, queueCapacity: {}\n\t{} activeCount: {}, poolSize: {}",
          Thread.currentThread().getName(), executor.getThreadNamePrefix(),
          executor.getActiveCount(), executor.getQueueSize(), executor.getCorePoolSize(),
          executor.getMaxPoolSize(), executor.getQueueCapacity(), scheduler.getThreadNamePrefix(),
          scheduler.getActiveCount(), scheduler.getPoolSize());
    }
  }
}
