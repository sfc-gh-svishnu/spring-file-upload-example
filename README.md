# Conclusion MultipartFile should not be used as it poses a memory/disk contention risk
This project found that [MultipartFile](https://docs.spring.io/spring-framework/docs/3.2.8.RELEASE_to_3.2.9.RELEASE/Spring%20Framework%203.2.9.RELEASE/org/springframework/web/multipart/MultipartFile.html) cannot be used without spring eagerly loading the entire contents in memory/disk.

To avoid I/O or memory contention in the servers, the right approach is to do this via the underlying `HttpServletRequest` using Apache Commons FileUpload utility.
* [servlet.multipart.resolve-lazily](https://github.com/snowflakedb/snapps/blob/406fc39031be9d6e87bf96da4fad35dd6d8ffb84/src/coldbrew/session-manager/src/main/resources/application.yaml#L255) configuration must be `True`.

Ideally we would encapsulate this in a [custom MultipartResolver](https://medium.com/@AlexanderObregon/breaking-down-the-multipart-upload-process-within-spring-boot-9ad27fb4138f) however doing so generically is not simple.
* **How to avoid reading the file bytes before the other smaller input parts?**
  * The smaller inputs might be required to process the file bytes, which should be streamed instead of requiring reading the whole contents.
  * You are at the mercy of how the client sends those parts, if they happen to put the file bytes first, and the smaller parts second, there might be NO WAY to process the file bytes in a streaming fashion without holding them somewhere first.

If you are facing a multi part scenario, consider moving away from `multipart/form-data` input into a `application/octet-stream` in the body with the rest of simple inputs passed as url params.
* Such a contract by design would ALWAYS allow processing the file bytes in streaming fashion as they are being received over the network.


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
1. https://medium.com/@AlexanderObregon/breaking-down-the-multipart-upload-process-within-spring-boot-9ad27fb4138f
1. https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/multipart/support/StandardServletMultipartResolver.html
1. https://github.com/spring-projects/spring-framework/blob/main/spring-web/src/main/java/org/springframework/web/multipart/support/StandardServletMultipartResolver.java
1. https://github.com/spring-projects/spring-framework/blob/main/spring-web/src/main/java/org/springframework/web/multipart/support/StandardMultipartHttpServletRequest.java
1. https://github.com/spring-projects/spring-framework/blob/main/spring-web/src/main/java/org/springframework/web/multipart/support/AbstractMultipartHttpServletRequest.java
1. https://github.com/apache/commons-fileupload/blob/master/commons-fileupload2-jakarta-servlet6/src/main/java/org/apache/commons/fileupload2/jakarta/servlet6/JakartaServletFileUpload.java
1. https://github.com/apache/commons-fileupload/blob/master/commons-fileupload2-core/src/main/java/org/apache/commons/fileupload2/core/AbstractFileUpload.java
1. https://stackoverflow.com/questions/49234757/cannot-get-spring-boot-to-lazily-resolve-a-multipart-file
1. https://stackoverflow.com/questions/38133381/disable-spring-boot-multipart-upload-by-controller
1. https://stackoverflow.com/questions/37870989/spring-how-to-stream-large-multipart-file-uploads-to-database-without-storing
1. https://stackoverflow.com/questions/32782026/springboot-large-streaming-file-upload-using-apache-commons-fileupload