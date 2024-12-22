all: build

build:
	@javac -classpath src -d bin src/Main.java

run:
	@java -cp bin Main

dev:
	$(MAKE) build
	$(MAKE) run

clean:
	@rm -rf bin/*

run-client:
	@java -cp bin Main client

run-server:
	@java -cp bin Main server

dev-client:
	$(MAKE) build
	$(MAKE) run-client

dev-server:
	$(MAKE) build
	$(MAKE) run-server

benchmark:
	@javac -classpath src -d bin src/Benchmark/B_Main.java
	@java -cp bin Benchmark.B_Main

relatorio: relatorio_build

relatorio-build:
	@echo "Compilando relatorio..."
	@typst compile relatorio/relatorio.typ

relatorio-watch:
	@echo "Assistindo alteracoes no relatorio..."
	@typst watch relatorio/relatorio.typ

relatorio-clean:
	@rm -rf relatorio/relatorio.pdf