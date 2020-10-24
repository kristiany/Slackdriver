# Slackdriver
## Doesn't Google Cloud already support this?
Sort of, via [email forwarding](https://cloud.google.com/error-reporting/docs/notifications).


## Google Stackdriver error reporting monitor 

Monitoring the slightly exceptional Google Cloud service Stackdriver Error reporting and push the errors to your Slack instance. Stackdriver error reporting doesn't provide much other information than a stack trace, so this app is enriching the errors with the log event to extract things like pod name etc.   

Currently monitoring errors for GKE, Kubernetes cluster, and Google Function. If you need more error support, please create a PR.

1. Build the code:
    ```
    ./gradlew build shadowJar
    ``` 
2. Build the docker image:
    ```
    docker build . -t error-monitor
    ```
3. Run the docker image:
    ```
    docker run -e GOOGLE_APPLICATION_CREDENTIALS="" -e SLACK_HOOK="" -e PROJECT_ID="" -v /[path to your Gcloud credentials config directory]:/app/creds error-monitor
    ```   
    * `GOOGLE_APPLICATION_CREDENTIALS` - Docker local path to you Google Cloud service-account json file. If you used the mounted path displayed in the docker run command above, your file path should start with `creds/`
    * `SLACK_HOOK` - Your default Slack hook URL
    * `PROJECT_ID` - Google Cloud Project id

### Setup for running in Google Cloud
A service account is needed, with the following access:
* Error reporting read access https://cloud.google.com/error-reporting/docs/iam
* Logs read access https://cloud.google.com/logging/docs/access-control

To create the service account via the CLI, set `PROJECT` to your project and run:
```
PROJECT="my-project" gcloud beta iam service-accounts create error-monitor \
  --description "Read access to errors and logs" \
  --display-name "error-monitor" \
  && gcloud projects add-iam-policy-binding $PROJECT \
  --member serviceAccount:error-monitor@${PROJECT}.iam.gserviceaccount.com \
  --role roles/errorreporting.viewer \
  && gcloud projects add-iam-policy-binding $PROJECT \
  --member serviceAccount:error-monitor@$PROJECT.iam.gserviceaccount.com \
  --role roles/logging.viewer
```

Create a key-file:
```
PROJECT="my-project" gcloud iam service-accounts keys create error-monitor-keyfile.json --iam-account error-monitor@$PROJECT.iam.gserviceaccount.com
``` 

Load the key-file as a Kubernetes secret:
```
kubectl create secret generic error-monitor-keyfile --from-file=error-monitor-keyfile.json
```

Enable Stackdriver Error Reporting API for your project if you haven't already: https://console.developers.google.com/apis/library/clouderrorreporting.googleapis.com

    
### Deploy to Kubernetes via Google Cloud Container Registry
1. Run a local Cloud Build to store the image in your Google respository
    ```
    gcloud builds submit . --config=cloudbuild.yaml
    ```
2. Edit the environment variables and the image name in the Kubernetes manifest `app.yaml`, you might also want to add `imagePullPolicy: Always`
3. Deploy it in your cluster   
