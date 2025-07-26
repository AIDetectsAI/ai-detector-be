# Ai Detector 

## Description
This is a Java-based project using Spring Boot and Maven. It provides a foundation for building modern web applications and services.

## Features
- Spring Boot framework for rapid development
- Maven for dependency management and build automation
- Customizable project structure

## Getting Started

### Prerequisites
- Java 24 or higher
- Maven 3.8 or higher

### Installation
1. Clone the repository:
   ```bash
   git clone <repository-url>

### Running as container
Beware it utilizes maven with spring-boot:build-image which uses Cloud Native Buildpacks to generate the image. You need to have java 24 version declared in maven as the used command depends on it.
To run backend on port 8080 on your local machine you simply need to run a script depending on your machine OS.

Windows machine:
```ps1
./run-be.ps1
```

Linux machine (after setting executable to script):
```sh
sh run-be.sh
```

