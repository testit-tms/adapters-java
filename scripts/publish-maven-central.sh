#!/bin/bash

# Script to publish all modules to Maven Central using JReleaser
# Usage: ./scripts/publish-maven-central.sh <version>
# Example: ./scripts/publish-maven-central.sh 2.7.1-TMS-CLOUD
# Or in GitHub Actions: PROJECT_VERSION will be set automatically

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Determine version: from parameter, environment variable, or error
if [ -n "$1" ]; then
    VERSION="$1"
    print_status "Using version from parameter: $VERSION"
elif [ -n "$PROJECT_VERSION" ]; then
    VERSION="$PROJECT_VERSION"
    print_status "Using version from PROJECT_VERSION environment variable: $VERSION"
else
    print_error "Version is required!"
    echo "Usage: $0 <version>"
    echo "Example: $0 2.7.1-TMS-CLOUD"
    echo "Or set PROJECT_VERSION environment variable in CI/CD"
    exit 1
fi

print_status "Starting Maven Central publishing process for version: $VERSION"

# Check if .env file exists (skip in CI/CD)
if [ ! -f ".env" ] && [ -z "$CI" ]; then
    print_error ".env file not found!"
    echo "Please create .env file with the following variables:"
    echo "JRELEASER_MAVENCENTRAL_USERNAME=your_username"
    echo "JRELEASER_MAVENCENTRAL_PASSWORD=your_password"
    echo "JRELEASER_GPG_PASSPHRASE=your_gpg_passphrase"
    echo "JRELEASER_GPG_SECRET_KEY=your_secret_key"
    echo "JRELEASER_GPG_PUBLIC_KEY=your_public_key"
    exit 1
fi

# Load environment variables (only if .env exists)
if [ -f ".env" ]; then
    print_status "Loading environment variables from .env file..."
    source .env
fi

# Validate required environment variables
required_vars=(
    "JRELEASER_MAVENCENTRAL_USERNAME"
    "JRELEASER_MAVENCENTRAL_PASSWORD"
    "JRELEASER_GPG_PASSPHRASE"
    "JRELEASER_GPG_SECRET_KEY"
    "JRELEASER_GPG_PUBLIC_KEY"
)

for var in "${required_vars[@]}"; do
    if [ -z "${!var}" ]; then
        print_error "Environment variable $var is not set!"
        exit 1
    fi
done

print_success "All required environment variables are set"

# Update version in jreleaser.yml
print_status "Updating version to $VERSION in jreleaser.yml..."
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    sed -i '' "s/version: .*/version: \"$VERSION\"/" jreleaser.yml
else
    # Linux/Windows with Git Bash
    sed -i "s/version: .*/version: \"$VERSION\"/" jreleaser.yml
fi

print_success "Version updated to $VERSION"

# Clean previous builds
print_status "Cleaning previous builds..."
./gradlew clean

# Stage all modules (with signing disabled for Gradle)
print_status "Staging all modules for JReleaser deployment..."
./gradlew jreleaserStage -DdisableSign=true

print_success "All modules staged successfully"

# Validate JReleaser configuration
print_status "Validating JReleaser configuration..."
jreleaser config --assembly


# Exit without deploy flag
if [ "$DEPLOY" != "true" ]; then
    print_status "Trying dry-run deployment..."
    jreleaser deploy --dry-run
    exit 0
fi

# Deploy to Maven Central using JReleaser (with signing enabled)
print_status "Deploying to Maven Central using JReleaser..."
print_warning "This will publish version $VERSION to Maven Central Portal"

jreleaser deploy

print_success "Successfully published version $VERSION to Maven Central!"
print_status "Check the deployment status at: https://central.sonatype.com/"

# Summary
echo
echo "=== DEPLOYMENT SUMMARY ==="
echo "Version: $VERSION"
echo "Modules published: 10"
echo "- testit-adapter-cucumber4"
echo "- testit-adapter-cucumber5"
echo "- testit-adapter-cucumber6"
echo "- testit-adapter-cucumber7"
echo "- testit-adapter-jbehave"
echo "- testit-adapter-junit4"
echo "- testit-adapter-junit5"
echo "- testit-adapter-selenide"
echo "- testit-adapter-testng"
echo "- testit-java-commons"
echo
print_success "Deployment completed successfully!" 