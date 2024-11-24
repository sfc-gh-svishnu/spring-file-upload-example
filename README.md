# Spring Boot File Upload Example 📁

This project demonstrates two different approaches to handling file uploads in Spring Boot application with both Multipart and Stream-based file upload mechanisms.

## Overview 🌟

The project implements two distinct file upload mechanisms:
1. Spring's built-in Multipart support
2. Apache Commons FileUpload (Stream-based approach)

Each approach is implemented in a separate module to demonstrate clean architecture and separation of concerns.

## Project Structure 🏗️

```
file-upload-parent/
├── file-upload-multipart/    # Spring Multipart implementation
├── file-upload-stream/       # Apache Commons FileUpload implementation
└── file-upload-app/          # Main Spring Boot application
```

## Upload Approaches Compared 🔄

### 1. Spring Multipart Upload (`/api/multipart/upload`)

Uses Spring's built-in `MultipartFile` handling:

### 2. Stream-based Upload (`/api/stream/upload`)

Uses Apache Commons FileUpload:

- **Configuration**:
    - Requires Spring's **_multipart to be disabled or set to lazy resolution_**.
    - Uses Apache Commons FileUpload dependencies:
```xml
  <dependency>
      <groupId>org.apache.commons</groupId>
      <artifactId>commons-fileupload2-core</artifactId>
      <version>2.0.0-M2</version>
  </dependency>
  <dependency>
      <groupId>org.apache.commons</groupId>
      <artifactId>commons-fileupload2-jakarta-servlet6</artifactId>
      <version>2.0.0-M2</version>
  </dependency>
```

## API Endpoints 🛣️

### Multipart Upload
```http
POST http://localhost:8888/api/multipart/upload
```

### Stream Upload
```http
POST http://localhost:8888/api/stream/upload
```

Both endpoints accept multipart/form-data with file field(s).

## Testing with Postman 🧪

A Postman collection is included in the repository (`SpringFileUploadExample-PostmanCollection.json`).

Sample curl commands:

```bash
# For Multipart upload
curl -X POST -F "file=@/path/to/file.txt" http://localhost:8888/api/multipart/upload

# For Stream upload
curl -X POST -F "file=@/path/to/file.txt" http://localhost:8888/api/stream/upload
```

## Key Configurations ⚙️

### Tomcat Configuration
```yaml
server:
  tomcat:
    max-swallow-size: -1
```

### Spring Server Configuration to support both Multipart and Stream uploads 📄

```yaml
spring:
  servlet:
    multipart:
      max-file-size: -1
      max-request-size: -1
      resolve-lazily: true # Reduces memory usage by processing the multipart files only when accessed
```
The `resolve-lazily: true` setting ensures multipart files are only processed when getFile() is called, rather than eagerly parsing the entire multipart request. This is especially beneficial when handling large file uploads or when not all parts of the multipart request need to be processed.

#### Official Documentation about `resolve-lazily`
Default is "false", resolving the multipart elements immediately, throwing corresponding exceptions at the time of the resolveMultipart(jakarta.servlet.http.HttpServletRequest) call. Switch this to "true" for lazy multipart parsing, throwing parse exceptions once the application attempts to obtain multipart files or parameters.

### References 📚
1. https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/multipart/support/StandardServletMultipartResolver.html
2. https://github.com/spring-projects/spring-framework/blob/main/spring-web/src/main/java/org/springframework/web/multipart/support/StandardServletMultipartResolver.java
3. https://github.com/spring-projects/spring-framework/blob/main/spring-web/src/main/java/org/springframework/web/multipart/support/StandardMultipartHttpServletRequest.java
4. https://github.com/spring-projects/spring-framework/blob/main/spring-web/src/main/java/org/springframework/web/multipart/support/AbstractMultipartHttpServletRequest.java
5. https://github.com/apache/commons-fileupload/blob/master/commons-fileupload2-jakarta-servlet6/src/main/java/org/apache/commons/fileupload2/jakarta/servlet6/JakartaServletFileUpload.java
6. https://github.com/apache/commons-fileupload/blob/master/commons-fileupload2-core/src/main/java/org/apache/commons/fileupload2/core/AbstractFileUpload.java
7. https://stackoverflow.com/questions/49234757/cannot-get-spring-boot-to-lazily-resolve-a-multipart-file
8. https://stackoverflow.com/questions/38133381/disable-spring-boot-multipart-upload-by-controller
9. https://stackoverflow.com/questions/37870989/spring-how-to-stream-large-multipart-file-uploads-to-database-without-storing
10. https://stackoverflow.com/questions/32782026/springboot-large-streaming-file-upload-using-apache-commons-fileupload