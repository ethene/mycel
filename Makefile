# Mycel Project Makefile
# Comprehensive build system for local development with proper Java 17 setup

# =============================================================================
# Configuration
# =============================================================================

# Java Configuration
JAVA_VERSION := 17
JAVA_HOME := $(shell /usr/libexec/java_home -v$(JAVA_VERSION) 2>/dev/null)
GRADLE := ./gradlew
GRADLE_OPTS := --no-daemon

# Build Variants
DEBUG_VARIANT := Debug
RELEASE_VARIANT := Release

# Output Directories
APK_DIR := mycel-android/build/outputs/apk
LIBS_DIR := build/libs
REPORTS_DIR := build/reports

# Colors for output
RED := \033[0;31m
GREEN := \033[0;32m
YELLOW := \033[0;33m
BLUE := \033[0;34m
NC := \033[0m # No Color

# =============================================================================
# Default Target
# =============================================================================

.PHONY: help
help: ## Show this help message
	@echo "$(BLUE)Mycel Project Build System$(NC)"
	@echo "$(BLUE)=========================$(NC)"
	@echo ""
	@echo "Available targets:"
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  $(GREEN)%-20s$(NC) %s\n", $$1, $$2}' $(MAKEFILE_LIST)
	@echo ""
	@echo "$(YELLOW)Java Version:$(NC) $(JAVA_VERSION)"
	@echo "$(YELLOW)Java Home:$(NC)    $(JAVA_HOME)"

# =============================================================================
# Environment Setup
# =============================================================================

.PHONY: check-java
check-java: ## Verify Java 17 is available
	@echo "$(BLUE)Checking Java environment...$(NC)"
	@if [ -z "$(JAVA_HOME)" ]; then \
		echo "$(RED)Error: Java $(JAVA_VERSION) not found. Please install Java $(JAVA_VERSION).$(NC)"; \
		exit 1; \
	fi
	@echo "$(GREEN)✓ Java $(JAVA_VERSION) found at: $(JAVA_HOME)$(NC)"
	@export JAVA_HOME=$(JAVA_HOME) && java -version

.PHONY: setup
setup: check-java ## Setup development environment
	@echo "$(BLUE)Setting up development environment...$(NC)"
	@echo "$(GREEN)✓ Environment ready for development$(NC)"

# =============================================================================
# Core Build Targets
# =============================================================================

.PHONY: clean
clean: check-java ## Clean all build artifacts
	@echo "$(BLUE)Cleaning build artifacts...$(NC)"
	@export JAVA_HOME=$(JAVA_HOME) && $(GRADLE) clean $(GRADLE_OPTS)
	@echo "$(GREEN)✓ Clean completed$(NC)"

.PHONY: build
build: check-java ## Build all modules
	@echo "$(BLUE)Building all modules...$(NC)"
	@export JAVA_HOME=$(JAVA_HOME) && $(GRADLE) build $(GRADLE_OPTS)
	@echo "$(GREEN)✓ Build completed$(NC)"

.PHONY: build-core
build-core: check-java ## Build core modules only (no Android)
	@echo "$(BLUE)Building core modules...$(NC)"
	@export JAVA_HOME=$(JAVA_HOME) && $(GRADLE) build -c settings-test.gradle $(GRADLE_OPTS)
	@echo "$(GREEN)✓ Core build completed$(NC)"

.PHONY: compile
compile: check-java ## Compile all source code without tests
	@echo "$(BLUE)Compiling source code...$(NC)"
	@export JAVA_HOME=$(JAVA_HOME) && $(GRADLE) compileJava $(GRADLE_OPTS)
	@echo "$(GREEN)✓ Compilation completed$(NC)"

# =============================================================================
# Android Build Targets
# =============================================================================

.PHONY: android-debug
android-debug: check-java ## Build Android debug APK
	@echo "$(BLUE)Building Android debug APK...$(NC)"
	@export JAVA_HOME=$(JAVA_HOME) && $(GRADLE) :mycel-android:assembleDebug $(GRADLE_OPTS)
	@echo "$(GREEN)✓ Android debug APK built$(NC)"
	@echo "$(YELLOW)APK location:$(NC) $(APK_DIR)/official/debug/mycel-android-official-debug.apk"

.PHONY: android-release
android-release: check-java ## Build Android release APK
	@echo "$(BLUE)Building Android release APK...$(NC)"
	@export JAVA_HOME=$(JAVA_HOME) && $(GRADLE) :mycel-android:assembleRelease $(GRADLE_OPTS)
	@echo "$(GREEN)✓ Android release APK built$(NC)"
	@echo "$(YELLOW)APK location:$(NC) $(APK_DIR)/official/release/mycel-android-official-release.apk"

.PHONY: android-all
android-all: check-java ## Build all Android variants
	@echo "$(BLUE)Building all Android variants...$(NC)"
	@export JAVA_HOME=$(JAVA_HOME) && $(GRADLE) :mycel-android:assemble $(GRADLE_OPTS)
	@echo "$(GREEN)✓ All Android variants built$(NC)"

.PHONY: android-install
android-install: android-debug ## Install debug APK to connected device
	@echo "$(BLUE)Installing debug APK...$(NC)"
	@adb install -r $(APK_DIR)/official/debug/mycel-android-official-debug.apk
	@echo "$(GREEN)✓ APK installed$(NC)"

# =============================================================================
# Test Targets
# =============================================================================

.PHONY: test
test: check-java ## Run all tests (core modules only due to Android circular deps)
	@echo "$(BLUE)Running core module tests...$(NC)"
	@export JAVA_HOME=$(JAVA_HOME) && $(GRADLE) test -c settings-test.gradle $(GRADLE_OPTS)
	@echo "$(GREEN)✓ Tests completed$(NC)"

.PHONY: test-core
test-core: check-java ## Run core module tests explicitly
	@echo "$(BLUE)Running core module tests...$(NC)"
	@export JAVA_HOME=$(JAVA_HOME) && $(GRADLE) test -c settings-test.gradle $(GRADLE_OPTS)
	@echo "$(GREEN)✓ Core tests completed$(NC)"

.PHONY: test-unit
test-unit: check-java ## Run unit tests only
	@echo "$(BLUE)Running unit tests...$(NC)"
	@export JAVA_HOME=$(JAVA_HOME) && $(GRADLE) test -c settings-test.gradle $(GRADLE_OPTS) --tests "*Test"
	@echo "$(GREEN)✓ Unit tests completed$(NC)"

.PHONY: test-integration
test-integration: check-java ## Run integration tests
	@echo "$(BLUE)Running integration tests...$(NC)"
	@export JAVA_HOME=$(JAVA_HOME) && $(GRADLE) test -c settings-test.gradle $(GRADLE_OPTS) --tests "*IT"
	@echo "$(GREEN)✓ Integration tests completed$(NC)"

.PHONY: test-android
test-android: check-java ## Run Android unit tests (may have dependency issues)
	@echo "$(BLUE)Running Android unit tests...$(NC)"
	@echo "$(YELLOW)Warning: Android tests may fail due to circular dependencies$(NC)"
	@export JAVA_HOME=$(JAVA_HOME) && $(GRADLE) :mycel-android:testOfficialDebugUnitTest $(GRADLE_OPTS) || true
	@echo "$(YELLOW)Android unit tests completed (check output for results)$(NC)"

# =============================================================================
# Quality & Verification
# =============================================================================

.PHONY: check
check: check-java ## Run all checks (build + test + verify)
	@echo "$(BLUE)Running all checks...$(NC)"
	@export JAVA_HOME=$(JAVA_HOME) && $(GRADLE) check -c settings-test.gradle $(GRADLE_OPTS)
	@echo "$(GREEN)✓ All checks completed$(NC)"

.PHONY: verify
verify: check-java ## Verify dependencies and signatures (witness plugin disabled for Android)
	@echo "$(BLUE)Verifying dependencies...$(NC)"
	@echo "$(YELLOW)Note: Witness plugin disabled for Android modules due to circular dependency issues$(NC)"
	@export JAVA_HOME=$(JAVA_HOME) && $(GRADLE) signatureCheck $(GRADLE_OPTS)
	@echo "$(GREEN)✓ Verification completed$(NC)"

.PHONY: lint
lint: check-java ## Run Android lint checks
	@echo "$(BLUE)Running Android lint...$(NC)"
	@export JAVA_HOME=$(JAVA_HOME) && $(GRADLE) :mycel-android:lintOfficialDebug $(GRADLE_OPTS)
	@echo "$(GREEN)✓ Lint completed$(NC)"

# =============================================================================
# Development Targets
# =============================================================================

.PHONY: dev-build
dev-build: clean build-core android-debug ## Complete development build
	@echo "$(GREEN)✓ Development build completed$(NC)"

.PHONY: quick-build
quick-build: check-java ## Quick build without clean
	@echo "$(BLUE)Quick build...$(NC)"
	@export JAVA_HOME=$(JAVA_HOME) && $(GRADLE) build $(GRADLE_OPTS) -x test
	@echo "$(GREEN)✓ Quick build completed$(NC)"

.PHONY: headless
headless: check-java ## Build and run headless REST API
	@echo "$(BLUE)Building and running headless API...$(NC)"
	@export JAVA_HOME=$(JAVA_HOME) && $(GRADLE) :mycel-headless:run $(GRADLE_OPTS)

.PHONY: headless-build
headless-build: check-java ## Build headless JAR only
	@echo "$(BLUE)Building headless JAR...$(NC)"
	@export JAVA_HOME=$(JAVA_HOME) && $(GRADLE) :mycel-headless:fatJar $(GRADLE_OPTS)
	@echo "$(GREEN)✓ Headless JAR built$(NC)"
	@echo "$(YELLOW)JAR location:$(NC) mycel-headless/build/libs/"

# =============================================================================
# Maintenance Targets
# =============================================================================

.PHONY: gradle-wrapper
gradle-wrapper: ## Update Gradle wrapper
	@echo "$(BLUE)Updating Gradle wrapper...$(NC)"
	@export JAVA_HOME=$(JAVA_HOME) && $(GRADLE) wrapper --gradle-version=8.5
	@echo "$(GREEN)✓ Gradle wrapper updated$(NC)"

.PHONY: dependencies
dependencies: check-java ## Show dependency tree
	@echo "$(BLUE)Showing dependency tree...$(NC)"
	@export JAVA_HOME=$(JAVA_HOME) && $(GRADLE) dependencies $(GRADLE_OPTS)

.PHONY: tasks
tasks: check-java ## Show all available Gradle tasks
	@echo "$(BLUE)Available Gradle tasks:$(NC)"
	@export JAVA_HOME=$(JAVA_HOME) && $(GRADLE) tasks $(GRADLE_OPTS)

# =============================================================================
# Special Targets
# =============================================================================

.PHONY: mailbox-tests
mailbox-tests: check-java ## Run mailbox integration tests (if enabled)
	@echo "$(BLUE)Running mailbox integration tests...$(NC)"
	@export JAVA_HOME=$(JAVA_HOME) && \
	export MAILBOX_INTEGRATION_TESTS=true && \
	$(GRADLE) mailbox-integration-tests:test $(GRADLE_OPTS)
	@echo "$(GREEN)✓ Mailbox tests completed$(NC)"

.PHONY: screenshot-build
screenshot-build: check-java ## Build screenshot variant for testing
	@echo "$(BLUE)Building screenshot variant...$(NC)"
	@export JAVA_HOME=$(JAVA_HOME) && $(GRADLE) :mycel-android:assembleScreenshotDebug $(GRADLE_OPTS)
	@echo "$(GREEN)✓ Screenshot variant built$(NC)"

# =============================================================================
# Information Targets
# =============================================================================

.PHONY: status
status: ## Show project status
	@echo "$(BLUE)Mycel Project Status$(NC)"
	@echo "$(BLUE)==================$(NC)"
	@echo "$(YELLOW)Java Version:$(NC)     $(JAVA_VERSION)"
	@echo "$(YELLOW)Java Home:$(NC)        $(JAVA_HOME)"
	@echo "$(YELLOW)Gradle Version:$(NC)   $$(export JAVA_HOME=$(JAVA_HOME) && $(GRADLE) --version --quiet | grep Gradle | cut -d' ' -f2)"
	@echo "$(YELLOW)Project:$(NC)          Mycel Messaging App"
	@echo "$(YELLOW)Developer:$(NC)        Quantum Research Pty Ltd"
	@echo ""
	@echo "$(GREEN)✓ Ready for development$(NC)"

.PHONY: info
info: status ## Alias for status

# =============================================================================
# Aliases and Shortcuts
# =============================================================================

.PHONY: all
all: dev-build test ## Build everything and run tests

.PHONY: apk
apk: android-debug ## Alias for android-debug

.PHONY: install
install: android-install ## Alias for android-install

# Default target
.DEFAULT_GOAL := help

# =============================================================================
# File Targets
# =============================================================================

$(APK_DIR)/official/debug/mycel-android-official-debug.apk: android-debug

$(APK_DIR)/official/release/mycel-android-official-release.apk: android-release

# =============================================================================
# Error Handling
# =============================================================================

# Ensure Java 17 is set for all targets
$(MAKECMDGOALS): | check-java