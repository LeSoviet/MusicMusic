# MusicMusic Improvement Roadmap

This roadmap outlines key improvements to enhance the MusicMusic project based on our audit.

## Phase 1: Architecture & Code Quality (Estimated: 2 weeks)

### 1.1 Fix Architecture Violations
- [x] Remove UI logging from business logic classes
- [ ] Abstract platform-specific file choosers behind interfaces
- [x] Ensure consistent version management across configuration files

### 1.2 Eliminate Code Duplication
- [x] Create centralized utility function for time formatting
- [x] Remove custom FlowRow implementation and use Compose's built-in version
- [x] Consolidate shared utility functions

## Phase 2: Dependency & Version Management (Estimated: 3 days)

### 2.1 Update Dependencies
- [ ] Update VLCJ to latest stable version (4.9.x)
- [x] Update SLF4J to latest version
- [ ] Check and update JAudioTagger if newer versions exist
- [ ] Verify all Kotlin and Compose dependencies are up-to-date

### 2.2 Version Consistency
- [x] Align version numbers across gradle.properties and build.gradle.kts
- [x] Implement consistent version management strategy

## Phase 3: Testing Enhancement (Estimated: 2 weeks)

### 3.1 Expand Test Coverage
- [ ] Add UI component tests for AlbumGrid, PlayerControls, etc.
- [ ] Add ViewModel tests for LibraryViewModel and RadioViewModel
- [ ] Implement integration tests between layers

### 3.2 Test Infrastructure
- [ ] Set up continuous integration for automated testing
- [ ] Add code coverage reporting

## Phase 4: Performance & Polish (Estimated: 1 week)

### 4.1 Performance Optimizations
- [ ] Implement caching strategies for frequently accessed data
- [ ] Optimize image loading and caching
- [ ] Review and improve memory usage patterns

### 4.2 Code Quality Improvements
- [ ] Address any remaining code smells
- [ ] Improve error handling consistency
- [ ] Enhance logging strategy

## Success Metrics

- 90%+ test coverage on core components
- Elimination of all identified code duplication
- Consistent architecture adherence
- Up-to-date dependencies with no security vulnerabilities
- Improved build times and performance metrics

## Timeline

Total estimated time: 5 weeks

| Week | Focus Area |
|------|------------|
| 1-2 | Architecture & Code Quality |
| 3 | Dependency Management |
| 4 | Testing Enhancement |
| 5 | Performance & Polish |

## Priority Matrix

| Priority | Task | Reason |
|----------|------|--------|
| High | Fix architecture violations | Critical for maintainability |
| High | Eliminate code duplication | Reduces maintenance overhead |
| High | Update dependencies | Security and feature benefits |
| Medium | Expand test coverage | Improves reliability |
| Medium | Performance optimizations | Enhances user experience |