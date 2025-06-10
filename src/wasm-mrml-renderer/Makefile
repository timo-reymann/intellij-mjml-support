.PHONY: help

SHELL := /bin/bash

help: ## Display this help page
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[33m%-30s\033[0m %s\n", $$1, $$2}'

build-release: ## Build release artifact
	cargo build --target wasm32-wasip1 --release

build-debug: ## Build debug artifact
	cargo build --target wasm32-wasip1

build: build-release build-debug ## Build all configurations

copy-renderer: ## Copy renderer to plugin resources
	cp target/wasm32-wasip1/release/wasm_mrml_renderer.wasm ../main/resources/wasi/mrml-render.wasm
	cargo tree -i mrml | head -n1 | cut -d' ' -f2  > ../main/resources/wasi/mrml-render.version
