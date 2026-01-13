# ☁️ OpenShift Deployment Architecture (PoC)

![OpenShift Architecture](../../docs/assets/openshift-architecture.png)

This directory contains the **Infrastructure as Code (IaC)** manifests required to deploy the `user-service` and its dependencies on the **Red Hat OpenShift Developer Sandbox**.

The deployment follows a **Declarative Approach**, prioritizing native OpenShift features (S2I, Routes, ImageStreams) while adapting to the resource and permission constraints of the Sandbox environment.

---

## 📂 Manifest Structure

| File | Description |
| :--- | :--- |
| `mysql-db.yaml` | **Persistence Layer:** MySQL 8.0 Deployment with PVC (1Gi) and internal ClusterIP Service. |
| `user-service-build.yaml` | **CI Pipeline:** Defines the `BuildConfig` using Source-to-Image (S2I) strategy to compile Java 21/Maven. |
| `user-service-deploy.yaml` | **Runtime:** Deployment, Service, and public Route. Includes Probes, Resource Limits, and Env Vars. |
| `webhook-rbac.yaml` | **Network Access:** Custom RoleBinding to allow GitHub to trigger the internal Webhook API from the internet. |
| `*-secret.yaml` | *(Ignored via .gitignore)* Contains actual credentials. Templates provided as `.example.yaml`. |

---

## 🧠 Architectural Decisions (PoC vs Enterprise)

### 1. Build Strategy: Native S2I (Source-to-Image)
*   **Implementation:** Used the official Red Hat UBI 9 OpenJDK 21 builder.
*   **Challenge:** The project is a **Maven Multi-Module** repo.
*   **Solution:** Configured `contextDir: /` to allow access to the parent POM and used `MAVEN_ARGS_APPEND: -pl ...` to isolate the build. Added `MAVEN_S2I_ARTIFACT_DIRS` to help the builder locate the nested JAR.

### 2. CI/CD Automation: Native Webhooks
*   **Constraint:** The Developer Sandbox restricts the installation of Operators (like **ArgoCD**) and limits Cluster-Scope permissions required for complex **Tekton** pipelines.
*   **Solution:** implemented a **Direct Webhook Trigger**.
*   **The RBAC Fix:** Since GitHub interacts as an unauthenticated client, I applied a specific `RoleBinding` (`webhook-rbac.yaml`) to allow the `system:unauthenticated` group to access *only* the Build Webhook API endpoint, secured by a secret token.

### 3. Configuration Management: Hybrid Approach
*   **Static Config:** Introduced a specific Spring profile (`application-openshift.yml`) in the source code to handle logging levels and disable unnecessary local tools (OpenTelemetry/Tracing) to keep logs clean.
*   **Dynamic Config:** Injected Database credentials and Auth0 Issuer URIs via Kubernetes **Secrets** and **Environment Variables** at runtime.

### 4. Secret Management
*   **Current Approach:** Local file application + Gitignore (Template pattern).
*   **Enterprise Standard:** In a real-world scenario, I would utilize **Sealed Secrets** (GitOps friendly encryption) or an **External Secrets Operator** integrated with HashiCorp Vault.

---

## 🚀 Deployment Lifecycle

The environment is designed to be **Self-Healing** and **Automated**:

1.  **Code Push:** Developer pushes to `poc-openshift` branch on GitHub.
2.  **Trigger:** GitHub Webhook hits the OpenShift API.
3.  **Build:** OpenShift spins up a Build Pod (memory tuned to avoid OOMKilled).
4.  **Update:** The `ImageStream` detects the new image.
5.  **Rollout:** The Deployment performs a **Rolling Update**, replacing the old Pod with the new version with zero downtime.

---

## 🛠️ How to Deploy (Manual Recreation)

If the Sandbox resets, restore the environment in this order:

```bash
# 1. Restore Database & Network Permissions
oc apply -f infra/openshift/mysql-db.yaml
oc apply -f infra/openshift/webhook-rbac.yaml

# 2. Restore Secrets (Local files)
oc apply -f infra/openshift/auth0-secret.yaml
oc apply -f infra/openshift/webhook-secret.yaml

# 3. Create Build Pipeline & Deployment
oc apply -f infra/openshift/user-service-build.yaml
oc apply -f infra/openshift/user-service-deploy.yaml

# 4. Trigger Initial Build (if webhook is not yet set)
oc start-build user-service-build