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

relatorio: relatorio_build

relatorio_build:
	@echo "Compilando relatorio..."
	@typst compile relatorio/relatorio.typ

relatorio_watch:
	@echo "Assistindo alteracoes no relatorio..."
	@typst watch relatorio/relatorio.typ

relatorio_clean:
	@rm -rf relatorio/relatorio.pdf