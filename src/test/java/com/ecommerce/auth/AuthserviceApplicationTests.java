package com.ecommerce.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static com.jayway.jsonpath.internal.path.PathCompiler.fail;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AuthserviceApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void testJavaVersion() {
        String version = System.getProperty("java.version");
        System.out.println("Java version: " + version);
        assertTrue(version.startsWith("21"), "Java version should start with 21");
    }

    @Test
    void testEnvVariableExists() {
        String env = System.getenv("SOME_ENV_VAR");
        System.out.println("SOME_ENV_VAR: " + env);
        assertTrue(env == null || !env.isEmpty());
    }

    @Test
    void testTempFileWritable() throws Exception {
        var tempFile = java.io.File.createTempFile("test", ".txt");
        try (var writer = new java.io.FileWriter(tempFile)) {
            writer.write("test");
        }
        assertTrue(tempFile.length() > 0, "Temp file should have data");
        tempFile.delete();
    }

    @Test
    void testCpuCoresAvailable() {
        int cores = Runtime.getRuntime().availableProcessors();
        System.out.println("CPU cores: " + cores);
        assertTrue(cores > 0, "CPU cores should be more than 0");
    }

    @Test
    void testMemoryAvailable() {
        long maxMemory = Runtime.getRuntime().maxMemory();
        System.out.println("Max memory: " + maxMemory);
        assertTrue(maxMemory > 0, "Max memory should be greater than 0");
    }

    @Test
    void testClassLoaderLoadsSpringBoot() {
        try {
            Class.forName("org.springframework.boot.SpringApplication");
        } catch (ClassNotFoundException e) {
            fail("Spring Boot class not found");
        }
    }

    @Test
    void testClasspathContainsH2Database() {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            fail("H2 Driver class not found");
        }
    }

    @Test
    void testClasspathContainsJwt() {
        try {
            Class.forName("io.jsonwebtoken.Jwts");
        } catch (ClassNotFoundException e) {
            fail("JWT class not found");
        }
    }

    @Test
    void testCurrentWorkingDirectory() {
        String cwd = System.getProperty("user.dir");
        System.out.println("Current working directory: " + cwd);
        assertNotNull(cwd);
        assertFalse(cwd.isEmpty());
    }
}
