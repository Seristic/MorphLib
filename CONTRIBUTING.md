# Contributing to MorphLib

Thank you for your interest in contributing to MorphLib! We welcome contributions from the community.

> **Note**: MorphLib is a **library mod** that provides data management for body proportions and morph states. It does not implement visual rendering or player model changes. Contributions should focus on the data layer, API improvements, and cross-platform compatibility.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Making Changes](#making-changes)
- [Testing](#testing)
- [Submitting Changes](#submitting-changes)
- [Reporting Issues](#reporting-issues)

## Code of Conduct

This project follows our [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you agree to abide by its terms.

## License Compliance

**Copyright (c) 2025 Seristic**

This project is licensed under the **SOLACE License** (Software Of Liberty And Community Equity). By contributing to this project, you agree to the following:

### 🏳️‍🌈 Preservation of Inclusive Content

This project celebrates diversity, equity, and inclusion, particularly supporting the LGBT community. All contributions **must**:

- ✅ **Preserve** any inclusive content, features, or references in their original intent and visibility
- ✅ **Respect** the inclusive values embedded in the codebase
- ❌ **Never remove, alter, or diminish** LGBT-supporting elements, pride-related content, or diversity features
- ❌ **Never submit** changes that conflict with our commitment to inclusivity

### 📜 License Requirements for Contributors

When contributing, you must:

- **Maintain Attribution**: Keep copyright notices and license headers intact
- **Non-Commercial Intent**: Understand that this software cannot be sold or directly commercialized
- **Follow License Terms**: Ensure your contributions comply with all SOLACE License terms
- **Respect Inclusive Values**: Uphold the project's commitment to diversity and equity

### Copyright Assignment

By submitting a contribution, you:

- Grant the project maintainer (Seristic) perpetual rights to use your contribution
- Confirm your contribution is original or properly attributed
- Agree your contribution will be licensed under the SOLACE License
- Acknowledge you have the right to submit the contribution

**For complete license terms, please read the [LICENSE](LICENSE) file.**

## Getting Started

### Prerequisites

Before you begin, ensure you have:

- **Java 21** (required for Minecraft 1.21.4)
- **Git** for version control
- **Gradle** (included via wrapper)
- **Minecraft development environment** (optional, for testing)

### Development Setup

1. **Fork the repository** on GitHub
2. **Clone your fork**:

   ```bash
   git clone https://github.com/YOUR_USERNAME/MorphLib.git
   cd MorphLib
   ```

3. **Set up upstream remote**:

   ```bash
   git remote add upstream https://github.com/Seristic/MorphLib.git
   ```

4. **Create a feature branch**:

   ```bash
   git checkout -b feature/your-feature-name
   ```

5. **Build the project**:

   ```bash
   ./gradlew build
   ```

6. **Run tests**:

   ```bash
   ./gradlew test
   ```

## Making Changes

### Code Style Guidelines

- **Java Conventions**: Follow standard Java naming conventions
- **Documentation**: Add JavaDoc for all public APIs
- **Logging**: Use the provided `ModLogger` for consistent logging
- **Error Handling**: Use appropriate exception handling and logging
- **Performance**: Consider performance implications of changes

### Architecture Guidelines

- **Platform Agnostic**: Keep platform-specific code in respective modules
- **Data Layer Only**: MorphLib provides data management, not rendering
- **Backward Compatibility**: Don't break existing APIs without good reason
- **Thread Safety**: Ensure thread-safe operations where necessary
- **Clean API**: Keep public APIs simple and well-documented

### File Structure

```text
MorphLib/
├── common/           # Shared code (data structures, API, cache)
│   └── src/main/java/com/seristic/morphlib/
├── neoforge/         # NeoForge-specific code (networking, events)
│   └── src/main/java/com/seristic/morphlib/neoforge/
├── fabric/           # Fabric-specific code (networking, events)
│   └── src/main/java/com/seristic/morphlib/fabric/
└── gradle/           # Build configuration
```

## Testing

### Unit Tests

Run unit tests for all modules:

```bash
./gradlew test
```

### Integration Tests

Test both platforms:

```bash
# Test NeoForge
./gradlew :neoforge:test

# Test Fabric
./gradlew :fabric:test
```

### Manual Testing

1. **Build the library**:

   ```bash
   ./gradlew build
   ```

2. **Run test environment**:

   ```bash
   # NeoForge
   ./gradlew :neoforge:runClient

   # Fabric
   ./gradlew :fabric:runClient
   ```

3. **Test with a dependent mod**:
   - Create a test mod that uses MorphLib API
   - Verify data storage and retrieval works correctly
   - Test client-server synchronization in multiplayer
   - Verify all MorphState fields are properly saved/loaded

## Submitting Changes

### Commit Guidelines

- **Clear Messages**: Write descriptive commit messages
- **Atomic Commits**: Each commit should be a single logical change
- **Reference Issues**: Use `Fixes #123` or `Closes #123` for issue references

Example commit messages:

```bash
feat: add sheep morph support
fix: resolve crash when morphing into chicken
docs: update API documentation for MorphState
```

### Pull Request Process

1. **Update your branch**:

   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. **Push your changes**:

   ```bash
   git push origin feature/your-feature-name
   ```

3. **Create a Pull Request**:

   - Go to your fork on GitHub
   - Click "New Pull Request"
   - Fill out the PR template
   - Reference any related issues

4. **PR Requirements**:
   - ✅ All tests pass
   - ✅ Code follows style guidelines
   - ✅ Documentation updated
   - ✅ Tested on both NeoForge and Fabric
   - ✅ No breaking changes without discussion

### PR Template

Please fill out the pull request template with:

- **Description**: What does this change?
- **Type**: Bug fix, feature, documentation, etc.
- **Breaking Changes**: Does this break existing APIs?
- **Testing**: How was this tested?
- **Screenshots**: If UI changes

## Reporting Issues

### Bug Reports

When reporting bugs, please include:

- **Minecraft Version**: 1.21.4
- **Mod Loader**: NeoForge/Fabric + version
- **MorphLib Version**: Check the mod jar filename
- **Steps to Reproduce**: Clear, numbered steps
- **Expected Behavior**: What should happen?
- **Actual Behavior**: What actually happens?
- **Logs**: Include relevant log excerpts
- **Screenshots**: If visual issues

### Feature Requests

For new features, please:

- **Describe the feature** clearly
- **Explain the use case** and why it's needed
- **Consider alternatives** you've thought about
- **Discuss implementation** if you have ideas

### Security Issues

For security-related issues, please see our [Security Policy](SECURITY.md).

## Additional Resources

- [API Documentation](https://github.com/Seristic/MorphLib/wiki)
- [Development Wiki](https://github.com/Seristic/MorphLib/wiki)
- [Discord Community](https://discord.gg/morphlib)

## Recognition

Contributors will be recognized in:

- The project's README.md
- Release notes
- GitHub's contributor insights

Thank you for contributing to MorphLib! 🎉
