package com.i27academy.k8s

class K8s {
    def jenkins
    K8s(jenkins) {
        this.jenkins = jenkins
    }

    def auth_login(gke_cluster_name, gke_zone, gke_project) {
        jenkins.sh """#!/bin/bash
        echo "Entering Authentication method for GKE Cluster Login"
        gcloud config set account jenkins@delta-sprite-416312.iam.gserviceaccount.com
        # gcloud auth activate-service-account jenkins@nice-carving-411801.iam.gserviceaccount.com --key-file=key.json
        gcloud compute instances list
        echo "************* Listing Number of Nodes in K8S *************"
        gcloud container clusters get-credentials $gke_cluster_name --zone $gke_zone --project $gke_project
        kubectl get nodes
        """
    }
    def k8sdeploy(fileName, docker_image , namespace){
        jenkins.sh """#!/bin/bash
        echo "Executing K8S Deploy Method"
        echo "Final Image Tag is $docker_image"
        sed -i "s|DIT|$docker_image|g" ./.cicd/$fileName
        kubectl apply -f ./.cicd/$fileName -n $namespace
        """
    }

    def k8sHelmChartDeploy(appName, env, helmChartPath) {
       jenkins.sh """#!/bin/bash
       echo "*************** Helm Groovy method Starts here ***************"
       echo "Installing the Chart"
       helm install ${appName}-${env}-chart -f ./.cicd/k8s/values_${env}.yaml ${helmChartPath}
       # helm install chartname -f valuesfilepath chartpath
       # helm upgrade chartname -f valuefilepath chartpath
       """ 
    }

    def gitClone() {
       jenkins.sh """#!/bin/bash
       echo "*************** Entering Git Clone Method ***************"
       git clone -b master https://github.com/i27devopsb2/i27-shared-lib.git
       echo "Listing the files"
       ls -la 
       echo "Showing the files under i27-shared-lib repo"
       ls -la i27-shared-lib
       echo "Showing the files under chart folder"
       ls -la i27-shared-lib/chart/
       """ 
    }
    
}