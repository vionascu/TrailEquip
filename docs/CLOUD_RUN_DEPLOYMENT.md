# Deploy TrailEquip to Google Cloud Run (Free Tier)

This guide shows how to deploy TrailEquip to Google Cloud Run at no cost using the free tier. The free tier includes:
- **2 million requests per month** (more than enough for development/testing)
- **400,000 GB-seconds per month** of compute time
- **No credit card required** for 90 days

## Prerequisites

1. **Google Cloud Account** (free tier): https://cloud.google.com/free
2. **Google Cloud CLI** installed: https://cloud.google.com/sdk/docs/install
3. **Docker** installed locally (for testing builds)
4. **Project in GitLab** with `.gitlab-ci.yml` configured

## Step 1: Create a Google Cloud Project

```bash
# Login to Google Cloud
gcloud auth login

# Create a new project
gcloud projects create trailequip-free --name="TrailEquip Free"

# Set the project
gcloud config set project trailequip-free

# Enable required APIs
gcloud services enable cloudbuild.googleapis.com
gcloud services enable run.googleapis.com
gcloud services enable sqladmin.googleapis.com
gcloud services enable artifactregistry.googleapis.com
```

## Step 2: Set Up Cloud SQL Database (PostgreSQL 15)

```bash
# Create Cloud SQL instance (shared tier - free)
gcloud sql instances create trailequip-db \
  --database-version POSTGRES_15 \
  --tier db-f1-micro \
  --region us-central1 \
  --root-password=your-secure-password

# Create database
gcloud sql databases create trailequip --instance=trailequip-db

# Create application user
gcloud sql users create appuser \
  --instance=trailequip-db \
  --password=app-secure-password

# Get connection string
gcloud sql instances describe trailequip-db --format='value(connectionName)'
```

## Step 3: Enable PostGIS Extension

```bash
# Connect to database and enable PostGIS
gcloud sql connect trailequip-db --user=postgres

# In the psql prompt:
\c trailequip
CREATE EXTENSION postgis;
CREATE EXTENSION postgis_topology;
\q
```

## Step 4: Create Artifact Registry (for Docker images)

```bash
# Create Artifact Registry repository
gcloud artifacts repositories create trailequip-repo \
  --repository-format=docker \
  --location=us-central1 \
  --description="TrailEquip Docker images"

# Configure Docker authentication
gcloud auth configure-docker us-central1-docker.pkg.dev
```

## Step 5: Update GitLab CI/CD with Cloud Run Deployment

Update your `.gitlab-ci.yml` to add Cloud Run deployment:

```yaml
# Add these variables to the top of .gitlab-ci.yml
variables:
  GCP_PROJECT_ID: trailequip-free
  GCP_REGION: us-central1
  GCP_ARTIFACT_REGISTRY: us-central1-docker.pkg.dev/trailequip-free/trailequip-repo
  CLOUD_SQL_CONNECTION: trailequip-free:us-central1:trailequip-db

# Add this job to the deploy stage
deploy_cloud_run:
  stage: deploy
  image: google/cloud-sdk:alpine
  services:
    - docker:24-dind
  variables:
    DOCKER_HOST: tcp://docker:2375
    DOCKER_TLS_CERTDIR: ""
    DOCKER_DRIVER: overlay2
  script:
    # Authenticate with Google Cloud
    - echo $GCP_SERVICE_ACCOUNT_KEY | base64 -d > ${HOME}/gcp-key.json
    - gcloud auth activate-service-account --key-file ${HOME}/gcp-key.json
    - gcloud config set project $GCP_PROJECT_ID
    - gcloud auth configure-docker $GCP_ARTIFACT_REGISTRY

    # Build and push image
    - docker build -t $GCP_ARTIFACT_REGISTRY/trailequip:$CI_COMMIT_SHA .
    - docker push $GCP_ARTIFACT_REGISTRY/trailequip:$CI_COMMIT_SHA

    # Deploy to Cloud Run
    - |
      gcloud run deploy trailequip \
        --image=$GCP_ARTIFACT_REGISTRY/trailequip:$CI_COMMIT_SHA \
        --platform managed \
        --region=$GCP_REGION \
        --allow-unauthenticated \
        --memory=512Mi \
        --cpu=1 \
        --timeout=3600 \
        --set-env-vars="SPRING_DATASOURCE_URL=jdbc:postgresql://127.0.0.1:5432/trailequip,SPRING_JPA_HIBERNATE_DDL_AUTO=validate,SPRING_DATASOURCE_USERNAME=appuser,SPRING_DATASOURCE_PASSWORD=$DB_PASSWORD,SPRING_CLOUD_SQL_INSTANCES=$CLOUD_SQL_CONNECTION" \
        --add-cloudsql-instances=$CLOUD_SQL_CONNECTION
  only:
    - main
  when: manual
```

## Step 6: Create Service Account for GitLab CI/CD

```bash
# Create service account
gcloud iam service-accounts create gitlab-deployer \
  --display-name="GitLab TrailEquip Deployer"

# Grant necessary roles
gcloud projects add-iam-policy-binding $GCP_PROJECT_ID \
  --member=serviceAccount:gitlab-deployer@trailequip-free.iam.gserviceaccount.com \
  --role=roles/run.admin

gcloud projects add-iam-policy-binding $GCP_PROJECT_ID \
  --member=serviceAccount:gitlab-deployer@trailequip-free.iam.gserviceaccount.com \
  --role=roles/artifactregistry.writer

gcloud projects add-iam-policy-binding $GCP_PROJECT_ID \
  --member=serviceAccount:gitlab-deployer@trailequip-free.iam.gserviceaccount.com \
  --role=roles/cloudsql.client

# Create and download key
gcloud iam service-accounts keys create key.json \
  --iam-account=gitlab-deployer@trailequip-free.iam.gserviceaccount.com

# Encode key for GitLab
base64 key.json > key.txt
```

## Step 7: Add Secrets to GitLab CI/CD

In your GitLab project:

1. Go to **Settings → CI/CD → Variables**
2. Add these variables:
   - `GCP_SERVICE_ACCOUNT_KEY`: Paste contents of `key.txt` (base64 encoded)
   - `DB_PASSWORD`: Your PostgreSQL password from Step 2

## Step 8: Deploy!

Push to main branch or manually trigger the `deploy_cloud_run` job in GitLab CI/CD:

```bash
git push gitlab main
```

Monitor the pipeline in GitLab. Once complete, your app will be available at:

```
https://trailequip-RANDOM-HASH.a.run.app
```

## Accessing Your Application

After successful deployment, visit:
**https://trailequip-REGION-PROJECT.a.run.app**

Get the exact URL:
```bash
gcloud run services describe trailequip --region=us-central1 --format='value(status.url)'
```

## Monitoring and Logs

```bash
# View logs
gcloud run services describe trailequip --region=us-central1

# View recent logs
gcloud logging read "resource.type=cloud_run_revision AND resource.labels.service_name=trailequip" \
  --region=us-central1 \
  --limit 50 \
  --format json

# View metrics
gcloud monitoring metrics-descriptors list --filter="resource_type:cloud_run_revision"
```

## Free Tier Limits and Costs

| Resource | Free Tier | Overage Cost |
|----------|-----------|--------------|
| Cloud Run Invocations | 2M/month | $0.40 per 1M |
| Cloud Run CPU-seconds | 180,000/month | $0.00002417 per CPU-second |
| Cloud SQL (db-f1-micro) | $6.50/month quota | Included |
| Artifact Registry | 0.50 GB free/month | $0.10 per GB |

**Total estimated free cost**: ~$6.50/month for Cloud SQL

## Troubleshooting

### "Permission denied" when deploying
- Ensure service account has `roles/run.admin` and `roles/cloudsql.client`

### Database connection failed
- Verify Cloud SQL instance is running: `gcloud sql instances list`
- Check authorization for Cloud SQL Proxy

### Image too large (>512 MB)
- Cloud Run max image size is 2 GB, but reduce to improve cold start time
- Consider using smaller base image or multi-stage builds

### Application times out
- Increase timeout: `--timeout=3600` (up to 3600 seconds max)
- Check logs for slow queries

## Alternative: Use GitHub Pages + Render

For a completely free solution without time limits:

1. **Frontend**: Deploy static build to GitHub Pages (free)
2. **Backend**: Deploy to Render.com free tier (hibernates after 15 min inactivity)

See `docs/DEPLOYMENT_GUIDE.md` for other options.

## Clean Up (when done testing)

```bash
# Delete Cloud Run service
gcloud run services delete trailequip --region=us-central1

# Delete Cloud SQL instance
gcloud sql instances delete trailequip-db

# Delete Artifact Registry
gcloud artifacts repositories delete trailequip-repo --location=us-central1

# Delete project
gcloud projects delete trailequip-free
```

---

**Next Steps**: After successful Cloud Run deployment, you'll have a public URL that anyone can access without installing anything or paying!
