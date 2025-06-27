# Publishing to Maven Central

This document describes how to publish TestIT Java Adapters to Maven Central using JReleaser.

## Prerequisites

1. **JReleaser CLI** installed and available in PATH
   ```bash
   # Install using SDK manager or download from https://jreleaser.org/guide/latest/tools/jreleaser-cli.html
   sdk install jreleaser
   ```

2. **Environment Variables** configured in `.env` file:
   ```bash
   # Copy template and fill in your credentials
   cp env.example .env
   # Edit .env with your actual values
   ```

3. **Required credentials:**
   - Maven Central Portal username/password
   - GPG key pair for signing
   - GPG passphrase

## Publishing Process

### 1. Quick Publish
```bash
PROJECT_VERSION="2.7.1-TEST" ./scripts/publish-maven-central.sh
```

### 2. Manual Steps

If you prefer to run steps manually:

```bash
# 1. Load environment variables
source .env

# 2. Update version in jreleaser.yml
sed -i "s/version: .*/version: '2.7.2-TMS-CLOUD'/" jreleaser.yml

# 3. Clean and stage all modules (with Gradle signing disabled)
./gradlew clean
./gradlew jreleaserStage -DdisableSign=true

# 4. Validate configuration
jreleaser config --assembly

# 5. Deploy to Maven Central (with JReleaser signing enabled)
jreleaser deploy
```

## Environment Variables

Create `.env` file with the following variables:

```bash
# Maven Central Portal credentials
JRELEASER_MAVENCENTRAL_USERNAME=your_sonatype_username
JRELEASER_MAVENCENTRAL_PASSWORD=your_sonatype_password

# GPG signing configuration
JRELEASER_GPG_PASSPHRASE=your_gpg_passphrase
JRELEASER_GPG_SECRET_KEY=your_base64_encoded_secret_key
JRELEASER_GPG_PUBLIC_KEY=your_base64_encoded_public_key
```

### GPG Key Setup

To get your base64 encoded keys:

```bash
# Export public key
gpg --armor --export your-key-id | base64 -w 0

# Export secret key
gpg --armor --export-secret-keys your-key-id | base64 -w 0
```

## Modules Published

The following modules will be published:

1. `testit-java-commons` - Core library
2. `testit-adapter-junit4` - JUnit 4 adapter
3. `testit-adapter-junit5` - JUnit 5 adapter
4. `testit-adapter-testng` - TestNG adapter
5. `testit-adapter-cucumber4` - Cucumber 4 adapter
6. `testit-adapter-cucumber5` - Cucumber 5 adapter
7. `testit-adapter-cucumber6` - Cucumber 6 adapter
8. `testit-adapter-cucumber7` - Cucumber 7 adapter
9. `testit-adapter-jbehave` - JBehave adapter
10. `testit-adapter-selenide` - Selenide adapter

## Troubleshooting

### Signing Issues
- Ensure GPG keys are properly base64 encoded
- Verify GPG passphrase is correct
- Check that signing is disabled for Gradle (`-DdisableSign=true`)

### Staging Issues
- Run `./gradlew clean` before staging
- Check that all modules build successfully
- Verify staging directories exist in `*/build/staging-deploy`

### Deployment Issues
- Verify Maven Central credentials
- Check network connectivity
- Review JReleaser logs in `out/jreleaser/`

## Configuration

The deployment is configured in `jreleaser.yml`:
- **Target**: Maven Central Portal Publisher API
- **Signing**: Always enabled with armored output
- **Retry**: 20s delay, 60 max retries
- **GitHub Releases**: Disabled (Maven Central only)

## GitHub Actions Integration

### Automatic Publishing on Tag

The repository includes a GitHub Actions workflow that automatically publishes to Maven Central when you create a version tag:

```bash
# Create and push a version tag
git tag v2.7.1-TMS-CLOUD
git push origin v2.7.1-TMS-CLOUD
```

The workflow will:
1. Extract version from tag (removes `v` prefix)
2. Set `PROJECT_VERSION` environment variable
3. Build and publish all modules
4. Create a GitHub Release with artifact links

### Manual Publishing via GitHub Actions

You can also trigger publishing manually from GitHub Actions UI:
1. Go to **Actions** tab in GitHub
2. Select **Publish to Maven Central** workflow
3. Click **Run workflow**
4. Enter the version to publish

### Required GitHub Secrets

Set these secrets in your GitHub repository settings:

```
MAVEN_CENTRAL_USERNAME=your_sonatype_username
MAVEN_CENTRAL_PASSWORD=your_sonatype_password
GPG_PASSPHRASE=your_gpg_passphrase
GPG_SECRET_KEY=your_base64_encoded_secret_key
GPG_PUBLIC_KEY=your_base64_encoded_public_key
```

### Version Synchronization

The system supports multiple ways to set the version:

1. **GitHub Actions Tag** (recommended): `PROJECT_VERSION` is automatically extracted from git tag
2. **Environment Variable**: Set `PROJECT_VERSION` in CI/CD
3. **Script Parameter**: Pass version as argument to the script

Priority: Parameter > Environment Variable > Error

## Local CI/CD Integration

For other CI/CD systems:

```bash
# Set environment variables in CI/CD system
export PROJECT_VERSION=$VERSION
export JRELEASER_MAVENCENTRAL_USERNAME=$MAVEN_CENTRAL_USERNAME
export JRELEASER_MAVENCENTRAL_PASSWORD=$MAVEN_CENTRAL_PASSWORD
export JRELEASER_GPG_PASSPHRASE=$GPG_PASSPHRASE
export JRELEASER_GPG_SECRET_KEY=$GPG_SECRET_KEY
export JRELEASER_GPG_PUBLIC_KEY=$GPG_PUBLIC_KEY
export CI=true

# Run publish script (version will be taken from PROJECT_VERSION)
./scripts/publish-maven-central.sh
``` 