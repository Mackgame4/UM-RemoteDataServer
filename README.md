# UM-RemoteDataServer
🗂️ Course project for UM for SD in 2024/25

##### Relatorio Typst (depois adicionar à pasta "Relatorio" do github e adicionar o enunciado): https://typst.app/project/wkG3SSLaquiHfTKi6y6aZz

## Description
This repository contains the code and tools for the **RemoteDataServer** project, designed for the **UM for SD course** in 2024/25. The project focuses on building a server-client architecture and includes functionalities for benchmarking and graphing performance data.

## Prerequisites
Ensure that the following dependencies are installed:
- **Java 8+**: Required for compiling and running Java code.
- **Typst**: For compiling and watching reports.

Additionally, the project uses external libraries for graphing:
- **JFreeChart**: For benchmarking and generating graphs.
- **JCommon**: A required dependency for JFreeChart.

## File Structure
- `src/`: Contains the Java source files.
- `bin/`: The output directory where compiled `.class` files are stored.
- `lib/`: Contains external libraries (e.g., `jfreechart`, `jcommon`).
- `relatorio/`: Contains the Typst file for the final report.

### Usage

- **`all`**: Default target, builds the project by running `make build`.
  
- **`build`**: Compiles the Java files in the `src` directory into the `bin` directory.
  ```
  make build
  ```

- **`run`**: Runs the compiled `Main` class from the `bin` directory.
  ```
  make run
  ```

- **`dev`**: Compiles the Java files and runs the `Main` class in development mode (both build and run).
  ```
  make dev
  ```

- **`clean`**: Deletes all compiled files in the `bin` directory.
  ```
  make clean
  ```

- **`run-client`**: Runs the `Main` class in client mode.
  ```
  make run-client
  ```

- **`run-server`**: Runs the `Main` class in server mode.
  ```
  make run-server
  ```

- **`dev-client`**: Compiles the Java files and runs the client mode.
  ```
  make dev-client
  ```

- **`dev-server`**: Compiles the Java files and runs the server mode.
  ```
  make dev-server
  ```

- **`benchmark`**: Compiles and runs the benchmark test.
  ```
  make benchmark
  ```

- **`benchmark-graph`**: Compiles and runs the benchmarking graph generation using JFreeChart and JCommon libraries.
  ```
  make benchmark-graph
  ```

- **`relatorio`**: Compiles the report using Typst.
  ```
  make relatorio
  ```

- **`relatorio-watch`**: Watches for changes in the `relatorio.typ` file and recompiles it.
  ```
  make relatorio-watch
  ```

- **`relatorio-clean`**: Deletes the compiled report (`relatorio.pdf`).
  ```
  make relatorio-clean
  ```