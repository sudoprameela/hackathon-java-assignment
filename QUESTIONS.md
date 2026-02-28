# Questions

Here are 2 questions related to the codebase. There's no right or wrong answer - we want to understand your reasoning.

## Question 1: API Specification Approaches

When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded everything directly. 

What are your thoughts on the pros and cons of each approach? Which would you choose and why?

**Answer:**

API Specification Approaches: OpenAPI YAML vs. Hand-Coded Endpoints

OpenAPI (Generated Code):
Pros:
- Single Source of Truth: The API contract is decoupled from the implementation, making it easier to share with frontend or mobile teams before backend implementation is complete.
- Client Generation: Simplifies frontend integration by auto-generating client SDKs.
- Reduced Drift: The generated code acts as a contract that the backend must fulfill, reducing the chance of documentation getting out of sync with actual behavior.
Cons:
- Steeper learning curve for managing OpenAPI definitions.
- Code generation tools may sometimes limit flexibility (e.g., specific framework support or custom annotations).

Hand-Coded Endpoints:
Pros:
- Faster initial development for simple or prototype services.
- Full control over framework-specific annotations, validation logic, and routing.
Cons:
- High risk of documentation drift. It requires developer significant efforts and descriptions in sync with code changes.
- Slower cross-team collaboration, as frontend teams must wait for backend deployment or manually inspect the code to understand the final API structure.

Recommendation:
I would choose an API-First Design approach with OpenAPI. In a production environment, clear contracts and smooth cross-team collaboration are paramount. The ability to generate robust client SDKs and guarantee up-to-date documentation outweighs the upfront learning curve and setup time. Hand-coding is best reserved for quick prototyping or internal utilities.

## Question 2: Testing Strategy

Given the need to balance thorough testing with time and resource constraints, how would you prioritize tests for this project? 

Which types of tests (unit, integration, parameterized, etc.) would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
Testing Strategy for Production Code

Given constraints, prioritize tests based on their return on investment (ROI) in confidence and fast feedback.

1. Integration Tests (Highest Priority for Use Cases):
- Rather than mock-heavy unit tests that test framework integrations, lean into Testcontainers with Quarkus Integration Tests. Testing the full vertical slice (REST -> Use Case -> DB) catches complex misconfigurations, transaction boundaries, and database query issues early. These tests mimic real-world behavior and provide the most confidence.

2. Unit Tests (For Complex Core Business Logic):
- Focus fast unit testing on strict domain logic with many edge cases (e.g., validations, capacity planning). Since these components shouldn't rely on the framework, unit tests run nearly instantly and cover wide combinatorics (best paired with Parameterized Tests).

3. Concurrency Tests (High Importance, Low Volume):
- Limit concurrency tests only to the riskiest, potentially highly-contended resources. Operations like inventory replacement or stock updates using optimistic locking need tests to ensure proper exceptions (e.g., OptimisticLockException) are mapped and handled. These tests are slow/brittle but prevent catastrophic data states.

4. End-to-End or Contract Tests (Lower Priority):
- Only use if multiple internal teams depend heavily on your endpoints, acting as a final sanity check post-deployment.

Ensuring Effective Coverage:
To ensure effectiveness over time, avoid testing implementation details (like asserting that a repository method was called) in favor of asserting the final database state or the returned response. Maintain code coverage metrics as PR gatekeepers, ensuring new features come with Testcontainers coverage.

