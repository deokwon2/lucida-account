package com.nkia.lucida.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.info.BuildProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

@EnableCaching
@SpringBootApplication
@ComponentScan(basePackages = {"com.nkia.lucida"})
public class Application {
  private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {
    commandLineAction(args);
  }

  private static final void commandLineAction(String[] args) {

    SpringApplication application = new SpringApplicationBuilder().sources(Application.class)
        .listeners(new ApplicationPidFileWriter("./application.pid")).build();
    application.run(args);
  }


  @EventListener(ApplicationReadyEvent.class)
  private void applicationReadyEventListener(ApplicationReadyEvent event) {

    BuildProperties buildProperties = event.getApplicationContext().getBean(BuildProperties.class);
    LOG.info("{}", printBuildInfo(buildProperties));

    Environment env = event.getApplicationContext().getBean(Environment.class);
    StringBuilder msg = new StringBuilder();
    msg.append("\n");
    msg.append("\n").append("Account Service has been started.");
    msg.append("\n").append("Port: ").append(env.getProperty("server.port"));
    msg.append("\n");
    LOG.info(msg.toString());
  }



  private static String printBuildInfo(BuildProperties buildProperties) {
    String textFormat = "%s %-30s %s";
    StringBuilder print = new StringBuilder();
    print.append("\n");
    print.append("\n").append("+-----------------------------------------------+");
    print.append("\n").append(
        String.format(textFormat, "| Product Name :", buildProperties.get("productName"), "|"));
    print.append("\n")
        .append(String.format(textFormat, "| Group        :", buildProperties.getGroup(), "|"));
    print.append("\n")
        .append(String.format(textFormat, "| Artifact     :", buildProperties.getArtifact(), "|"));
    print.append("\n")
        .append(String.format(textFormat, "| Version      :", buildProperties.getVersion(), "|"));
    print.append("\n")
        .append(String.format(textFormat, "| Build Date   :", buildProperties.get("date"), "|"));
    print.append("\n").append(
        String.format(textFormat, "| Create By    :", buildProperties.get("createBy"), "|"));
    print.append("\n").append("+-----------------------------------------------+");
    print.append("\n");
    return print.toString();
  }
}
