# Spring Hedera Application

This project is a Spring Boot application that integrates with the Hedera network.

## Prerequisites

To build and run this application, you need:

*   Java Development Kit (JDK) 17 or higher, I 
*   Apache Maven 3.6.0 or higher

## Building the Application

To build the application, navigate to the project root directory and run the following Maven command:

```bash
mvn clean install
```

This will compile the code, run tests, and package the application into a JAR file in the `target/` directory.

## Running the Application

After building, you can run the application using the Spring Boot Maven plugin:

```bash
mvn spring-boot:run
```

The application will start and connect to the Hedera network based on the configuration in `src/main/resources/application.yml`.

## Testing the Application

To run the unit and integration tests, use the following Maven command:

```bash
mvn test
```

## Configuration

The Hedera network, Azure OpenAI configuration, and other application settings are located in `src/main/resources/application.yml`. You **must** update this file with your actual `API_KEY`, `ENDPOINT`, `ACCOUNT_ID`, and `PRIVATE_KEY` values, replacing the placeholders (e.g., `${API_KEY}`).
