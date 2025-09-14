package com.pdfprocessor.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.pdfprocessor.api.ApiApplication;
import com.pdfprocessor.api.controller.JobController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
    classes = ApiApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(
    properties = {
      "app.storage.base-path=/tmp/pdf-processor-test",
      "app.queue.redis.host=localhost",
      "app.queue.redis.port=6379",
      "app.security.api-keys[0]=test-key-67890"
    })
class ControllerLoadingTest {

  @Autowired private ApplicationContext applicationContext;

  @Test
  void shouldLoadJobController() {
    System.out.println("[DEBUG] Verificando se JobController está carregado...");

    // Listar todos os beans carregados
    String[] beanNames = applicationContext.getBeanDefinitionNames();
    System.out.println("[DEBUG] Total de beans carregados: " + beanNames.length);

    for (String beanName : beanNames) {
      if (beanName.toLowerCase().contains("controller")) {
        System.out.println("[DEBUG] Controller bean encontrado: " + beanName);
      }
    }

    // Verificar se JobController está presente
    boolean hasJobController = applicationContext.containsBean("jobController");
    System.out.println("[DEBUG] JobController presente: " + hasJobController);

    if (hasJobController) {
      JobController controller = applicationContext.getBean(JobController.class);
      assertNotNull(controller);
      System.out.println("[DEBUG] JobController carregado: " + controller);
    } else {
      System.out.println("[DEBUG] JobController NÃO foi carregado!");
    }
  }
}
