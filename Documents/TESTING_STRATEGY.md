# Testing Strategy & Coverage Requirements

## Quality Gates (MANDATORY)

**Minimum Test Coverage: 80%**

Coverage applies to:
- ✅ Business/domain logic
- ✅ Data parsing & normalization
- ✅ Critical infrastructure code
- ❌ Boilerplate (getters/setters, trivial methods)

**Any change reducing coverage below 80% is a FAILURE**

## Test Folder Structure

All tests located in:
```
/tests
├── unit
│   ├── domain
│   │   └── model/
│   ├── application
│   │   └── service/
│   └── infrastructure
│       └── overpass/
├── integration
│   └── service/
└── e2e
    └── api/
```

## Test Naming Conventions

### Unit Tests

Format: `{ClassName}Test.java`

Examples:
```
DifficultyTest.java
TrailNormalizerTest.java
TrailExportServiceTest.java
```

### Test Methods

Format: `{methodName}_should{ExpectedBehavior}_when{Condition}`

Examples:
```
void shouldInferEasyFromLowMetrics()
void shouldParseOSMCMarkingCorrectly()
void shouldHandleNullValuesGracefully()
void shouldThrowExceptionWhenValidationFails()
void shouldDeduplicateTrailsByOsmId()
```

## Test Categories

### Unit Tests (70% of all tests)

Test individual components in isolation.

**When to write:**
- Domain models (entities, value objects)
- Service methods with clear inputs/outputs
- Utility functions
- Enum logic

**Example - Difficulty.java:**

```java
@Test
void shouldInferHardFromHighMetrics() {
    Difficulty difficulty = Difficulty.inferFromMetrics(2000, 28.0);
    assertEquals(Difficulty.HARD, difficulty);
}
```

**Characteristics:**
- No database required
- No external API calls
- Use mocks for dependencies
- Fast execution (< 100ms each)

### Integration Tests (20% of all tests)

Test multiple components working together.

**When to write:**
- Service orchestration (OSMIngestionService)
- Database queries
- API endpoint combinations

**Example - OSMIngestionService:**

```java
@Test
void shouldIngestAndPersistTrailsSuccessfully() {
    // Mock Overpass API
    when(overpassApiClient.queryBucegiHikingRoutes())
        .thenReturn(mockRelations);

    // Call service
    OSMIngestionService.IngestionResult result =
        ingestionService.ingestBucegiTrails();

    // Verify persistence
    verify(trailRepository, times(3)).save(any(Trail.class));
}
```

**Characteristics:**
- May use mocks for external services
- Test real repository interactions
- Medium execution time (100ms - 1s)

### End-to-End Tests (10% of all tests)

Test full API workflows.

**When to write:**
- Complete ingestion pipelines
- API endpoint chains
- Real database scenarios

**Example:**

```java
@Test
void shouldCompleteTrailIngestionWorkflow() {
    // Start with real Overpass data
    // Process through all services
    // Verify in database
}
```

## Coverage Calculation

### What's Measured

```
Coverage % = (Lines executed) / (Total lines of code) × 100
```

### What's Included

✅ Domain models
✅ Service methods
✅ Repository queries
✅ Exception handling

### What's Excluded

❌ Getters/setters
❌ Boilerplate constructors
❌ Spring annotations (auto-configured)
❌ IDE-generated code

### Coverage Calculation Example

**Trail.java:**
```
Total lines: 200
Executable lines: 180  (excludes getters/setters)
Covered lines: 150

Coverage = 150/180 = 83.3% ✓
```

## Running Tests

### Run All Tests

```bash
mvn test
```

### Run Specific Test Class

```bash
mvn test -Dtest=DifficultyTest
mvn test -Dtest=TrailNormalizerTest
```

### Run with Coverage Report

```bash
mvn clean test jacoco:report
```

Reports generated:
```
target/site/jacoco/index.html
```

### Run Specific Category

```bash
# Unit tests only
mvn test -Dgroups=unit

# Integration tests
mvn test -Dgroups=integration

# Exclude slow tests
mvn test -DexcludedGroups=slow
```

## Continuous Integration

### CI Pipeline

GitLab CI runs on every commit:

1. **Compile** - `mvn clean compile`
2. **Unit Tests** - `mvn test`
3. **Coverage** - `mvn jacoco:report`
4. **Validate** - Coverage ≥ 80%?
5. **Build** - `mvn package`

### Pipeline Failure Conditions

Pipeline **fails if:**
- ❌ Tests don't compile
- ❌ Any test fails
- ❌ Coverage < 80%
- ❌ Code smells detected

### CI Configuration (.gitlab-ci.yml)

```yaml
test:
  stage: test
  script:
    - mvn clean test jacoco:report
    - |
      COVERAGE=$(grep -oP '(?<=>)[^<]*(?=%)' \
        target/site/jacoco/index.html | head -1)
      if (( $(echo "$COVERAGE < 80" | bc -l) )); then
        echo "Coverage ${COVERAGE}% below 80%"
        exit 1
      fi

coverage:
  stage: test
  script:
    - mvn jacoco:report
  artifacts:
    reports:
      coverage_report:
        coverage_format: cobertura
        path: target/site/jacoco/cobertura-coverage.xml
```

## Test Coverage by Module

### Domain Models (Target: 90%)

| Class | Methods | Coverage |
|-------|---------|----------|
| Trail | 30 | 95% |
| Waypoint | 20 | 95% |
| Difficulty | 8 | 100% |
| TrailMarking | 15 | 90% |
| TrailSegment | 18 | 85% |

### Application Services (Target: 85%)

| Class | Methods | Coverage |
|-------|---------|----------|
| TrailNormalizer | 12 | 90% |
| OSMIngestionService | 8 | 85% |
| TrailExportService | 6 | 80% |

### Infrastructure (Target: 75%)

| Class | Methods | Coverage |
|-------|---------|----------|
| OverpassApiClient | 7 | 80% |
| TrailRepository | 6 | 70% |

## Regression Testing

### Regression Test Rule

**Every bug fix MUST include a regression test**

### Example

**Bug:** Trail normalization crashes on null OSM ID

**Test to add:**

```java
@Test
void shouldNormalizeTrailWithNullOsmId() {
    OverpassRelation relation = new OverpassRelation(
        null,  // osmId = null (was causing crash)
        "Trail", "hiking", null, null, null, null, null, null,
        new ArrayList<>(), coordinates
    );

    Trail trail = normalizer.normalizeToDomain(relation);

    assertNotNull(trail);
    assertNull(trail.getOsmId());
}
```

## Test Dependencies

### Unit Testing Framework

```gradle
testImplementation 'org.junit.jupiter:junit-jupiter:5.9.3'
testImplementation 'org.mockito:mockito-core:5.3.1'
testImplementation 'org.mockito:mockito-junit-jupiter:5.3.1'
testImplementation 'org.hamcrest:hamcrest:2.2'
```

### Code Coverage

```gradle
plugins {
    id 'jacoco'
}

jacoco {
    toolVersion = "0.8.10"
}

jacocoTestReport {
    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.collect {
            fileTree(dir: it, exclude: [
                '**/dto/**',
                '**/config/**',
            ])
        }))
    }
}
```

## Mocking Best Practices

### Mock External Services

```java
@Mock
private OverpassApiClient overpassApiClient;

@Mock
private TrailRepository trailRepository;

@BeforeEach
void setUp() {
    MockitoAnnotations.openMocks(this);
}
```

### Don't Mock Domain Logic

```java
// ✓ GOOD - Mock external dependency
when(overpassApiClient.queryBucegiHikingRoutes())
    .thenReturn(mockRelations);

// ✗ BAD - Mock business logic
TrailNormalizer normalizer = mock(TrailNormalizer.class);
```

## Test Data

### Use Test Builders

```java
private Trail createSimpleTrail() {
    Trail trail = new Trail();
    trail.setId(UUID.randomUUID());
    trail.setName("Test Trail");
    trail.setDistance(10.0);
    trail.setDifficulty(Difficulty.EASY);
    return trail;
}
```

### Avoid Hard-coded Values

```java
// ✓ GOOD - Clear, reusable
coordinates.add(new Coordinate(25.54, 45.35, 1000));

// ✗ BAD - Magic numbers
coordinates.add(new Coordinate(25.54, 45.35, 1000));
```

## Test Isolation

### Each Test Must Be Independent

```java
// ✓ GOOD - Isolated
@Test
void testA() {
    service.create(trail1);
    assertEquals(1, service.count());
}

@Test
void testB() {
    // Doesn't depend on testA
    service.create(trail2);
    assertEquals(1, service.count());
}

// ✗ BAD - Dependent tests
@Test
void testA() {
    service.create(trail1);
}

@Test
void testB() {
    // Depends on testA creating trail1
    assertTrue(service.exists(trail1));
}
```

## Performance Tests

### Test Execution Time

- ✓ Unit tests: < 100ms each
- ✓ Integration tests: < 1s each
- ✓ All tests combined: < 2 minutes

### Slow Test Example

```java
@Tag("slow")
@Test
@Timeout(5)  // 5 second timeout
void shouldCompleteIngestionWithinTimeout() {
    // Long-running test
}

// Skip slow tests in quick runs
mvn test -DexcludedGroups=slow
```

## Monitoring Coverage

### Generate Coverage Report

```bash
mvn clean test jacoco:report
open target/site/jacoco/index.html
```

### View Coverage Trends

Using JaCoCo with CI/CD:

```
Week 1: 75% ⬆️
Week 2: 78% ⬆️
Week 3: 82% ⬆️
Week 4: 85% ✓ (Target reached)
```

## Coverage Goals by Phase

| Phase | Target | Deadline |
|-------|--------|----------|
| Initial Development | 60% | Sprint 1 |
| Beta Release | 75% | Sprint 2 |
| Production | 80% | Sprint 3 |
| Mature | 85% | Sprint 4+ |

## Test Reporting

### CI/CD Reports

After `mvn test jacoco:report`, reports available at:

1. **HTML Report**: `target/site/jacoco/index.html`
2. **CSV Report**: `target/site/jacoco/jacoco.csv`
3. **XML Report**: `target/jacoco.exec`

### Check Coverage Command

```bash
#!/bin/bash
COVERAGE=$(grep -oP '(?<=>)[^<]*(?=%)' target/site/jacoco/index.html | head -1)
if (( $(echo "$COVERAGE < 80" | bc -l) )); then
    echo "FAILURE: Coverage ${COVERAGE}% below 80%"
    exit 1
fi
echo "SUCCESS: Coverage ${COVERAGE}% >= 80%"
```

---

**For architecture details, see [ARCHITECTURE.md](ARCHITECTURE.md)**
**For startup instructions, see [STARTUP.md](STARTUP.md)**
