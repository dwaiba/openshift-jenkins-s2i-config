# Innovation Labs Jenkins Master Configuration
This repo is used to build a customized OpenShift Jenkins 2 image with [source to image (S2I)](https://github.com/openshift/source-to-image). The base OpenShift Jenkins S2I can be found at `registry.access.redhat.com/openshift3/jenkins-2-rhel7`. The resulting image is a Jenkins master, and should be used in a master / slaves architecture. This image is configured to provide slaves as k8s pods via the [k8s Jenkins plugin](https://docs.openshift.com/container-platform/3.5/using_images/other_images/jenkins.html#using-the-jenkins-kubernetes-plug-in-to-run-jobs). Thus, this repo doesn't define any build tools or the like, as they are the responsibility of the slaves.

It's advised to use this configuration in combination with the out of the box OpenShift Jenkins templates, as demonstrated in [this example](https://github.com/rht-labs/examples/tree/jenkins-ocp-templates/jenkins-ocp-templates).

## How This Repo Works

The directory structure is dictated by [OpenShift Jenkins S2I image](https://docs.openshift.com/container-platform/3.5/using_images/other_images/jenkins.html#jenkins-as-s2i-builder). In particular:

- [plugins.txt](plugins.txt) is used to install plugins during the S2I build. If you want the details, here is the [S2I assemble script](https://github.com/openshift/jenkins/blob/master/2/contrib/s2i/assemble), which calls the [install jenkins plugins script](https://github.com/openshift/jenkins/blob/master/2/contrib/jenkins/install-plugins.sh).
- files in the [configuration](configuration) directory will have comments describing exactly what they do

### Slack Integration
To Integrate with slack follow the steps at https://github.com/jenkinsci/slack-plugin. Particulary, create a webhook at  https://customteamname.slack.com/services/new/jenkins-ci. After the webhook setup is complete at slack, record and add the environmental variables for:
1. The base url as SLACK_BASE_URL
2. The slack token as SLACK_TOKEN
3. The slack room you selected as the default slack channel as SLACK_ROOM
4. optionally, a jenkins credential can be used for the token and referenced by a custom id at SLACK_TOKEN_CREDENTIAL_ID. This takes precedences over the SLACK_TOKEN

### SonarQube Integration
By default the deployment will attempt to connect to SonarQube and configure its setup including an authentication token. The default url is http://sonarqube:9000. This can be overriden adding an environment variable named SONARQUBE_URL. To disable SonarQube entirely set an environment variable named DISABLE_SONAR with any value.

## Contributing

There are some [helpers](helpers/README.MD) to get configuration out of a running Jenkins. See [the guide](https://github.com/rht-labs/api-design/blob/master/CONTRIBUTING.md) for opening PRs/Issues.

### License
[ASL 2.0](LICENSE)
