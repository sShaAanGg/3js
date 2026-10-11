---
name: backend-test
description: Build and test the Spring Boot backend, including on a machine without Java, Maven or Docker access. Use after any backend change.
---

# Backend test

1. Use local tools if present: `cd backend && mvn package`.
2. No JDK/Maven? Download them into a scratch dir (not the repo). Corretto's CDN is fast; Adoptium (served via GitHub) can crawl at ~25 KB/s.
   ```bash
   T=<scratch>/tools; mkdir -p $T/jdk $T/maven
   curl -fsSL https://corretto.aws/downloads/latest/amazon-corretto-21-x64-linux-jdk.tar.gz | tar -xz -C $T/jdk --strip-components=1
   curl -fsSL https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz | tar -xz -C $T/maven --strip-components=1
   JAVA_HOME=$T/jdk MAVEN_OPTS=-Xmx512m $T/maven/bin/mvn -B -f backend/pom.xml -Dmaven.repo.local=$T/m2 package
   ```
   Keep `MAVEN_OPTS=-Xmx512m`: the laptop has under 5 GB free.
3. Unit and repository tests run on H2, so they never execute `database/init.sql` or Postgres-specific SQL.
4. For SQL or schema changes, also run a **throwaway** smoke test on real Postgres. Add `io.zonky.test:embedded-postgres:2.1.0` (test scope) and a `@SpringBootTest` that loads `../database/init.sql`, then call the endpoints. Delete both afterwards and don't commit them.
5. In the PR, report the test counts, and say what was *not* run (e.g. `docker compose up`, the browser UI).

Gotcha: test timestamps must be in the past if code compares them with `Instant.now()`.
